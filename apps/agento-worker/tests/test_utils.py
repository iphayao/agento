"""Tests for JSON parsing and cancellation utilities."""

import pytest

from app.workflow.agents import _parse_json
from app import cancellation


# ─── _parse_json ──────────────────────────────────────────────────────────────

def test_parses_plain_json_object():
    result = _parse_json('{"key": "value", "number": 42}')
    assert result == {"key": "value", "number": 42}


def test_parses_json_with_json_markdown_fence():
    raw = '```json\n{"hook": "เนียนนุ่ม", "body": "ฝุ่นน้อย"}\n```'
    result = _parse_json(raw)
    assert result["hook"] == "เนียนนุ่ม"
    assert result["body"] == "ฝุ่นน้อย"


def test_parses_json_with_bare_markdown_fence():
    raw = '```\n{"key": "value"}\n```'
    result = _parse_json(raw)
    assert result == {"key": "value"}


def test_parses_thai_content_correctly():
    raw = '{"title": "ทิชชู่ SoClean", "hashtags": ["#SoClean", "#ทิชชู่SoClean"]}'
    result = _parse_json(raw)
    assert result["title"] == "ทิชชู่ SoClean"
    assert "#SoClean" in result["hashtags"]


def test_raises_on_invalid_json():
    with pytest.raises(Exception):
        _parse_json("this is not json at all")


def test_raises_on_empty_string():
    with pytest.raises(Exception):
        _parse_json("")


def test_parses_nested_object():
    raw = '{"outer": {"inner": "value"}, "list": [1, 2, 3]}'
    result = _parse_json(raw)
    assert result["outer"]["inner"] == "value"
    assert result["list"] == [1, 2, 3]


# ─── cancellation ─────────────────────────────────────────────────────────────

def test_is_cancelled_returns_false_initially():
    wf_id = "test-wf-not-cancelled"
    assert not cancellation.is_cancelled(wf_id)


def test_cancel_marks_workflow_as_cancelled():
    wf_id = "test-wf-cancel-001"
    cancellation.cancel(wf_id)
    assert cancellation.is_cancelled(wf_id)
    cancellation.cleanup(wf_id)  # restore state


def test_cleanup_removes_cancelled_flag():
    wf_id = "test-wf-cleanup-001"
    cancellation.cancel(wf_id)
    cancellation.cleanup(wf_id)
    assert not cancellation.is_cancelled(wf_id)


def test_cleanup_on_uncancelled_workflow_is_safe():
    cancellation.cleanup("does-not-exist")  # must not raise
