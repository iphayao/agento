"""agento-worker — FastAPI entrypoint for LangGraph agent workflows."""

import logging
import sys

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.middleware import CorrelationIdFilter, CorrelationIdMiddleware
from app.routers.workflows import router as workflows_router
from app.routers.performance import router as performance_router
from app.routers.calendar import router as calendar_router

# ── Structured logging setup ──────────────────────────────────────────────────
_log_level = getattr(logging, settings.log_level.upper(), logging.INFO)

handler = logging.StreamHandler(sys.stdout)
handler.setFormatter(logging.Formatter(
    "%(asctime)s [%(levelname)s] [%(correlation_id)s] %(name)s — %(message)s"
))
handler.addFilter(CorrelationIdFilter())

logging.basicConfig(level=_log_level, handlers=[handler])
logging.getLogger("uvicorn.access").handlers = [handler]

logger = logging.getLogger(__name__)
logger.info("agento-worker starting up (provider=%s model=%s)",
            settings.ai_provider, settings.ai_model)

# ── FastAPI app ───────────────────────────────────────────────────────────────
app = FastAPI(
    title="agento-worker",
    description="LangGraph agentic workflow executor for Agento content generation",
    version="7.0.0",
)

app.add_middleware(CorrelationIdMiddleware)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(workflows_router)
app.include_router(performance_router)
app.include_router(calendar_router)


@app.get("/health")
async def health() -> dict:
    return {
        "status": "ok",
        "service": "agento-worker",
        "ai_provider": settings.ai_provider,
        "ai_model": settings.ai_model,
    }
