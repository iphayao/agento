"""LangGraph StateGraph for content generation workflow.

6-step pipeline: Brand Strategist → Customer Insight → Content Writer
               → Claim Compliance → Editor → Final Formatter
"""

import logging
from langgraph.graph import StateGraph, END

from app.workflow.state import WorkflowState
from app.workflow.agents import (
    brand_strategist_node,
    customer_insight_node,
    content_writer_node,
    claim_compliance_node,
    editor_node,
    final_formatter_node,
)

logger = logging.getLogger(__name__)


def build_workflow_graph() -> StateGraph:
    graph = StateGraph(WorkflowState)

    graph.add_node("brand_strategist", brand_strategist_node)
    graph.add_node("customer_insight", customer_insight_node)
    graph.add_node("content_writer", content_writer_node)
    graph.add_node("claim_compliance", claim_compliance_node)
    graph.add_node("editor", editor_node)
    graph.add_node("final_formatter", final_formatter_node)

    graph.set_entry_point("brand_strategist")
    graph.add_edge("brand_strategist", "customer_insight")
    graph.add_edge("customer_insight", "content_writer")
    graph.add_edge("content_writer", "claim_compliance")
    graph.add_edge("claim_compliance", "editor")
    graph.add_edge("editor", "final_formatter")
    graph.add_edge("final_formatter", END)

    return graph.compile()


# Module-level compiled graph (singleton)
workflow_graph = build_workflow_graph()
