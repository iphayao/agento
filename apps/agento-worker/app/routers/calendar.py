"""FastAPI router for calendar planning endpoint."""

import logging

from fastapi import APIRouter, Depends

from app.calendar.models import CalendarPlanRequest, CalendarPlanResponse
from app.calendar.planner import generate_plan
from app.security import require_api_key

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/calendar", tags=["calendar"])


@router.post(
    "/plan",
    response_model=CalendarPlanResponse,
    dependencies=[Depends(require_api_key)],
)
async def plan_calendar(request: CalendarPlanRequest) -> CalendarPlanResponse:
    """Generate content calendar suggestions for a date range.

    Synchronous: waits for the LLM to return all suggestions before responding.
    Spring Boot calls this and saves the suggestions as CalendarItems.
    """
    logger.info(
        "Calendar plan request: calendarId=%s, period=%s–%s, numItems=%d",
        request.calendarId, request.periodStart, request.periodEnd, request.numItems,
    )

    suggestions = await generate_plan(request)

    return CalendarPlanResponse(
        calendarId=request.calendarId,
        suggestions=suggestions,
        message=f"Generated {len(suggestions)} calendar item suggestions",
    )
