CREATE TYPE character_type AS ENUM ('DND14', 'DND24', 'VTM5');

CREATE TABLE share_links
(
    token_hash     VARCHAR(64) PRIMARY KEY,
    character_type character_type NOT NULL,
    character_id   BIGINT         NOT NULL,
    revoked        BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    revoked_at     TIMESTAMPTZ
);

CREATE INDEX idx_share_links_character
    ON share_links (character_type, character_id, revoked);

CREATE INDEX idx_share_links_active
    ON share_links (token_hash)
    WHERE revoked = FALSE;
