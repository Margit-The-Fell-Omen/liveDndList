ALTER TABLE character_saving_throws
    ALTER COLUMN saving_throw_proficiencies TYPE varchar(32) USING saving_throw_proficiencies::text;