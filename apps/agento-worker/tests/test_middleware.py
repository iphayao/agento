"""Tests for CorrelationIdMiddleware."""

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.middleware import CorrelationIdMiddleware, get_correlation_id, CORRELATION_ID_HEADER


def _build_app() -> FastAPI:
    app = FastAPI()
    app.add_middleware(CorrelationIdMiddleware)

    @app.get("/test")
    async def test_endpoint():
        return {"correlation_id": get_correlation_id()}

    return app


@pytest.fixture
def client():
    return TestClient(_build_app())


def test_generates_correlation_id_when_not_provided(client):
    resp = client.get("/test")
    assert resp.status_code == 200
    corr_id = resp.headers.get(CORRELATION_ID_HEADER)
    assert corr_id is not None and len(corr_id) > 0


def test_propagates_provided_correlation_id(client):
    provided_id = "test-correlation-12345"
    resp = client.get("/test", headers={CORRELATION_ID_HEADER: provided_id})
    assert resp.headers.get(CORRELATION_ID_HEADER) == provided_id
    assert resp.json()["correlation_id"] == provided_id


def test_different_requests_get_different_ids(client):
    resp1 = client.get("/test")
    resp2 = client.get("/test")
    id1 = resp1.headers.get(CORRELATION_ID_HEADER)
    id2 = resp2.headers.get(CORRELATION_ID_HEADER)
    assert id1 != id2


def test_correlation_id_available_in_endpoint(client):
    custom_id = "my-custom-id"
    resp = client.get("/test", headers={CORRELATION_ID_HEADER: custom_id})
    assert resp.json()["correlation_id"] == custom_id
