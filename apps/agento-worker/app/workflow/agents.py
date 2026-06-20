"""LangGraph agent node functions.

Each node receives WorkflowState, calls an LLM, calls back to Spring Boot,
and returns the updated state slice.

Provider abstraction: all LLM calls go through _call_llm().
Set AI_PROVIDER=openai (default) and configure AI_API_KEY.
Anthropic: set AI_PROVIDER=anthropic, AI_API_KEY=sk-ant-...
"""

import json
import logging
import re
import unicodedata
from typing import Any

import httpx
from tenacity import retry, retry_if_exception_type, stop_after_attempt, wait_exponential

from app import cancellation
from app.callback import notify_step
from app.config import settings
from app.workflow.state import WorkflowState

logger = logging.getLogger(__name__)

# ─── Prohibited terms (mirrors ComplianceChecker.java) ───────────────────────

_PROHIBITED_TH = [
    "ไร้ฝุ่น 100%", "ไม่มีฝุ่นเลย", "ปลอดฝุ่นสมบูรณ์แบบ",
    "antibacterial", "ฆ่าเชื้อโรค", "medically safe", "ปลอดภัยทางการแพทย์",
    "hypoallergenic", "dermatologist tested", "dermatologist approved",
    "ทดสอบโดยแพทย์", "สะอาดที่สุด", "นุ่มที่สุด", "ปลอดภัยที่สุด",
    "ดีที่สุด", "อันดับ 1", "ขายดีที่สุด",
]
_PROHIBITED_EN = [
    "dust-free", "zero dust", "100% dust", "antibacterial", "medically safe",
    "hypoallergenic", "dermatologist", "safest", "cleanest", "softest", "best tissue",
]
_PROHIBITED_TH_NFC = [unicodedata.normalize("NFC", t) for t in _PROHIBITED_TH]


def find_prohibited_terms(text: str) -> list[str]:
    if not text:
        return []
    normalized = unicodedata.normalize("NFC", text)
    lower = normalized.lower()
    found: list[str] = []
    for i, term in enumerate(_PROHIBITED_TH_NFC):
        if term in normalized:
            found.append(_PROHIBITED_TH[i])
    for term in _PROHIBITED_EN:
        if term.lower() in lower:
            found.append(term)
    return found


# ─── LLM Provider (singleton — created once at module import) ─────────────────

def _create_llm_client() -> tuple[str, Any]:
    """Initialize the LLM client from config. Called once at import time."""
    if settings.ai_provider.lower() == "anthropic":
        try:
            import anthropic
            return "anthropic", anthropic.AsyncAnthropic(api_key=settings.ai_api_key)
        except ImportError as exc:
            raise ImportError("pip install anthropic to use AI_PROVIDER=anthropic") from exc
    else:
        from openai import AsyncOpenAI
        return "openai", AsyncOpenAI(
            api_key=settings.ai_api_key,
            base_url=settings.ai_base_url,
        )


_LLM_PROVIDER, _LLM_CLIENT = _create_llm_client()


@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type((httpx.HTTPError, httpx.TimeoutException, TimeoutError, ConnectionError)),
    reraise=True,
)
async def _call_llm(system_prompt: str, user_prompt: str) -> str:
    """Call the configured LLM. Retries only on transient network errors."""
    if _LLM_PROVIDER == "anthropic":
        message = await _LLM_CLIENT.messages.create(
            model=settings.ai_model,
            max_tokens=settings.ai_max_tokens,
            system=system_prompt,
            messages=[{"role": "user", "content": user_prompt}],
        )
        return message.content[0].text

    # OpenAI-compatible
    response = await _LLM_CLIENT.chat.completions.create(
        model=settings.ai_model,
        temperature=settings.ai_temperature,
        max_tokens=settings.ai_max_tokens,
        response_format={"type": "json_object"},
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
    )
    return response.choices[0].message.content


def _parse_json(raw: str) -> dict:
    """Parse JSON from LLM response, stripping markdown fences if present."""
    clean = re.sub(r"```(?:json)?", "", raw).strip()
    return json.loads(clean)


def _brand_context(brand: dict) -> str:
    lines = [f"Brand: {brand.get('brandName', 'SoClean')}"]
    if brand.get("slogan"):
        lines.append(f"Slogan: {brand['slogan']}")
    if brand.get("toneOfVoice"):
        lines.append(f"Tone: {brand['toneOfVoice']}")
    if brand.get("targetAudience"):
        lines.append(f"Target audience: {brand['targetAudience']}")
    if brand.get("keyMessages"):
        lines.append(f"Key messages: {', '.join(brand['keyMessages'])}")
    return "\n".join(lines)


def _product_context(products: list[dict]) -> str:
    if not products:
        return "Product: SoClean facial tissue, 2-ply, 180 sheets, pack of 5"
    parts = []
    for p in products:
        desc = (
            f"{p.get('productName', 'SoClean')}: "
            f"{p.get('ply', 2)}-ply, {p.get('sheetCount', 180)} sheets, "
            f"pack of {p.get('packSize', 5)}, carton of {p.get('cartonSize', 50)}"
        )
        if p.get("keyBenefits"):
            desc += f". Benefits: {', '.join(p['keyBenefits'])}"
        parts.append(desc)
    return "\n".join(parts)


CLAIM_SAFETY_RULES = """
CRITICAL CLAIM SAFETY RULES (non-negotiable):
- NEVER use: ไร้ฝุ่น 100%, ไม่มีฝุ่นเลย, antibacterial, ฆ่าเชื้อโรค, medically safe,
  ปลอดภัยทางการแพทย์, hypoallergenic, dermatologist, สะอาดที่สุด, นุ่มที่สุด,
  ปลอดภัยที่สุด, ดีที่สุด, อันดับ 1, ขายดีที่สุด, dust-free, zero dust, safest, cleanest
- USE INSTEAD: ฝุ่นน้อย, เนียนนุ่ม, ให้สัมผัสสะอาด, เหมาะกับการใช้งานทุกวัน, คุ้มค่า
"""

# ─── Guard helpers ─────────────────────────────────────────────────────────────


async def _should_skip(state: WorkflowState, step: str) -> bool:
    """Return True if this step should be skipped due to prior errors or cancellation.

    Notifies Spring Boot with SKIPPED status so the UI can reflect it.
    """
    wf_id = state["workflow_id"]
    if state.get("errors") or cancellation.is_cancelled(wf_id):
        await notify_step(
            state["callback_base_url"], state["callback_api_key"], wf_id, step, "SKIPPED"
        )
        return True
    return False


# ─── Node functions ────────────────────────────────────────────────────────────

async def brand_strategist_node(state: WorkflowState) -> dict:
    """Step 1: Produce a campaign strategy brief."""
    step = "brand_strategist"
    wf_id = state["workflow_id"]
    base_url = state["callback_base_url"]
    api_key = state["callback_api_key"]

    # Only check cancellation for the first node (no prior errors possible)
    if cancellation.is_cancelled(wf_id):
        await notify_step(base_url, api_key, wf_id, step, "SKIPPED")
        return {**state, "current_step": step}

    await notify_step(base_url, api_key, wf_id, step, "RUNNING")

    system = f"""You are a senior brand strategist for a Thai FMCG company.
{_brand_context(state['brand'])}
{CLAIM_SAFETY_RULES}
Respond ONLY with valid JSON: {{"strategyBrief": "string (2-3 paragraphs)"}}"""

    campaign = state["campaign"]
    user = (
        f"Campaign: {campaign.get('name')}\n"
        f"Objective: {campaign.get('objective')}\n"
        f"Channel: {campaign.get('channel')}\n"
        f"Target audience: {campaign.get('targetAudience')}\n"
        f"Content angle: {campaign.get('contentAngle')}\n\n"
        "Write a strategy brief for this campaign. Focus on positioning, messaging pillars, "
        "and emotional triggers. Write in English."
    )

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        brief = result.get("strategyBrief", raw)
        output = json.dumps({"strategyBrief": brief})
        await notify_step(base_url, api_key, wf_id, step, "COMPLETED",
                          output=output, input_payload=user)
        return {**state, "strategy_brief": brief, "current_step": step}
    except Exception as exc:
        msg = f"brand_strategist failed: {exc}"
        await notify_step(base_url, api_key, wf_id, step, "FAILED", error=msg)
        return {**state, "errors": state.get("errors", []) + [msg], "current_step": step}


async def customer_insight_node(state: WorkflowState) -> dict:
    """Step 2: Map customer pain points and messaging triggers."""
    step = "customer_insight"
    wf_id = state["workflow_id"]
    base_url = state["callback_base_url"]
    api_key = state["callback_api_key"]

    if await _should_skip(state, step):
        return {**state, "current_step": step}

    await notify_step(base_url, api_key, wf_id, step, "RUNNING")

    campaign = state["campaign"]
    system = f"""You are a customer insight researcher for Thai consumer markets.
{_brand_context(state['brand'])}
Strategy brief: {state.get('strategy_brief', '')}
Respond ONLY with valid JSON:
{{"painPoints": ["string"], "triggers": ["string"], "messagingPillars": ["string"]}}"""

    user = (
        f"Target audience: {campaign.get('targetAudience')}\n"
        f"Channel: {campaign.get('channel')}\n"
        f"Content angle: {campaign.get('contentAngle')}\n\n"
        "Identify 3-5 pain points, emotional triggers, and messaging pillars "
        "that will resonate with this audience for tissue paper."
    )

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        output = json.dumps(result)
        await notify_step(base_url, api_key, wf_id, step, "COMPLETED",
                          output=output, input_payload=user)
        insight_text = (
            f"Pain points: {', '.join(result.get('painPoints', []))}\n"
            f"Triggers: {', '.join(result.get('triggers', []))}\n"
            f"Pillars: {', '.join(result.get('messagingPillars', []))}"
        )
        return {**state, "customer_insights": insight_text, "current_step": step}
    except Exception as exc:
        msg = f"customer_insight failed: {exc}"
        await notify_step(base_url, api_key, wf_id, step, "FAILED", error=msg)
        return {**state, "errors": state.get("errors", []) + [msg], "current_step": step}


async def content_writer_node(state: WorkflowState) -> dict:
    """Step 3: Write the initial content draft."""
    step = "content_writer"
    wf_id = state["workflow_id"]
    base_url = state["callback_base_url"]
    api_key = state["callback_api_key"]

    if await _should_skip(state, step):
        return {**state, "current_step": step}

    await notify_step(base_url, api_key, wf_id, step, "RUNNING")

    campaign = state["campaign"]
    channel = campaign.get("channel", "tiktok")

    system = f"""You are a Thai marketing content writer.
{_brand_context(state['brand'])}
Products: {_product_context(state['products'])}
Customer insights: {state.get('customer_insights', '')}
{CLAIM_SAFETY_RULES}

Write content suitable for: {channel}
Respond ONLY with valid JSON:
{{"title":"string","hook":"string (Thai, 1-2 sentences)","body":"string (Thai, conversational)","callToAction":"string (Thai)","hashtags":["#SoClean","#ทิชชู่SoClean"]}}"""

    user = (
        f"Campaign: {campaign.get('name')}\n"
        f"Objective: {campaign.get('objective')}\n"
        f"Content angle: {campaign.get('contentAngle')}\n\n"
        "Write engaging Thai content for this campaign. "
        "Hook must grab attention in 3 seconds. Body should build desire. "
        "Always include #SoClean #ทิชชู่SoClean #BNPaper in hashtags."
    )

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        # Ensure required hashtags are present
        hashtags = result.get("hashtags", [])
        for required in ["#SoClean", "#ทิชชู่SoClean", "#BNPaper"]:
            if required not in hashtags:
                hashtags.append(required)
        result["hashtags"] = hashtags
        output = json.dumps(result)
        await notify_step(base_url, api_key, wf_id, step, "COMPLETED",
                          output=output, input_payload=user)
        return {**state, "content_draft": result, "current_step": step}
    except Exception as exc:
        msg = f"content_writer failed: {exc}"
        await notify_step(base_url, api_key, wf_id, step, "FAILED", error=msg)
        return {**state, "errors": state.get("errors", []) + [msg], "current_step": step}


async def claim_compliance_node(state: WorkflowState) -> dict:
    """Step 4: Check content for prohibited claims and flag warnings."""
    step = "claim_compliance"
    wf_id = state["workflow_id"]
    base_url = state["callback_base_url"]
    api_key = state["callback_api_key"]

    if await _should_skip(state, step):
        return {**state, "current_step": step}

    await notify_step(base_url, api_key, wf_id, step, "RUNNING")

    draft = state.get("content_draft") or {}
    full_text = " ".join([
        draft.get("title", ""),
        draft.get("hook", ""),
        draft.get("body", ""),
        draft.get("callToAction", ""),
    ])

    # Server-side term check (deterministic, no LLM needed)
    server_warnings = find_prohibited_terms(full_text)

    # LLM also reviews for subtle compliance issues
    system = f"""You are a compliance reviewer for Thai marketing content.
{CLAIM_SAFETY_RULES}
Your task: review the content below and flag any prohibited terms or unsafe claims.
Respond ONLY with valid JSON:
{{"isSafe": boolean, "prohibitedTermsFound": ["string"], "suggestedRevisions": ["string"]}}"""

    user = f"Review this content:\n{json.dumps(draft, ensure_ascii=False)}"

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        ai_warnings = result.get("prohibitedTermsFound", [])

        # Merge server + AI findings
        all_warnings = list(set(server_warnings + ai_warnings))
        compliance_result = {
            "isSafe": len(all_warnings) == 0,
            "warnings": all_warnings,
            "suggestedRevisions": result.get("suggestedRevisions", []),
        }
        output = json.dumps(compliance_result)
        await notify_step(base_url, api_key, wf_id, step, "COMPLETED",
                          output=output, input_payload=user)
        return {
            **state,
            "compliance_result": compliance_result,
            "compliance_warnings": all_warnings,
            "current_step": step,
        }
    except Exception as exc:
        # Fallback: use server-side check only — log the LLM failure but don't fail the step
        logger.warning("LLM compliance check failed, using server-side only: %s", exc)
        compliance_result = {
            "isSafe": len(server_warnings) == 0,
            "warnings": server_warnings,
            "suggestedRevisions": [],
        }
        output = json.dumps(compliance_result)
        await notify_step(base_url, api_key, wf_id, step, "COMPLETED", output=output)
        return {
            **state,
            "compliance_result": compliance_result,
            "compliance_warnings": server_warnings,
            "current_step": step,
        }


async def editor_node(state: WorkflowState) -> dict:
    """Step 5: Refine content based on compliance feedback and brand voice."""
    step = "editor"
    wf_id = state["workflow_id"]
    base_url = state["callback_base_url"]
    api_key = state["callback_api_key"]

    if await _should_skip(state, step):
        return {**state, "current_step": step}

    await notify_step(base_url, api_key, wf_id, step, "RUNNING")

    draft = state.get("content_draft") or {}
    compliance = state.get("compliance_result") or {}
    warnings = compliance.get("warnings", [])
    revisions = compliance.get("suggestedRevisions", [])

    system = f"""You are a senior content editor for a Thai FMCG brand.
{_brand_context(state['brand'])}
{CLAIM_SAFETY_RULES}

Your task: refine the content draft to:
1. Fix any prohibited terms: {warnings if warnings else 'none found'}
2. Apply suggested revisions: {revisions if revisions else 'none'}
3. Improve clarity, flow, and emotional impact
4. Ensure the tone matches: {state['brand'].get('toneOfVoice', 'warm and honest')}

Respond ONLY with valid JSON (same schema as input):
{{"title":"string","hook":"string","body":"string","callToAction":"string","hashtags":["string"]}}"""

    user = f"Original draft:\n{json.dumps(draft, ensure_ascii=False)}"

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        # Preserve required hashtags
        hashtags = result.get("hashtags", draft.get("hashtags", []))
        for required in ["#SoClean", "#ทิชชู่SoClean", "#BNPaper"]:
            if required not in hashtags:
                hashtags.append(required)
        result["hashtags"] = hashtags
        output = json.dumps(result)
        await notify_step(base_url, api_key, wf_id, step, "COMPLETED",
                          output=output, input_payload=user)
        return {**state, "edited_content": result, "current_step": step}
    except Exception as exc:
        # Fallback: use draft as-is — editor failure is non-critical
        logger.warning("Editor LLM failed, using draft as-is: %s", exc)
        await notify_step(base_url, api_key, wf_id, step, "COMPLETED",
                          output=json.dumps(draft))
        return {**state, "edited_content": draft, "current_step": step}


async def final_formatter_node(state: WorkflowState) -> dict:
    """Step 6: Format final output as structured JSON for saving as GeneratedContent."""
    step = "final_formatter"
    wf_id = state["workflow_id"]
    base_url = state["callback_base_url"]
    api_key = state["callback_api_key"]

    if await _should_skip(state, step):
        return {**state, "current_step": step}

    await notify_step(base_url, api_key, wf_id, step, "RUNNING")

    content = state.get("edited_content") or state.get("content_draft") or {}
    warnings = state.get("compliance_warnings", [])

    compliance_notes = (
        "Claim-safe — no prohibited terms detected"
        if not warnings
        else f"WARNING — prohibited terms detected: {', '.join(warnings)}"
    )

    final_output = {
        "title": content.get("title", ""),
        "hook": content.get("hook", ""),
        "body": content.get("body", ""),
        "callToAction": content.get("callToAction", ""),
        "hashtags": content.get("hashtags", ["#SoClean", "#ทิชชู่SoClean", "#BNPaper"]),
        "complianceNotes": compliance_notes,
    }

    output = json.dumps(final_output, ensure_ascii=False)
    await notify_step(base_url, api_key, wf_id, step, "COMPLETED", output=output)

    return {**state, "final_output": final_output, "current_step": step}
