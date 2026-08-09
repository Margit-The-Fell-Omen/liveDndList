-- ============================================================
-- Enum types
-- ============================================================

CREATE TYPE feature_source_type AS ENUM (
    'CLASS', 'SUBCLASS', 'RACE', 'SUBRACE', 'BACKGROUND', 'FEAT'
    );

CREATE TYPE feature_effect_type AS ENUM (
    'MODIFY_ABILITY_SCORE', 'SET_ABILITY_SCORE_MINIMUM', 'GRANT_ABILITY_SCORE_IMPROVEMENT',
    'GRANT_SKILL_PROFICIENCY', 'GRANT_SKILL_EXPERTISE',
    'GRANT_SAVING_THROW_PROFICIENCY',
    'GRANT_ARMOR_PROFICIENCY', 'GRANT_WEAPON_PROFICIENCY',
    'GRANT_TOOL_PROFICIENCY', 'GRANT_LANGUAGE',
    'MODIFY_SPEED', 'GRANT_SPEED_TYPE',
    'MODIFY_ARMOR_CLASS', 'MODIFY_INITIATIVE',
    'GRANT_DAMAGE_RESISTANCE', 'GRANT_DAMAGE_IMMUNITY', 'GRANT_DAMAGE_VULNERABILITY',
    'GRANT_CONDITION_IMMUNITY',
    'GRANT_SENSE',
    'GRANT_SPELLCASTING', 'GRANT_SPELL_SLOTS', 'GRANT_SPELL', 'GRANT_CANTRIP',
    'GRANT_RITUAL_CASTING',
    'MODIFY_SPELL_ATTACK_BONUS', 'MODIFY_SPELL_SAVE_DC', 'PREPARED_SPELLS_COUNT',
    'SET_HIT_DIE', 'MODIFY_HIT_POINTS_PER_LEVEL', 'MODIFY_MAX_HIT_POINTS',
    'SET_CREATURE_SIZE', 'SET_CREATURE_TYPE',
    'GRANT_FEAT', 'GRANT_FIGHTING_STYLE',
    'GRANT_RESOURCE',
    'GRANT_ACTION', 'GRANT_BONUS_ACTION', 'GRANT_REACTION',
    'MODIFY_ATTACK_BONUS', 'MODIFY_DAMAGE', 'ADD_ABILITY_MODIFIER_TO_DAMAGE',
    'NARRATIVE_ONLY'
    );

CREATE TYPE choice_options_source AS ENUM (
    'INLINE', 'SKILL_LIST', 'LANGUAGE_LIST', 'FEAT_LIST',
    'SPELL_LIST', 'TOOL_LIST', 'WEAPON_LIST', 'ARMOR_LIST', 'ABILITY_LIST'
    );

CREATE TYPE rest_type AS ENUM ('SHORT', 'LONG', 'DAWN', 'NONE');

CREATE TYPE visibility_scope AS ENUM ('PUBLIC', 'PRIVATE', 'SHARED');

CREATE TYPE weapon_category AS ENUM ('SIMPLE', 'MARTIAL');

CREATE TYPE creature_size AS ENUM ('TINY', 'SMALL', 'MEDIUM', 'LARGE', 'HUGE', 'GARGANTUAN');

CREATE TYPE caster_type AS ENUM ('FULL', 'HALF', 'THIRD', 'PACT', 'NONE');

CREATE TYPE sense_type AS ENUM ('DARKVISION', 'BLINDSIGHT', 'TREMORSENSE', 'TRUESIGHT');

CREATE TYPE speed_type AS ENUM ('WALK', 'FLY', 'SWIM', 'CLIMB', 'BURROW');

CREATE TYPE annotation_source AS ENUM (
    'NONE', 'OPEN5E_SYNC', 'MANUAL', 'FILE_LOADER'
    );

-- ============================================================
-- Catalog tables
-- ============================================================

CREATE TABLE features
(
    id                   BIGSERIAL PRIMARY KEY,
    key                  VARCHAR(255)        NOT NULL UNIQUE,
    name                 VARCHAR(255)        NOT NULL,
    description          TEXT,
    source_type          feature_source_type NOT NULL,
    source_key           VARCHAR(255)        NOT NULL,
    gained_at_level      INTEGER,
    prerequisite         TEXT,
    visibility           visibility_scope    NOT NULL DEFAULT 'PUBLIC',
    author_id            BIGINT              REFERENCES users (id) ON DELETE SET NULL,
    display_order        INTEGER             NOT NULL DEFAULT 0,
    document_id          BIGINT              REFERENCES documents (id) ON DELETE SET NULL,
    effects_annotated_by annotation_source   NOT NULL DEFAULT 'NONE',
    effects_annotated_at TIMESTAMPTZ,
    created_at           TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_features_class_level
        CHECK (
            (source_type IN ('CLASS', 'SUBCLASS') AND gained_at_level IS NOT NULL)
                OR
            (source_type NOT IN ('CLASS', 'SUBCLASS'))
            )
);

CREATE INDEX idx_features_source ON features (source_type, source_key);
CREATE INDEX idx_features_visibility ON features (visibility);
CREATE INDEX idx_features_document ON features (document_id);
CREATE INDEX idx_features_source_level ON features (source_type, source_key, gained_at_level);

CREATE TABLE feature_effects
(
    id            BIGSERIAL PRIMARY KEY,
    feature_id    BIGINT              NOT NULL REFERENCES features (id) ON DELETE CASCADE,
    effect_type   feature_effect_type NOT NULL,
    payload       JSONB               NOT NULL DEFAULT '{}'::jsonb,
    choice_key    VARCHAR(255),
    display_order INTEGER             NOT NULL DEFAULT 0
);

CREATE INDEX idx_feature_effects_feature ON feature_effects (feature_id);
CREATE INDEX idx_feature_effects_type ON feature_effects (effect_type);

CREATE TABLE feature_choices
(
    id             BIGSERIAL PRIMARY KEY,
    feature_id     BIGINT                NOT NULL REFERENCES features (id) ON DELETE CASCADE,
    choice_key     VARCHAR(255)          NOT NULL,
    name           VARCHAR(255)          NOT NULL,
    description    TEXT,
    choose_count   INTEGER               NOT NULL DEFAULT 1 CHECK (choose_count >= 1),
    options_source choice_options_source NOT NULL,
    options_filter JSONB                 NOT NULL DEFAULT '{}'::jsonb,
    display_order  INTEGER               NOT NULL DEFAULT 0,

    UNIQUE (feature_id, choice_key)
);

CREATE INDEX idx_feature_choices_feature ON feature_choices (feature_id);

CREATE TYPE character_feature_source AS ENUM (
    'CLASS', 'SUBCLASS', 'RACE', 'SUBRACE', 'BACKGROUND', 'FEAT', 'FIGHTING_STYLE', 'CUSTOM'
    );

CREATE TABLE character_features
(
    id             BIGSERIAL PRIMARY KEY,
    character_id   BIGINT                   NOT NULL REFERENCES characters (id) ON DELETE CASCADE,
    feature_id     BIGINT                   NOT NULL REFERENCES features (id) ON DELETE CASCADE,
    source         character_feature_source NOT NULL,
    source_context JSONB                    NOT NULL DEFAULT '{}'::jsonb,
    active         BOOLEAN                  NOT NULL DEFAULT TRUE,
    display_order  INTEGER                  NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ              NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_character_features_not_custom CHECK (source <> 'CUSTOM'),

    UNIQUE (character_id, feature_id, source, source_context)
);

CREATE INDEX idx_character_features_character ON character_features (character_id);
CREATE INDEX idx_character_features_feature ON character_features (feature_id);

CREATE TABLE character_feature_choices
(
    id                   BIGSERIAL PRIMARY KEY,
    character_feature_id BIGINT       NOT NULL REFERENCES character_features (id) ON DELETE CASCADE,
    choice_key           VARCHAR(255) NOT NULL,
    selected_values      JSONB        NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    UNIQUE (character_feature_id, choice_key)
);

CREATE TABLE character_custom_features
(
    id            BIGSERIAL PRIMARY KEY,
    character_id  BIGINT       NOT NULL REFERENCES characters (id) ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ccf_character ON character_custom_features (character_id);

CREATE TABLE character_feats
(
    id                      BIGSERIAL PRIMARY KEY,
    character_id            BIGINT      NOT NULL REFERENCES characters (id) ON DELETE CASCADE,
    feat_id                 BIGINT      NOT NULL REFERENCES dnd_feats (id) ON DELETE CASCADE,
    acquired_at_total_level INTEGER,
    asi_slot_class_key      VARCHAR(255),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (character_id, feat_id)
);

CREATE INDEX idx_character_feats_character ON character_feats (character_id);

CREATE TABLE character_resources
(
    id                BIGSERIAL PRIMARY KEY,
    character_id      BIGINT       NOT NULL REFERENCES characters (id) ON DELETE CASCADE,
    resource_key      VARCHAR(255) NOT NULL,
    display_name      VARCHAR(255) NOT NULL,
    current_uses      INTEGER      NOT NULL DEFAULT 0,
    max_uses          INTEGER      NOT NULL DEFAULT 0,
    refresh_on        rest_type    NOT NULL DEFAULT 'LONG',
    source_feature_id BIGINT       REFERENCES features (id) ON DELETE SET NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    UNIQUE (character_id, resource_key)
);

CREATE INDEX idx_char_resources_character ON character_resources (character_id);

CREATE TABLE character_subclass_choices
(
    id           BIGSERIAL PRIMARY KEY,
    character_id BIGINT       NOT NULL REFERENCES characters (id) ON DELETE CASCADE,
    class_key    VARCHAR(255) NOT NULL,
    subclass_key VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    UNIQUE (character_id, class_key)
);

CREATE INDEX idx_character_subclass_choices_character
    ON character_subclass_choices (character_id);

ALTER TABLE characters
    DROP COLUMN IF EXISTS features_and_traits,
    DROP COLUMN IF EXISTS armor_class,
    DROP COLUMN IF EXISTS proficiency_bonus,
    DROP COLUMN IF EXISTS initiative;

ALTER TABLE characters
    ADD COLUMN base_walking_speed_override INTEGER;

ALTER TABLE dnd_classes
    ADD COLUMN visibility visibility_scope NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE dnd_classes
    ADD COLUMN author_id BIGINT REFERENCES users (id) ON DELETE SET NULL;
ALTER TABLE races
    ADD COLUMN visibility visibility_scope NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE races
    ADD COLUMN author_id BIGINT REFERENCES users (id) ON DELETE SET NULL;
ALTER TABLE backgrounds
    ADD COLUMN visibility visibility_scope NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE backgrounds
    ADD COLUMN author_id BIGINT REFERENCES users (id) ON DELETE SET NULL;
ALTER TABLE dnd_feats
    ADD COLUMN visibility visibility_scope NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE dnd_feats
    ADD COLUMN author_id BIGINT REFERENCES users (id) ON DELETE SET NULL;