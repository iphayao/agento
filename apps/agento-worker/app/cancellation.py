"""In-memory cancellation state for active workflows.

A workflow ID added here causes agent nodes to skip execution and return SKIPPED.
The set is cleaned up in _run_workflow's finally block.
"""

_cancelled: set[str] = set()


def cancel(workflow_id: str) -> None:
    _cancelled.add(workflow_id)


def is_cancelled(workflow_id: str) -> bool:
    return workflow_id in _cancelled


def cleanup(workflow_id: str) -> None:
    _cancelled.discard(workflow_id)
