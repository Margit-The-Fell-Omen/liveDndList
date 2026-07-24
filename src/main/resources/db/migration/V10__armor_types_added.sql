CREATE TYPE armor_category AS ENUM ('LIGHT', 'MEDIUM', 'HEAVY', 'SHIELD');
ALTER TABLE equipment
    ADD COLUMN armor_category armor_category;

ALTER TABLE characters
    ADD COLUMN armor_class_bonus INTEGER NOT NULL DEFAULT 0;