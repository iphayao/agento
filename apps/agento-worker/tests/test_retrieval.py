"""Tests for the RAG retrieval module."""

import pytest
import respx
import httpx

from app.retrieval import (
    retrieve_knowledge,
    empty_context,
    format_section,
    _organize_by_type,
)


def test_empty_context_has_all_keys():
    ctx = empty_context()
    assert "brand_guidelines" in ctx
    assert "approved_claims" in ctx
    assert "prohibited_claims" in ctx
    assert "customer_reviews" in ctx
    assert "winning_content" in ctx
    assert "competitor_notes" in ctx
    assert "market_insights" in ctx
    assert "product_facts" in ctx
    assert all(isinstance(v, list) for v in ctx.values())


def test_organize_by_type_groups_results():
    results = [
        {"documentType": "BRAND_GUIDELINE", "chunkText": "Brand voice is warm."},
        {"documentType": "APPROVED_CLAIM", "chunkText": "ฝุ่นน้อย ใช้งานทุกวัน"},
        {"documentType": "CUSTOMER_REVIEW", "chunkText": "นุ่มมาก ชอบมากค่ะ"},
        {"documentType": "UNKNOWN_TYPE", "chunkText": "should be ignored"},
    ]
    ctx = _organize_by_type(results)
    assert ctx["brand_guidelines"] == ["Brand voice is warm."]
    assert ctx["approved_claims"] == ["ฝุ่นน้อย ใช้งานทุกวัน"]
    assert ctx["customer_reviews"] == ["นุ่มมาก ชอบมากค่ะ"]
    # Unknown type is ignored; all other keys remain empty lists
    assert ctx["winning_content"] == []


def test_format_section_empty():
    assert format_section([], "Brand Voice") == ""


def test_format_section_with_items():
    items = ["First chunk.", "Second chunk."]
    result = format_section(items, "Brand Voice")
    assert "Brand Voice" in result
    assert "First chunk." in result
    assert "Second chunk." in result


def test_format_section_respects_max_items():
    items = ["A", "B", "C", "D", "E"]
    result = format_section(items, "Test", max_items=2)
    assert "C" not in result
    assert "A" in result
    assert "B" in result


@pytest.mark.asyncio
@respx.mock
async def test_retrieve_knowledge_success():
    mock_response = {
        "success": True,
        "data": {
            "query": "SoClean brand",
            "results": [
                {
                    "documentType": "BRAND_GUIDELINE",
                    "chunkText": "SoClean brand voice: warm and honest.",
                    "score": 0.92,
                },
                {
                    "documentType": "APPROVED_CLAIM",
                    "chunkText": "ฝุ่นน้อย เนียนนุ่ม",
                    "score": 0.85,
                },
            ],
        },
    }
    respx.post("http://localhost:8080/api/knowledge/search").mock(
        return_value=httpx.Response(200, json=mock_response)
    )

    result = await retrieve_knowledge(
        base_url="http://localhost:8080/api",
        api_key="test-key",
        query="SoClean brand campaign",
    )

    assert result["brand_guidelines"] == ["SoClean brand voice: warm and honest."]
    assert result["approved_claims"] == ["ฝุ่นน้อย เนียนนุ่ม"]
    assert result["customer_reviews"] == []


@pytest.mark.asyncio
@respx.mock
async def test_retrieve_knowledge_returns_empty_on_404():
    respx.post("http://localhost:8080/api/knowledge/search").mock(
        return_value=httpx.Response(404)
    )
    result = await retrieve_knowledge(
        base_url="http://localhost:8080/api",
        api_key="",
        query="test",
    )
    assert result == empty_context()


@pytest.mark.asyncio
@respx.mock
async def test_retrieve_knowledge_returns_empty_on_network_error():
    respx.post("http://localhost:8080/api/knowledge/search").mock(
        side_effect=httpx.ConnectError("connection refused")
    )
    result = await retrieve_knowledge(
        base_url="http://localhost:8080/api",
        api_key="",
        query="test",
    )
    assert result == empty_context()


@pytest.mark.asyncio
@respx.mock
async def test_retrieve_knowledge_returns_empty_on_500():
    respx.post("http://localhost:8080/api/knowledge/search").mock(
        return_value=httpx.Response(500, json={"error": "server error"})
    )
    result = await retrieve_knowledge(
        base_url="http://localhost:8080/api",
        api_key="",
        query="test",
    )
    assert result == empty_context()
