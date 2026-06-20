"""FastAPI router for workflow endpoints."""

import logging

from fastapi import APIRouter, BackgroundTasks, Depends

from app import cancellation
from app.callback import notify_complete, notify_fail
from app.models import FinalContent, WorkflowRequest, WorkflowResponse
from app.security import require_api_key
from app.workflow.graph import workflow_graph
from app.workflow.state import WorkflowState

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/workflows", tags=["workflows"])


@router.post(
    "/content-generation",
    response_model=WorkflowResponse,
    status_code=202,
    dependencies=[Depends(require_api_key)],
)
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


@router.post(
    "/{workflow_id}/cancel",
    dependencies=[Depends(require_api_key)],
)
async def cancel_workflow(workflow_id: str) -> dict:
    """Signal the worker to stop executing a workflow.

    The worker checks this flag at the start of each agent node.
    Any already-running LLM call will complete, but no new steps will start.
    """
    cancellation.cancel(workflow_id)
    logger.info("Cancellation requested for workflow %s", workflow_id)
    return {"workflowId": workflow_id, "status": "cancel_requested"}


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
        # RAG context (populated by retrieval node)
        "retrieved_knowledge": None,
        # Outputs (populated by agent nodes)
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

    final_state: WorkflowState | None = None
    try:
        final_state = await workflow_graph.ainvoke(initial_state)

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
        # Use final_state.current_step if available — it reflects the actual last step
        current_step = final_state.get("current_step") if final_state else None
        logger.error("Workflow %s failed: %s", wf_id, exc, exc_info=True)
        await notify_fail(base_url, api_key, wf_id, str(exc), current_step)

    finally:
        cancellation.cleanup(wf_id)
