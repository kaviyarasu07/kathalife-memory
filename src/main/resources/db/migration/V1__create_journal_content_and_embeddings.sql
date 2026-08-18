CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE journal_entry_content (
    journal_entry_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    full_text TEXT NOT NULL,
    activity_date DATE NOT NULL,
    language VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE journal_embeddings (
    embedding_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id VARCHAR(36) NOT NULL REFERENCES journal_entry_content(journal_entry_id) ON DELETE CASCADE,
    chunk_text TEXT NOT NULL,
    chunk_sequence INTEGER NOT NULL,
    embedding_vector vector(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_journal_embeddings_entry_id ON journal_embeddings(journal_entry_id);

CREATE INDEX idx_journal_embeddings_vector_hnsw
    ON journal_embeddings
    USING hnsw (embedding_vector vector_cosine_ops);