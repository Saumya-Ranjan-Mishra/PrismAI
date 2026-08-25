CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS query_embedding (
    id         BIGSERIAL PRIMARY KEY,
    query      TEXT NOT NULL UNIQUE,
    embedding  vector(384) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS query_metadata (
    id BIGSERIAL PRIMARY KEY,
    user_query_intent TEXT,
    served_from TEXT,
    token_count BIGINT,
    query_embedding_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_query_metadata_embedding
        FOREIGN KEY (query_embedding_id) REFERENCES query_embedding(id)
);

CREATE INDEX IF NOT EXISTS idx_query_embedding_cosine
    ON query_embedding USING hnsw (embedding vector_cosine_ops);
