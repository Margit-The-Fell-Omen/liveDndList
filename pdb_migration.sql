CREATE TYPE character_race_type AS ENUM (
    'HUMAN', 'ELF', 'DWARF', 'HALFLING', 'GNOME', 
    'HALF_ELF', 'HALF_ORC', 'TIEFLING', 'DRAGONBORN', 'AARAKOCRA'
);

CREATE TYPE character_alignment_type AS ENUM (
    'LAWFUL_GOOD', 'NEUTRAL_GOOD', 'CHAOTIC_GOOD',
    'LAWFUL_NEUTRAL', 'TRUE_NEUTRAL', 'CHAOTIC_NEUTRAL',
    'LAWFUL_EVIL', 'NEUTRAL_EVIL', 'CHAOTIC_EVIL', 'UNALIGNED'
);

CREATE TYPE ability_type AS ENUM (
    'STRENGTH', 'DEXTERITY', 'CONSTITUTION', 
    'INTELLIGENCE', 'WISDOM', 'CHARISMA'
);

CREATE TYPE equipment_type AS ENUM (
    'WEAPON', 'ARMOR', 'SHIELD', 'GEAR', 
    'TOOL', 'CONSUMABLE', 'TREASURE', 'MAGIC_ITEM'
);

CREATE TYPE skill_type AS ENUM (
    'ACROBATICS', 'ANIMAL_HANDLING', 'ARCANA', 'ATHLETICS',
    'DECEPTION', 'HISTORY', 'INSIGHT', 'INTIMIDATION',
    'INVESTIGATION', 'MEDICINE', 'NATURE', 'PERCEPTION',
    'PERFORMANCE', 'PERSUASION', 'RELIGION', 'SLEIGHT_OF_HAND',
    'STEALTH', 'SURVIVAL'
);

CREATE TYPE spell_school_type AS ENUM (
    'ABJURATION', 'CONJURATION', 'DIVINATION', 'ENCHANTMENT',
    'EVOCATION', 'ILLUSION', 'NECROMANCY', 'TRANSMUTATION'
);

ALTER TABLE characters ALTER COLUMN race DROP DEFAULT;
ALTER TABLE characters ALTER COLUMN race TYPE character_race_type USING race::character_race_type;

ALTER TABLE characters ALTER COLUMN alignment DROP DEFAULT;
ALTER TABLE characters ALTER COLUMN alignment TYPE character_alignment_type USING alignment::character_alignment_type;

ALTER TABLE characters ALTER COLUMN spellcasting_ability DROP DEFAULT;
ALTER TABLE characters ALTER COLUMN spellcasting_ability TYPE ability_type USING spellcasting_ability::ability_type;

ALTER TABLE character_saving_throws ALTER COLUMN saving_throw_proficiencies DROP DEFAULT;
ALTER TABLE character_saving_throws ALTER COLUMN saving_throw_proficiencies TYPE ability_type USING saving_throw_proficiencies::ability_type;

ALTER TABLE equipment ALTER COLUMN type DROP DEFAULT;
ALTER TABLE equipment ALTER COLUMN type TYPE equipment_type USING type::equipment_type;

ALTER TABLE skills ALTER COLUMN skill_type DROP DEFAULT;
ALTER TABLE skills ALTER COLUMN skill_type TYPE skill_type USING skill_type::skill_type;

ALTER TABLE spells ALTER COLUMN school DROP DEFAULT;
ALTER TABLE spells ALTER COLUMN school TYPE spell_school_type USING school::spell_school_type;
