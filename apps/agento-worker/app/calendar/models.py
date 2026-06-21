"""Pydantic models for calendar planning requests and responses."""

from pydantic import BaseModel

from app.models import BrandProfile, ProductFact


class CalendarPlanRequest(BaseModel):
    calendarId: str
    brand: BrandProfile
    products: list[ProductFact] = []
    periodStart: str  # YYYY-MM-DD
    periodEnd: str    # YYYY-MM-DD
    objective: str = ""
    numItems: int = 7


class CalendarItemSuggestion(BaseModel):
    plannedDate: str  # YYYY-MM-DD
    channel: str
    contentType: str
    contentAngle: str
    targetAudience: str
    hookDirection: str
    ctaDirection: str


class CalendarPlanResponse(BaseModel):
    calendarId: str
    suggestions: list[CalendarItemSuggestion]
    message: str
