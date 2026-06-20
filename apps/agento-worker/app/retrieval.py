"""RAG retrieval: fetches relevant knowledge from the Spring Boot knowledge search API.

The retrieval node calls POST /knowledge/search with the campaign context as the query.
Spring Boot handles embedding the query and returning cosine-similar chunks.
Results are organized by document type for injection into each agent node.
"""

import logging
from typing import Optional

import httpx

logger = logging.getLogger(__name__)

RETRIEVAL_TIMEOUT = 15.0

_TYPE_MAP = {
    "BRAND_GUIDELINE": "brand_guidelines",
    "PRODUCT_FACT": "product_facts",
    "APPROVED_CLAIM": "approved_claims",
    "PROHIBITED_CLAIM": "prohibited_claims",
    "CUSTOMER_REVIEW": "customer_reviews",
    "WINNING_CONTENT": "winning_content",
    "COMPETITOR_NOTE": "competitor_notes",
    "MARKET_INSIGHT": "market_insights",
}


async def retrieve_knowledge(
    base_url: str,
    api_key: str,
    query: str,
    top_k: int = 10,
    min_score: float = 0.0,
    doc_type: Optional[str] = None,
) -> dict:
    """Query the knowledge search endpoint and return organized context dict.

    Returns an empty context dict on any error — retrieval failure is non-critical.
    """
    search_url = f"{base_url}/knowledge/search"
    headers = {"Content-Type": "application/json"}
    if api_key:
        headers["X-Api-Key"] = api_key

    payload: dict = {"query": query, "topK": top_k, "minScore": min_score}
    if doc_type:
        payload["documentType"] = doc_type

    try:
        async with httpx.AsyncClient(timeout=RETRIEVAL_TIMEOUT) as client:
            response = await client.post(search_url, json=payload, headers=headers)
            if response.status_code == 404:
                logger.debug("Knowledge search endpoint not found — knowledge base may be empty")
                return empty_context()
            response.raise_for_status()
            data = response.json()
    except httpx.HTTPStatusError as e:
        logger.warning("Knowledge search HTTP error %s: %s", e.response.status_code, e)
        return empty_context()
    except Exception as e:
        logger.warning("Knowledge retrieval failed: %s", e)
        return empty_context()

    results = data.get("data", {}).get("results", [])
    return _organize_by_type(results)


def _organize_by_type(results: list) -> dict:
    ctx = empty_context()
    for item in results:
        key = _TYPE_MAP.get(item.get("documentType", ""))
        if key:
            ctx[key].append(item.get("chunkText", ""))
    return ctx


def empty_context() -> dict:
    return {
        "brand_guidelines": [],
        "product_facts": [],
        "approved_claims": [],
        "prohibited_claims": [],
        "customer_reviews": [],
        "winning_content": [],
        "competitor_notes": [],
        "market_insights": [],
    }


def format_section(items: list[str], label: str, max_items: int = 3) -> str:
    """Format a list of retrieved chunks as a labelled prompt section."""
    if not items:
        return ""
    subset = items[:max_items]
    body = "\n---\n".join(subset)
    return f"\n## {label} (from brand memory):\n{body}\n"
