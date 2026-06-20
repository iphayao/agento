"""Performance analysis router — POST /performance/analyze"""

import logging

import httpx
from fastapi import APIRouter, BackgroundTasks, Depends

from app.performance.graph import build_performance_graph
from app.performance.models import (
    AnalysisRequest,
    AnalysisCallbackPayload,
    InsightItem,
    SummaryItem,
)
from app.security import require_api_key

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/performance", tags=["performance"])

_graph = build_performance_graph()


@router.post("/analyze", dependencies=[Depends(require_api_key)])
async def analyze(
    request: AnalysisRequest,
    background_tasks: BackgroundTasks,
):
    """Accept a performance analysis request and execute it in the background."""
    background_tasks.add_task(_run_analysis, request)
    return {"status": "accepted", "recordCount": len(request.records)}


async def _run_analysis(request: AnalysisRequest) -> None:
    logger.info("Starting performance analysis — %d records, channel=%s",
                len(request.records), request.channel)
    try:
        initial_state = {
            "records": [r.model_dump() for r in request.records],
            "channel": request.channel,
            "callback_base_url": request.callbackBaseUrl,
            "callback_api_key": request.callbackApiKey,
            "top_hooks": [],
            "weak_hooks": [],
            "top_angles": [],
            "weak_angles": [],
            "top_ctas": [],
            "weak_ctas": [],
            "channel_signals": [],
            "insights": [],
            "summary": None,
            "errors": [],
        }
        final_state = await _graph.ainvoke(initial_state)
        await _send_callback(
            callback_base_url=request.callbackBaseUrl,
            callback_api_key=request.callbackApiKey,
            insights_raw=final_state.get("insights", []),
            summary_raw=final_state.get("summary"),
        )
    except Exception as e:
        logger.error("Performance analysis pipeline failed: %s", e, exc_info=True)


async def _send_callback(
    callback_base_url: str,
    callback_api_key: str,
    insights_raw: list[dict],
    summary_raw: dict | None,
) -> None:
    insights = [InsightItem(**i) for i in insights_raw]
    summary = SummaryItem(**summary_raw) if summary_raw else None

    payload = AnalysisCallbackPayload(insights=insights, summary=summary)
    url = f"{callback_base_url}/performance/insights/callback"
    headers: dict[str, str] = {"Content-Type": "application/json"}
    if callback_api_key:
        headers["X-Api-Key"] = callback_api_key

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            resp = await client.post(url, json=payload.model_dump(), headers=headers)
            resp.raise_for_status()
        logger.info("Performance callback sent — %d insights", len(insights))
    except Exception as e:
        logger.error("Performance callback to %s failed: %s", url, e)
