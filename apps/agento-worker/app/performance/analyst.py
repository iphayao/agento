"""Performance Analyst agent nodes.

Analyses historical content performance records to extract winning hooks,
angles, CTAs, and channel signals. Reuses the shared provider-abstracted LLM helpers.
"""

import datetime
import logging

from app.performance.models import AnalysisState, InsightItem, SummaryItem
from app.workflow.agents import _call_llm, _parse_json

logger = logging.getLogger(__name__)


def _top(records: list[dict], key: str, n: int = 5) -> list[dict]:
    return sorted(records, key=lambda r: float(r.get(key, 0)), reverse=True)[:n]


def _bottom(records: list[dict], key: str, n: int = 5) -> list[dict]:
    return sorted(records, key=lambda r: float(r.get(key, 0)))[:n]


def _record_summary(records: list[dict]) -> str:
    lines = []
    for r in records[:20]:
        lines.append(
            f"- Hook: {r.get('hook', '')[:80]!r} | CTA: {r.get('callToAction', '')[:60]!r} "
            f"| Channel: {r.get('channel', '')} "
            f"| EngRate: {float(r.get('engagementRate', 0)):.4f} "
            f"| ConvRate: {float(r.get('conversionRate', 0)):.4f} "
            f"| Revenue: {float(r.get('revenue', 0)):.2f} "
            f"| ROAS: {float(r.get('roas', 0)):.2f} "
            f"| Orders: {r.get('orders', 0)}"
        )
    return "\n".join(lines)


async def hook_analyzer_node(state: AnalysisState) -> dict:
    records = state["records"]
    if not records:
        return {**state, "top_hooks": [], "weak_hooks": []}

    top_records = _top(records, "engagementRate", 5)
    weak_records = _bottom(records, "engagementRate", 5)

    system = """You are a content performance analyst for a Thai FMCG brand.
Analyze which content hooks drove the highest and lowest engagement.
Respond ONLY with valid JSON:
{"winningHooks": ["string"], "weakHooks": ["string"]}"""

    user = (
        f"Top engaging content:\n{_record_summary(top_records)}\n\n"
        f"Lowest engaging content:\n{_record_summary(weak_records)}\n\n"
        "Identify 3-5 winning hook patterns and 2-3 weak hook patterns. Be specific."
    )

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        return {
            **state,
            "top_hooks": result.get("winningHooks", []),
            "weak_hooks": result.get("weakHooks", []),
        }
    except Exception as e:
        logger.warning("hook_analyzer failed: %s", e)
        return {**state, "errors": state.get("errors", []) + [f"hook_analyzer: {e}"]}


async def angle_analyzer_node(state: AnalysisState) -> dict:
    records = state["records"]
    if not records:
        return {**state, "top_angles": [], "weak_angles": []}

    top_records = _top(records, "revenue", 5)
    weak_records = [r for r in records if int(r.get("orders", 0)) == 0][:5]

    system = """You are a content performance analyst for a Thai FMCG brand.
Analyze which content angles drove the highest revenue vs. lowest.
Focus on the title and hook to infer the angle.
Respond ONLY with valid JSON:
{"winningAngles": ["string"], "weakAngles": ["string"]}"""

    user = (
        f"Top revenue content:\n{_record_summary(top_records)}\n\n"
        f"Zero-order content:\n{_record_summary(weak_records) if weak_records else '(none)'}\n\n"
        "Identify 3-5 winning angles and 2-3 weak angles based on the content patterns."
    )

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        return {
            **state,
            "top_angles": result.get("winningAngles", []),
            "weak_angles": result.get("weakAngles", []),
        }
    except Exception as e:
        logger.warning("angle_analyzer failed: %s", e)
        return {**state, "errors": state.get("errors", []) + [f"angle_analyzer: {e}"]}


async def cta_analyzer_node(state: AnalysisState) -> dict:
    records = state["records"]
    if not records:
        return {**state, "top_ctas": [], "weak_ctas": []}

    top_records = _top(records, "conversionRate", 5)
    weak_records = _bottom(records, "conversionRate", 5)

    system = """You are a content performance analyst for a Thai FMCG brand.
Analyze which calls-to-action (CTAs) drove the highest and lowest conversion rates.
Respond ONLY with valid JSON:
{"strongCTAs": ["string"], "weakCTAs": ["string"]}"""

    user = (
        f"Highest converting content:\n{_record_summary(top_records)}\n\n"
        f"Lowest converting content:\n{_record_summary(weak_records)}\n\n"
        "Identify 3-5 strong CTA patterns and 2-3 weak CTA patterns."
    )

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        return {
            **state,
            "top_ctas": result.get("strongCTAs", []),
            "weak_ctas": result.get("weakCTAs", []),
        }
    except Exception as e:
        logger.warning("cta_analyzer failed: %s", e)
        return {**state, "errors": state.get("errors", []) + [f"cta_analyzer: {e}"]}


async def channel_analyzer_node(state: AnalysisState) -> dict:
    records = state["records"]
    if not records:
        return {**state, "channel_signals": []}

    channel_stats: dict[str, dict] = {}
    for r in records:
        ch = r.get("channel", "unknown")
        if ch not in channel_stats:
            channel_stats[ch] = {"count": 0, "eng": 0.0, "rev": 0.0, "roas": 0.0}
        channel_stats[ch]["count"] += 1
        channel_stats[ch]["eng"] += float(r.get("engagementRate", 0))
        channel_stats[ch]["rev"] += float(r.get("revenue", 0))
        channel_stats[ch]["roas"] += float(r.get("roas", 0))

    channel_summary = "\n".join(
        f"- {ch}: {v['count']} posts, avg engagement={v['eng']/v['count']:.4f}, "
        f"total revenue={v['rev']:.2f}, avg ROAS={v['roas']/v['count']:.2f}"
        for ch, v in channel_stats.items()
    )

    system = """You are a channel performance analyst for a Thai e-commerce brand.
Analyze which channels perform best based on engagement, revenue, and ROAS.
Respond ONLY with valid JSON:
{"channelSignals": ["string"], "channelRecommendations": ["string"]}"""

    user = (
        f"Channel performance breakdown:\n{channel_summary}\n\n"
        "Provide 3-5 specific channel signals and recommendations."
    )

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        signals = result.get("channelSignals", []) + result.get("channelRecommendations", [])
        return {**state, "channel_signals": signals}
    except Exception as e:
        logger.warning("channel_analyzer failed: %s", e)
        return {**state, "errors": state.get("errors", []) + [f"channel_analyzer: {e}"]}


async def insight_builder_node(state: AnalysisState) -> dict:
    """Assemble InsightItem list from all analysis results."""
    insights: list[dict] = []

    for hook in state.get("top_hooks", []):
        insights.append(InsightItem(insightType="WINNING_HOOK",
                                    insightText=hook,
                                    confidenceScore=0.80).model_dump())
    for hook in state.get("weak_hooks", []):
        insights.append(InsightItem(insightType="WINNING_HOOK",
                                    insightText=f"Avoid this hook pattern: {hook}",
                                    confidenceScore=0.55).model_dump())
    for angle in state.get("top_angles", []):
        insights.append(InsightItem(insightType="WINNING_ANGLE",
                                    insightText=angle,
                                    confidenceScore=0.78).model_dump())
    for angle in state.get("weak_angles", []):
        insights.append(InsightItem(insightType="LOW_PERFORMING_ANGLE",
                                    insightText=angle,
                                    confidenceScore=0.70).model_dump())
    for cta in state.get("top_ctas", []):
        insights.append(InsightItem(insightType="STRONG_CTA",
                                    insightText=cta,
                                    confidenceScore=0.80).model_dump())
    for cta in state.get("weak_ctas", []):
        insights.append(InsightItem(insightType="WEAK_CTA",
                                    insightText=cta,
                                    confidenceScore=0.70).model_dump())
    for signal in state.get("channel_signals", []):
        insights.append(InsightItem(insightType="CHANNEL_SIGNAL",
                                    insightText=signal,
                                    confidenceScore=0.72).model_dump())

    return {**state, "insights": insights}


async def summary_node(state: AnalysisState) -> dict:
    """Generate a human-readable performance summary and recommendations."""
    today = datetime.date.today()
    period_start = str(today - datetime.timedelta(days=30))
    period_end = str(today)

    system = """You are a marketing performance strategist for a Thai FMCG brand.
Write a concise performance summary and actionable recommendations.
Respond ONLY with valid JSON:
{
  "summaryText": "string (2-3 paragraphs)",
  "recommendedAngles": ["string"],
  "recommendedHooks": ["string"],
  "recommendedCTAs": ["string"],
  "avoidPatterns": ["string"]
}"""

    context_parts = []
    if state.get("top_hooks"):
        context_parts.append(f"Winning hooks: {', '.join(state['top_hooks'][:3])}")
    if state.get("top_angles"):
        context_parts.append(f"Winning angles: {', '.join(state['top_angles'][:3])}")
    if state.get("top_ctas"):
        context_parts.append(f"Strong CTAs: {', '.join(state['top_ctas'][:3])}")
    if state.get("weak_angles"):
        context_parts.append(f"Weak angles to avoid: {', '.join(state['weak_angles'][:2])}")
    if state.get("channel_signals"):
        context_parts.append(f"Channel signals: {', '.join(state['channel_signals'][:2])}")

    user = (
        f"Performance analysis results for channel: {state.get('channel', 'all')}\n"
        + "\n".join(context_parts)
        + "\n\nWrite a strategic summary with specific recommendations for next content."
    )

    try:
        raw = await _call_llm(system, user)
        result = _parse_json(raw)
        summary = SummaryItem(
            periodStart=period_start,
            periodEnd=period_end,
            channel=state.get("channel") if state.get("channel") != "all" else None,
            summaryText=result.get("summaryText", ""),
            recommendedAngles=result.get("recommendedAngles", []),
            recommendedHooks=result.get("recommendedHooks", []),
            recommendedCTAs=result.get("recommendedCTAs", []),
            avoidPatterns=result.get("avoidPatterns", []),
        )
        return {**state, "summary": summary.model_dump()}
    except Exception as e:
        logger.warning("summary_node failed: %s", e)
        return {**state, "errors": state.get("errors", []) + [f"summary_node: {e}"]}
