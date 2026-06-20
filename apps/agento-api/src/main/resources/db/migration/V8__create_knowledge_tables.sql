-- Phase 3: Knowledge Base tables for brand memory and RAG

CREATE TABLE knowledge_documents (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(500) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    content     TEXT         NOT NULL,
    source      VARCHAR(500),
    tags        TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_knowledge_doc_type CHECK (
        type IN ('BRAND_GUIDELINE','PRODUCT_FACT','APPROVED_CLAIM','PROHIBITED_CLAIM',
                 'CUSTOMER_REVIEW','WINNING_CONTENT','COMPETITOR_NOTE','MARKET_INSIGHT')
    ),
    CONSTRAINT chk_knowledge_doc_status CHECK (status IN ('ACTIVE','ARCHIVED'))
);

CREATE INDEX idx_knowledge_documents_type   ON knowledge_documents(type);
CREATE INDEX idx_knowledge_documents_status ON knowledge_documents(status);

-- Chunks store the embedding as the pgvector bracket literal "[0.1,0.2,...]" in a TEXT column.
-- Native SQL casts this to vector for cosine-distance search: embedding::vector <=> query::vector
CREATE TABLE knowledge_chunks (
    id           UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id  UUID    NOT NULL REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    chunk_index  INTEGER NOT NULL DEFAULT 0,
    chunk_text   TEXT    NOT NULL,
    embedding    TEXT,
    metadata     TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_knowledge_chunks_document_id ON knowledge_chunks(document_id);
