# Prompt 03 — Brand Memory and RAG

```text
You are an expert AI engineer specializing in RAG systems and marketing automation.

Read CLAUDE.md first.
Review the existing implementation before editing.

Implement only Phase 3 of Agento: Brand Memory and RAG.

Goal:
Add brand memory and retrieval-augmented generation so AI can use:
1. SoClean brand voice
2. Product facts
3. Approved claims
4. Prohibited claims
5. Customer reviews
6. Winning content examples
7. Competitor notes
8. Past approved content

Technology:
- PostgreSQL
- pgvector
- Spring Boot
- Python agento-worker
- Embedding provider abstraction
- LangGraph workflow enhancement

New entities:
1. KnowledgeDocument
   - id
   - title
   - type: BRAND_GUIDELINE, PRODUCT_FACT, APPROVED_CLAIM, PROHIBITED_CLAIM, CUSTOMER_REVIEW, WINNING_CONTENT, COMPETITOR_NOTE, MARKET_INSIGHT
   - content
   - source
   - tags
   - status
   - createdAt
   - updatedAt

2. KnowledgeChunk
   - id
   - documentId
   - chunkText
   - embedding
   - metadata
   - createdAt

Backend requirements:
1. Enable pgvector in PostgreSQL migration.
2. Add KnowledgeDocument CRUD APIs.
3. Add API to upload or paste knowledge content.
4. Add chunking service.
5. Add embedding service abstraction.
6. Store embeddings in pgvector.
7. Add semantic search API: POST /api/knowledge/search
8. Search supports query, document type filter, topK, and score threshold.

Agento-worker requirements:
1. Add Retrieval Node before Brand Strategist Agent.
2. Retrieve relevant brand memory based on campaign context.
3. Inject retrieved context into each agent step.
4. Compliance Agent retrieves approved/prohibited claims.
5. Content Writer retrieves winning content examples.
6. Editor Agent retrieves brand voice guidelines.

Frontend requirements:
Add Knowledge Base section:
1. List documents
2. Create document
3. Edit document
4. Delete document
5. Upload/paste customer reviews
6. Add approved/prohibited claims
7. View generated chunks
8. Test semantic search

Acceptance criteria:
1. User can add brand knowledge.
2. System chunks and embeds knowledge.
3. User can semantic search knowledge.
4. Agent workflow uses retrieved knowledge.
5. Generated content reflects brand voice and approved claims.
6. Compliance warnings improve using prohibited claim memory.
7. Past approved content can influence future generation.

Before editing:
1. Explain task goal.
2. List files to create or modify.
3. Confirm architecture rules.
4. Produce implementation plan.

After editing:
1. Summarize changes.
2. List tests added.
3. Explain how to test semantic search.
4. Recommend running the review prompt next.
```
