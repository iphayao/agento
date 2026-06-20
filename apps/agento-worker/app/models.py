from pydantic import BaseModel
from typing import Any, Optional


class BrandProfile(BaseModel):
    brandName: str
    slogan: str = ""
    toneOfVoice: str = ""
    targetAudience: str = ""
    keyMessages: list[str] = []
    prohibitedClaims: list[str] = []


class ProductFact(BaseModel):
    productName: str
    sku: str = ""
    ply: int = 2
    sheetCount: int = 180
    packSize: int = 5
    cartonSize: int = 50
    keyBenefits: list[str] = []
    proofPoints: list[str] = []


class CampaignInfo(BaseModel):
    id: str
    name: str
    objective: str = ""
    channel: str = ""
    targetAudience: str = ""
    contentAngle: str = ""


class WorkflowRequest(BaseModel):
    workflowId: str
    campaignId: str
    callbackBaseUrl: str
    callbackApiKey: str = ""
    brand: BrandProfile
    products: list[ProductFact] = []
    campaign: CampaignInfo


class WorkflowResponse(BaseModel):
    workflowId: str
    message: str


class FinalContent(BaseModel):
    title: str
    hook: str
    body: str
    callToAction: str
    hashtags: list[str] = []
    complianceNotes: str = ""


class StepCallbackPayload(BaseModel):
    stepName: str
    status: str  # RUNNING | COMPLETED | FAILED
    inputPayload: Optional[str] = None
    outputPayload: Optional[str] = None
    errorMessage: Optional[str] = None


class CompleteCallbackPayload(BaseModel):
    finalContent: FinalContent
    complianceWarnings: list[str] = []


class FailCallbackPayload(BaseModel):
    errorMessage: str
    failedStep: Optional[str] = None
