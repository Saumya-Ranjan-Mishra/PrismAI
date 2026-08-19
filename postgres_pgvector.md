## Build the docker image for postgres with pgvector

```cmd
docker build -f pgvector.dockerfile -t postgres_pgvector:0.1 .
```

## Run the docker image with post forward

```cmd
docker run --name postgres_pgvector_01 -e POSTGRES_PASSWORD=mysecretpassword -d -p 5432:5432 postgres_pgvector:0.1
```

## Test the Similarity search

```SQL
CREATE EXTENSION IF NOT EXISTS vector;

DROP TABLE IF EXISTS items;

CREATE TABLE items (
  id bigserial PRIMARY KEY,
  name text,
  embedding vector(3)
);

INSERT INTO items (name, embedding) VALUES
('apple',  '[1, 0, 0]'),
('banana', '[0.9, 0.1, 0]'),
('car',    '[0, 1, 0]'),
('bus',    '[0, 0.9, 0.1]'),
('cat',    '[0, 0, 1]');

-- Search by L2 distance
SELECT id, name, embedding, embedding <-> '[1, 0, 0]'::vector AS distance
FROM items
ORDER BY embedding <-> '[1, 0, 0]'::vector
LIMIT 3;

-- Seach by cosine distance
SELECT id, name, embedding, embedding <=> '[1, 0, 0]'::vector AS cosine_distance
FROM items
ORDER BY embedding <=> '[1, 0, 0]'::vector
LIMIT 3;

--Search by inner product
-- Note: <#> returns negative inner product, so sort ascending for “most similar”.
SELECT id, name, embedding, embedding <#> '[1, 0, 0]'::vector AS neg_inner_product
FROM items
ORDER BY embedding <#> '[1, 0, 0]'::vector
LIMIT 3;

--creating index (IVFFlat)
CREATE INDEX IF NOT EXISTS items_embedding_cosine_idx
ON items USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 10);

```

## Test with HNSW index

```SQL
-- HNSW index for cosine distance
CREATE INDEX IF NOT EXISTS items_embedding_hnsw_cosine_idx
ON items
USING hnsw (embedding vector_cosine_ops);

-- HNSW index for L2 distance
CREATE INDEX IF NOT EXISTS items_embedding_hnsw_l2_idx
ON items
USING hnsw (embedding vector_l2_ops);

-- HNSW index for inner product
CREATE INDEX IF NOT EXISTS items_embedding_hnsw_ip_idx
ON items
USING hnsw (embedding vector_ip_ops);

-- Optional build-time tuning
CREATE INDEX IF NOT EXISTS items_embedding_hnsw_cosine_tuned_idx
ON items
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 128);

-- Optional query-time tuning (higher = better recall, slower)
SET hnsw.ef_search = 100;

-- Query using cosine distance
SELECT id, name, embedding, embedding <=> '[1, 0, 0]'::vector AS cosine_distance
FROM items
ORDER BY embedding <=> '[1, 0, 0]'::vector
LIMIT 3;

-- Verify plan/index usage
EXPLAIN ANALYZE
SELECT id, name
FROM items
ORDER BY embedding <=> '[1, 0, 0]'::vector
LIMIT 3;
```
