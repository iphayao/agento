"""agento-worker — FastAPI entrypoint for LangGraph agent workflows."""

import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.routers.workflows import router as workflows_router
from app.routers.performance import router as performance_router

logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s [%(levelname)s] %(name)s — %(message)s",
)

app = FastAPI(
    title="agento-worker",
    description="LangGraph agentic workflow executor for Agento content generation",
    version="2.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(workflows_router)
app.include_router(performance_router)


@app.get("/health")
async def health() -> dict:
    return {
        "status": "ok",
        "service": "agento-worker",
        "ai_provider": settings.ai_provider,
        "ai_model": settings.ai_model,
    }
