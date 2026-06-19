# agento-worker

AI agent runtime for Agento.

**Tech:** Python 3.11+ + FastAPI + LangGraph + Pydantic

## Responsibilities

- Receive task dispatch from agento-api
- Run LangGraph multi-step workflows for content generation
- Call LLM via provider abstraction (never hard-coded to one vendor)
- Perform claim-safety checks on generated output
- POST structured JSON results back to agento-api callback
- All generated content returned as structured JSON matching content-schemas

## Status

TODO — App scaffold created in Phase 1.

## Development

```bash
python -m venv .venv
source .venv/bin/activate    # Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env         # add LLM API key
uvicorn main:app --reload --port 8001
# Docs: http://localhost:8001/docs
```

## Structure (planned)

```
agento-worker/
  main.py                  # FastAPI app entrypoint
  api/
    tasks.py               # POST /tasks/run, GET /tasks/{id}
  workflows/
    content_generation.py  # LangGraph graph definition
  providers/
    base.py                # LLM provider interface
    anthropic_provider.py
    openai_provider.py
  schemas/
    task.py                # Pydantic models
    content.py
  claim_safety/
    checker.py             # Keyword + rule-based claim filter
  requirements.txt
  .env.example
```

## LLM Provider Abstraction

The worker never imports `anthropic` or `openai` directly in workflow code.
Workflows call `provider.generate(prompt, schema)` and get back validated JSON.
Provider is selected from `AI_PROVIDER` env var at startup.
