CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS query_embedding (
    id         BIGSERIAL PRIMARY KEY,
    query      TEXT NOT NULL UNIQUE,
    embedding  vector(384) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_query_embedding_cosine
    ON query_embedding USING hnsw (embedding vector_cosine_ops);
