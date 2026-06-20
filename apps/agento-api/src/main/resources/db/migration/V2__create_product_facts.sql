CREATE TABLE product_facts (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100),
    sheet_count INTEGER,
    ply INTEGER,
    pack_size INTEGER,
    carton_size INTEGER,
    key_benefits TEXT,
    proof_points TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
