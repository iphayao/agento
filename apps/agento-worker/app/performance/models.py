from pydantic import BaseModel
from typing import Optional, TypedDict
import datetime


class PerformanceRecord(BaseModel):
    id: str
    channel: str
    hook: str = ""
    callToAction: str = ""
    title: str = ""
    engagementRate: float = 0.0
    conversionRate: float = 0.0
    revenue: float = 0.0
    roas: float = 0.0
    orders: int = 0


class AnalysisRequest(BaseModel):
    records: list[PerformanceRecord]
    channel: str = "all"
    callbackBaseUrl: str
    callbackApiKey: str = ""


class InsightItem(BaseModel):
    generatedContentId: Optional[str] = None
    campaignId: Optional[str] = None
    insightType: str
    insightText: str
    confidenceScore: float = 0.0


class SummaryItem(BaseModel):
    periodStart: str
    periodEnd: str
    channel: Optional[str] = None
    summaryText: str
    recommendedAngles: list[str] = []
    recommendedHooks: list[str] = []
    recommendedCTAs: list[str] = []
    avoidPatterns: list[str] = []


class AnalysisCallbackPayload(BaseModel):
    insights: list[InsightItem] = []
    summary: Optional[SummaryItem] = None


# TypedDict for LangGraph state (nodes receive and return dicts)
class AnalysisState(TypedDict):
    records: list[dict]          # serialized PerformanceRecord dicts
    channel: str
    callback_base_url: str
    callback_api_key: str
    top_hooks: list[str]
    weak_hooks: list[str]
    top_angles: list[str]
    weak_angles: list[str]
    top_ctas: list[str]
    weak_ctas: list[str]
    channel_signals: list[str]
    insights: list[dict]         # serialized InsightItem dicts
    summary: Optional[dict]      # serialized SummaryItem dict
    errors: list[str]
