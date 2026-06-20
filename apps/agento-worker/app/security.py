"""FastAPI API key validation dependency.

When WORKER_API_KEY is not set the dependency allows all traffic (dev mode).
Set WORKER_API_KEY to a strong secret before exposing the worker to any network.
"""

import logging
from fastapi import HTTPException, Security
from fastapi.security import APIKeyHeader

from app.config import settings

logger = logging.getLogger(__name__)

_api_key_header = APIKeyHeader(name="X-Api-Key", auto_error=False)


async def require_api_key(api_key: str | None = Security(_api_key_header)) -> None:
    if not settings.worker_api_key:
        return  # dev mode — no key configured, allow all
    if api_key != settings.worker_api_key:
        logger.warning("Rejected request with invalid or missing X-Api-Key")
        raise HTTPException(
            status_code=401,
            detail="Unauthorized — valid X-Api-Key header required",
        )
