from typing import TypedDict, Optional
from app.models import BrandProfile, ProductFact, CampaignInfo, FinalContent


class WorkflowState(TypedDict):
    # Input
    workflow_id: str
    campaign_id: str
    brand: dict
    products: list[dict]
    campaign: dict
    callback_base_url: str
    callback_api_key: str

    # Step outputs (accumulated through the graph)
    strategy_brief: Optional[str]
    customer_insights: Optional[str]
    content_draft: Optional[dict]
    compliance_result: Optional[dict]
    edited_content: Optional[dict]
    final_output: Optional[dict]

    # Tracking
    compliance_warnings: list[str]
    errors: list[str]
    current_step: Optional[str]
