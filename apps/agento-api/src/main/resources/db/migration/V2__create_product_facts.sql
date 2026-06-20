CREATE TABLE IF NOT EXISTS product_facts (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL,
    sku          VARCHAR(100) UNIQUE,
    sheet_count  INT NOT NULL DEFAULT 180,
    ply          INT NOT NULL DEFAULT 2,
    pack_size    INT NOT NULL DEFAULT 5,
    carton_size  INT NOT NULL DEFAULT 50,
    key_benefits TEXT,
    proof_points TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
