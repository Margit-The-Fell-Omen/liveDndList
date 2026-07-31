CREATE TABLE dnd_feats
(
    id               BIGSERIAL PRIMARY KEY,
    key              VARCHAR(255) NOT NULL UNIQUE,
    name             VARCHAR(255) NOT NULL,
    "desc"           TEXT,
    type             VARCHAR(255),
    has_prerequisite BOOLEAN      NOT NULL DEFAULT FALSE,
    prerequisite     VARCHAR(255),
    benefits         TEXT[],
    document_id      BIGINT,

    CONSTRAINT fk_dnd_feats_document
        FOREIGN KEY (document_id) REFERENCES documents (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_dnd_feats_type ON dnd_feats (type);
CREATE INDEX idx_dnd_feats_document ON dnd_feats (document_id);