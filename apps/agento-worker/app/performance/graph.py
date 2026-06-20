"""LangGraph pipeline for performance analysis."""

from langgraph.graph import StateGraph, END

from app.performance.analyst import (
    hook_analyzer_node,
    angle_analyzer_node,
    cta_analyzer_node,
    channel_analyzer_node,
    insight_builder_node,
    summary_node,
)
from app.performance.models import AnalysisState


def build_performance_graph():
    builder = StateGraph(AnalysisState)

    builder.add_node("hook_analyzer", hook_analyzer_node)
    builder.add_node("angle_analyzer", angle_analyzer_node)
    builder.add_node("cta_analyzer", cta_analyzer_node)
    builder.add_node("channel_analyzer", channel_analyzer_node)
    builder.add_node("insight_builder", insight_builder_node)
    builder.add_node("summary", summary_node)

    builder.set_entry_point("hook_analyzer")
    builder.add_edge("hook_analyzer", "angle_analyzer")
    builder.add_edge("angle_analyzer", "cta_analyzer")
    builder.add_edge("cta_analyzer", "channel_analyzer")
    builder.add_edge("channel_analyzer", "insight_builder")
    builder.add_edge("insight_builder", "summary")
    builder.add_edge("summary", END)

    return builder.compile()
