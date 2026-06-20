"""Async HTTP helpers for calling back to the Spring Boot API."""

import json
import logging
import httpx
from app.models import (
    StepCallbackPayload,
    CompleteCallbackPayload,
    FailCallbackPayload,
    FinalContent,
)

logger = logging.getLogger(__name__)


def _headers(api_key: str) -> dict:
    h = {"Content-Type": "application/json"}
    if api_key:
        h["X-Api-Key"] = api_key
    return h


async def notify_step(
    base_url: str,
    api_key: str,
    workflow_id: str,
    step_name: str,
    status: str,
    output: str | None = None,
    error: str | None = None,
    input_payload: str | None = None,
) -> None:
    url = f"{base_url}/agent-workflows/{workflow_id}/step-callback"
    payload = StepCallbackPayload(
        stepName=step_name,
        status=status,
        inputPayload=input_payload,
        outputPayload=output,
        errorMessage=error,
    )
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            r = await client.post(url, json=payload.model_dump(), headers=_headers(api_key))
            if r.status_code != 200:
                logger.warning("Step callback returned %d for step %s", r.status_code, step_name)
    except Exception as exc:
        logger.warning("Step callback failed for step %s: %s", step_name, exc)


async def notify_complete(
    base_url: str,
    api_key: str,
    workflow_id: str,
    final_content: FinalContent,
    compliance_warnings: list[str],
) -> None:
    url = f"{base_url}/agent-workflows/{workflow_id}/complete"
    payload = CompleteCallbackPayload(
        finalContent=final_content,
        complianceWarnings=compliance_warnings,
    )
    try:
        async with httpx.AsyncClient(timeout=15.0) as client:
            r = await client.post(url, json=payload.model_dump(), headers=_headers(api_key))
            if r.status_code != 200:
                logger.error("Complete callback returned %d", r.status_code)
    except Exception as exc:
        logger.error("Complete callback failed: %s", exc)


async def notify_fail(
    base_url: str,
    api_key: str,
    workflow_id: str,
    error_message: str,
    failed_step: str | None,
) -> None:
    url = f"{base_url}/agent-workflows/{workflow_id}/fail"
    payload = FailCallbackPayload(errorMessage=error_message, failedStep=failed_step)
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            r = await client.post(url, json=payload.model_dump(), headers=_headers(api_key))
            if r.status_code != 200:
                logger.error("Fail callback returned %d", r.status_code)
    except Exception as exc:
        logger.error("Fail callback failed: %s", exc)
