"""FastAPI router for workflow endpoints."""

import asyncio
import logging

from fastapi import APIRouter, BackgroundTasks, HTTPException

from app.callback import notify_complete, notify_fail
from app.models import FinalContent, WorkflowRequest, WorkflowResponse
from app.workflow.graph import workflow_graph
from app.workflow.state import WorkflowState

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/workflows", tags=["workflows"])


@router.post("/content-generation", response_model=WorkflowResponse, status_code=202)
async def start_content_generation(
    request: WorkflowRequest,
    background_tasks: BackgroundTasks,
) -> WorkflowResponse:
    """Accept a content-generation workflow and run it in the background.

    Returns immediately with 202 Accepted. The worker will POST step updates
    and a final result back to the Spring Boot callback URL.
    """
    background_tasks.add_task(_run_workflow, request)
    return WorkflowResponse(
        workflowId=request.workflowId,
        message="Workflow accepted and queued for execution",
    )


async def _run_workflow(request: WorkflowRequest) -> None:
    """Execute the LangGraph workflow and send callbacks to Spring Boot."""
    wf_id = request.workflowId
    base_url = request.callbackBaseUrl
    api_key = request.callbackApiKey

    logger.info("Starting content-generation workflow %s", wf_id)

    initial_state: WorkflowState = {
        "workflow_id": wf_id,
        "campaign_id": request.campaignId,
        "brand": request.brand.model_dump(),
        "products": [p.model_dump() for p in request.products],
        "campaign": request.campaign.model_dump(),
        "callback_base_url": base_url,
        "callback_api_key": api_key,
        # Outputs (populated by nodes)
        "strategy_brief": None,
        "customer_insights": None,
        "content_draft": None,
        "compliance_result": None,
        "edited_content": None,
        "final_output": None,
        # Tracking
        "compliance_warnings": [],
        "errors": [],
        "current_step": None,
    }

    try:
        final_state: WorkflowState = await workflow_graph.ainvoke(initial_state)

        final_output = final_state.get("final_output")
        if not final_output or not final_output.get("body"):
            raise ValueError("Workflow completed but produced no content body")

        final_content = FinalContent(
            title=final_output.get("title", ""),
            hook=final_output.get("hook", ""),
            body=final_output.get("body", ""),
            callToAction=final_output.get("callToAction", ""),
            hashtags=final_output.get("hashtags", []),
            complianceNotes=final_output.get("complianceNotes", ""),
        )
        warnings = final_state.get("compliance_warnings", [])

        await notify_complete(base_url, api_key, wf_id, final_content, warnings)
        logger.info("Workflow %s completed successfully", wf_id)

    except Exception as exc:
        current_step = initial_state.get("current_step")
        logger.error("Workflow %s failed: %s", wf_id, exc, exc_info=True)
        await notify_fail(base_url, api_key, wf_id, str(exc), current_step)
