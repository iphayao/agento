"""Tests for the Performance Analyst agent nodes.

Uses mock LLM responses to verify node logic without real API calls.
"""

import json
from unittest.mock import AsyncMock, patch

import pytest

from app.performance.analyst import (
    hook_analyzer_node,
    angle_analyzer_node,
    cta_analyzer_node,
    channel_analyzer_node,
    insight_builder_node,
    summary_node,
)
from app.performance.models import AnalysisState


def make_state(**kwargs) -> AnalysisState:
    base: AnalysisState = {
        "records": [],
        "channel": "all",
        "callback_base_url": "http://localhost:8080/api",
        "callback_api_key": "",
        "top_hooks": [],
        "weak_hooks": [],
        "top_angles": [],
        "weak_angles": [],
        "top_ctas": [],
        "weak_ctas": [],
        "channel_signals": [],
        "insights": [],
        "summary": None,
        "errors": [],
    }
    base.update(kwargs)
    return base


def make_record(**kwargs) -> dict:
    base = {
        "id": "uuid-1",
        "channel": "tiktok",
        "hook": "Test hook",
        "callToAction": "Buy now",
        "title": "Test title",
        "engagementRate": 0.15,
        "conversionRate": 0.06,
        "revenue": 1500.0,
        "roas": 3.0,
        "orders": 10,
    }
    base.update(kwargs)
    return base


# ─── hook_analyzer_node ──────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_hook_analyzer_empty_records():
    state = make_state(records=[])
    result = await hook_analyzer_node(state)
    assert result["top_hooks"] == []
    assert result["weak_hooks"] == []


@pytest.mark.asyncio
async def test_hook_analyzer_parses_llm_response():
    records = [
        make_record(hook="Hook A", engagementRate=0.20),
        make_record(hook="Hook B", engagementRate=0.05),
        make_record(hook="Hook C", engagementRate=0.15),
        make_record(hook="Hook D", engagementRate=0.10),
        make_record(hook="Hook E", engagementRate=0.01),
    ]
    llm_response = json.dumps({
        "winningHooks": ["Hooks that ask questions", "Hooks with numbers"],
        "weakHooks": ["Generic hooks without specifics"],
    })
    state = make_state(records=records)
    with patch(
        "app.performance.analyst._call_llm", new=AsyncMock(return_value=llm_response)
    ):
        result = await hook_analyzer_node(state)
    assert "Hooks that ask questions" in result["top_hooks"]
    assert "Generic hooks without specifics" in result["weak_hooks"]


@pytest.mark.asyncio
async def test_hook_analyzer_llm_failure_adds_error():
    records = [make_record()]
    state = make_state(records=records)
    with patch(
        "app.performance.analyst._call_llm", new=AsyncMock(side_effect=Exception("LLM down"))
    ):
        result = await hook_analyzer_node(state)
    assert any("hook_analyzer" in e for e in result["errors"])


# ─── angle_analyzer_node ─────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_angle_analyzer_identifies_zero_order_content():
    records = [
        make_record(revenue=5000, orders=20),
        make_record(revenue=0, orders=0),
        make_record(revenue=3000, orders=15),
    ]
    llm_response = json.dumps({
        "winningAngles": ["Value angle drives revenue"],
        "weakAngles": ["Generic brand angle underperforms"],
    })
    state = make_state(records=records)
    with patch(
        "app.performance.analyst._call_llm", new=AsyncMock(return_value=llm_response)
    ):
        result = await angle_analyzer_node(state)
    assert len(result["top_angles"]) > 0
    assert len(result["weak_angles"]) > 0


# ─── cta_analyzer_node ───────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_cta_analyzer_parses_strong_and_weak():
    records = [make_record(callToAction=f"CTA {i}", conversionRate=0.1 - i * 0.01)
               for i in range(6)]
    llm_response = json.dumps({
        "strongCTAs": ["Urgency-based CTAs", "Price-anchored CTAs"],
        "weakCTAs": ["Vague CTAs like 'click here'"],
    })
    state = make_state(records=records)
    with patch(
        "app.performance.analyst._call_llm", new=AsyncMock(return_value=llm_response)
    ):
        result = await cta_analyzer_node(state)
    assert "Urgency-based CTAs" in result["top_ctas"]
    assert "Vague CTAs like 'click here'" in result["weak_ctas"]


# ─── channel_analyzer_node ───────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_channel_analyzer_aggregates_by_channel():
    records = [
        make_record(channel="tiktok", engagementRate=0.20, revenue=2000),
        make_record(channel="tiktok", engagementRate=0.18, revenue=1800),
        make_record(channel="shopee", engagementRate=0.05, revenue=500),
    ]
    llm_response = json.dumps({
        "channelSignals": ["TikTok drives 3x higher engagement"],
        "channelRecommendations": ["Increase TikTok budget"],
    })
    state = make_state(records=records)
    with patch(
        "app.performance.analyst._call_llm", new=AsyncMock(return_value=llm_response)
    ):
        result = await channel_analyzer_node(state)
    assert len(result["channel_signals"]) == 2


# ─── insight_builder_node ────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_insight_builder_assembles_all_types():
    state = make_state(
        top_hooks=["Hook A"],
        weak_hooks=["Hook Z"],
        top_angles=["Angle 1"],
        weak_angles=["Angle 2"],
        top_ctas=["CTA X"],
        weak_ctas=["CTA Y"],
        channel_signals=["TikTok wins"],
    )
    result = await insight_builder_node(state)
    insights = result["insights"]
    types = {i["insightType"] for i in insights}
    assert "WINNING_HOOK" in types
    assert "WINNING_ANGLE" in types
    assert "LOW_PERFORMING_ANGLE" in types
    assert "STRONG_CTA" in types
    assert "WEAK_CTA" in types
    assert "CHANNEL_SIGNAL" in types


@pytest.mark.asyncio
async def test_insight_builder_empty_state_produces_no_insights():
    state = make_state()
    result = await insight_builder_node(state)
    assert result["insights"] == []


# ─── summary_node ─────────────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_summary_node_builds_summary():
    llm_response = json.dumps({
        "summaryText": "Overall TikTok performed best this month.",
        "recommendedAngles": ["Value angle"],
        "recommendedHooks": ["Question hook"],
        "recommendedCTAs": ["Urgency CTA"],
        "avoidPatterns": ["Vague hooks"],
    })
    state = make_state(
        top_hooks=["Question hooks"],
        top_angles=["Value angle"],
        top_ctas=["Urgency CTA"],
    )
    with patch(
        "app.performance.analyst._call_llm", new=AsyncMock(return_value=llm_response)
    ):
        result = await summary_node(state)
    assert result["summary"] is not None
    assert "TikTok" in result["summary"]["summaryText"]
    assert result["summary"]["recommendedAngles"] == ["Value angle"]


@pytest.mark.asyncio
async def test_summary_node_llm_failure_adds_error():
    state = make_state()
    with patch(
        "app.performance.analyst._call_llm", new=AsyncMock(side_effect=Exception("timeout"))
    ):
        result = await summary_node(state)
    assert any("summary_node" in e for e in result["errors"])
