CREATE TABLE release_notes (
    id              UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    version         VARCHAR(50),
    raw_commits     TEXT NOT NULL,
    generated_markdown TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
