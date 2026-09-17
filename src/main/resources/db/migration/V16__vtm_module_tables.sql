-- ============================================================
-- Enum types
-- ============================================================

CREATE TYPE chronicle_status_type AS ENUM (
    'PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED'
    );

CREATE TYPE discipline_type AS ENUM (
    'PHYSICAL', 'MENTAL', 'SOCIAL'
    );

-- ============================================================
-- VTM catalog tables
-- ============================================================

CREATE TABLE vtm_clans
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    curse       TEXT,
    compulsion  TEXT,
    sourcebook  VARCHAR(255)
);

CREATE TABLE vtm_disciplines
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255)   NOT NULL UNIQUE,
    description     TEXT,
    discipline_type discipline_type NOT NULL,
    sourcebook      VARCHAR(255)
);

CREATE INDEX idx_vtm_disciplines_type ON vtm_disciplines (discipline_type);

CREATE TABLE vtm_powers
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    level         INTEGER      NOT NULL CHECK (level BETWEEN 1 AND 5),
    amalgam       VARCHAR(255),
    prerequisite  TEXT,
    discipline_id BIGINT       NOT NULL REFERENCES vtm_disciplines (id) ON DELETE CASCADE,

    UNIQUE (discipline_id, name)
);

CREATE INDEX idx_vtm_powers_discipline ON vtm_powers (discipline_id);

CREATE TABLE vtm_predator_types
(
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL UNIQUE,
    description       TEXT,
    granted_specialty VARCHAR(255),
    sourcebook        VARCHAR(255)
);

CREATE TABLE vtm_clan_disciplines
(
    clan_id       BIGINT NOT NULL REFERENCES vtm_clans (id) ON DELETE CASCADE,
    discipline_id BIGINT NOT NULL REFERENCES vtm_disciplines (id) ON DELETE CASCADE,

    PRIMARY KEY (clan_id, discipline_id)
);

CREATE INDEX idx_vtm_clan_disciplines_discipline ON vtm_clan_disciplines (discipline_id);

-- ============================================================
-- VTM player-facing tables
-- ============================================================

CREATE TABLE vtm_chronicles
(
    id          BIGSERIAL PRIMARY KEY,
    player_id   BIGINT                NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title       VARCHAR(255)          NOT NULL,
    description TEXT,
    setting     TEXT,
    start_date  DATE,
    status      chronicle_status_type NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vtm_chronicles_player ON vtm_chronicles (player_id);

CREATE TABLE vtm_characters
(
    id               BIGSERIAL PRIMARY KEY,
    chronicle_id     BIGINT       NOT NULL REFERENCES vtm_chronicles (id) ON DELETE CASCADE,
    clan_id          BIGINT       NOT NULL REFERENCES vtm_clans (id) ON DELETE RESTRICT,
    predator_type_id BIGINT       NOT NULL REFERENCES vtm_predator_types (id) ON DELETE RESTRICT,
    name             VARCHAR(255) NOT NULL,
    concept          TEXT,
    ambition         TEXT,
    desire           TEXT,
    generation       INTEGER,
    humanity         INTEGER,
    blood_potency    INTEGER,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vtm_characters_chronicle ON vtm_characters (chronicle_id);
CREATE INDEX idx_vtm_characters_clan ON vtm_characters (clan_id);
CREATE INDEX idx_vtm_characters_predator_type ON vtm_characters (predator_type_id);

CREATE TABLE vtm_character_disciplines
(
    character_id  BIGINT  NOT NULL REFERENCES vtm_characters (id) ON DELETE CASCADE,
    discipline_id BIGINT  NOT NULL REFERENCES vtm_disciplines (id) ON DELETE CASCADE,
    dots          INTEGER NOT NULL DEFAULT 1 CHECK (dots BETWEEN 1 AND 5),

    PRIMARY KEY (character_id, discipline_id)
);

CREATE INDEX idx_vtm_character_disciplines_discipline
    ON vtm_character_disciplines (discipline_id);

CREATE TABLE vtm_mortal_masks
(
    id           BIGSERIAL PRIMARY KEY,
    character_id BIGINT       NOT NULL UNIQUE REFERENCES vtm_characters (id) ON DELETE CASCADE,
    mortal_name  VARCHAR(255) NOT NULL,
    occupation   VARCHAR(255),
    cover_story  TEXT,
    description  TEXT
);