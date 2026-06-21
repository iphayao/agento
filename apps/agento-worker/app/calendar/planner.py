"""Calendar Planner Agent — generates content item suggestions for a date range.

Single LangGraph node: calls the LLM with brand/product context and period info,
parses the structured JSON response, and returns a list of CalendarItemSuggestion.
"""

import logging

from app.calendar.models import CalendarItemSuggestion, CalendarPlanRequest
from app.workflow.agents import _call_llm, _parse_json

logger = logging.getLogger(__name__)

_CHANNELS = ["tiktok", "shopee", "lazada", "facebook", "reseller"]
_CONTENT_TYPES = [
    "TIKTOK_CAPTION", "TIKTOK_SCRIPT", "SHOPEE_DESCRIPTION",
    "LAZADA_DESCRIPTION", "FACEBOOK_POST", "RESELLER_POST",
]


async def generate_plan(request: CalendarPlanRequest) -> list[CalendarItemSuggestion]:
    """Call the LLM to suggest calendar items for the given period."""

    brand = request.brand
    products = request.products

    brand_summary = (
        f"Brand: {brand.brandName}\n"
        f"Slogan: {brand.slogan}\n"
        f"Tone: {brand.toneOfVoice}\n"
        f"Target audience: {brand.targetAudience}\n"
        f"Key messages: {', '.join(brand.keyMessages[:5])}\n"
        f"Prohibited claims: {', '.join(brand.prohibitedClaims[:5])}"
    )

    product_summary = ""
    for p in products[:3]:
        product_summary += (
            f"- {p.productName}: {p.ply}-ply, {p.sheetCount} sheets, "
            f"pack of {p.packSize}, carton of {p.cartonSize}. "
            f"Benefits: {', '.join(p.keyBenefits[:3])}\n"
        )

    system = f"""You are a content calendar strategist for a Thai FMCG brand selling facial tissue.
Your job is to plan a diverse, strategic content calendar.

Available channels: {', '.join(_CHANNELS)}
Available content types: {', '.join(_CONTENT_TYPES)}

Brand context:
{brand_summary}

Product context:
{product_summary if product_summary else 'Facial tissue, 2-ply, 180 sheets, soft, low-dust positioning.'}

Rules:
- Vary channels across the calendar (don't repeat the same channel more than 2 days in a row)
- Mix content types (product showcase, testimonial, lifestyle, promo, educational)
- Use claim-safe Thai language angles (e.g. "ฝุ่นน้อย", "เนียนนุ่ม", "คุ้มค่า")
- Avoid prohibited claims: {', '.join(brand.prohibitedClaims[:5]) or 'dust-free 100%, antibacterial, safest'}
- Tailor hook and CTA direction to the specific channel and audience
- Each suggestion must have: plannedDate (YYYY-MM-DD), channel, contentType, contentAngle, targetAudience, hookDirection, ctaDirection

Respond ONLY with valid JSON:
{{
  "suggestions": [
    {{
      "plannedDate": "YYYY-MM-DD",
      "channel": "tiktok|shopee|lazada|facebook|reseller",
      "contentType": "TIKTOK_CAPTION|TIKTOK_SCRIPT|SHOPEE_DESCRIPTION|LAZADA_DESCRIPTION|FACEBOOK_POST|RESELLER_POST",
      "contentAngle": "short Thai/English angle description",
      "targetAudience": "specific audience segment",
      "hookDirection": "how to open/hook the reader",
      "ctaDirection": "what action to drive"
    }}
  ]
}}"""

    user = (
        f"Create a content calendar plan.\n"
        f"Period: {request.periodStart} to {request.periodEnd}\n"
        f"Campaign objective: {request.objective or 'Increase brand awareness and drive sales'}\n"
        f"Number of items to generate: {request.numItems}\n\n"
        f"Generate exactly {request.numItems} diverse content items spread across the period."
    )

    raw = await _call_llm(system, user)
    result = _parse_json(raw)

    suggestions_raw = result.get("suggestions", [])
    suggestions: list[CalendarItemSuggestion] = []

    for s in suggestions_raw:
        try:
            suggestions.append(CalendarItemSuggestion(
                plannedDate=s.get("plannedDate", request.periodStart),
                channel=s.get("channel", "tiktok").lower(),
                contentType=s.get("contentType", "TIKTOK_CAPTION"),
                contentAngle=s.get("contentAngle", ""),
                targetAudience=s.get("targetAudience", brand.targetAudience),
                hookDirection=s.get("hookDirection", ""),
                ctaDirection=s.get("ctaDirection", ""),
            ))
        except Exception as e:
            logger.warning("Skipping invalid suggestion: %s — %s", s, e)

    logger.info("Calendar planner generated %d suggestions for %s–%s",
                len(suggestions), request.periodStart, request.periodEnd)
    return suggestions
