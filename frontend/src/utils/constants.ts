// src/utils/constants.ts

import type {AbilityName, Character, CharacterAlignment, SkillName} from '@/types';

// ===============================================================
// LOCAL UI-ONLY TYPES
// These are not from the backend, they are for configuring the UI.
// ===============================================================

export interface AbilityInfo {
  key: keyof Character['abilityScores']; // e.g., 'strength'
  name: string; // e.g., 'Strength'
  abbr: string; // e.g., 'STR'
}

export interface SkillInfo {
  key: SkillName; // e.g., 'ACROBATICS'
  name: string; // e.g., 'Acrobatics'
  ability: AbilityName; // e.g., 'DEXTERITY'
}

// ===============================================================
// ABILITIES & SKILLS CONFIGURATION
// Used to render UI elements in a consistent order.
// ===============================================================

export const ABILITIES: readonly AbilityInfo[] = [
  {key: 'strength', name: 'Strength', abbr: 'STR'},
  {key: 'dexterity', name: 'Dexterity', abbr: 'DEX'},
  {key: 'constitution', name: 'Constitution', abbr: 'CON'},
  {key: 'intelligence', name: 'Intelligence', abbr: 'INT'},
  {key: 'wisdom', name: 'Wisdom', abbr: 'WIS'},
  {key: 'charisma', name: 'Charisma', abbr: 'CHA'},
];

export const SKILLS: readonly SkillInfo[] = [
  {key: 'ACROBATICS', name: 'Acrobatics', ability: 'DEXTERITY'},
  {key: 'ANIMAL_HANDLING', name: 'Animal Handling', ability: 'WISDOM'},
  {key: 'ARCANA', name: 'Arcana', ability: 'INTELLIGENCE'},
  {key: 'ATHLETICS', name: 'Athletics', ability: 'STRENGTH'},
  {key: 'DECEPTION', name: 'Deception', ability: 'CHARISMA'},
  {key: 'HISTORY', name: 'History', ability: 'INTELLIGENCE'},
  {key: 'INSIGHT', name: 'Insight', ability: 'WISDOM'},
  {key: 'INTIMIDATION', name: 'Intimidation', ability: 'CHARISMA'},
  {key: 'INVESTIGATION', name: 'Investigation', ability: 'INTELLIGENCE'},
  {key: 'MEDICINE', name: 'Medicine', ability: 'WISDOM'},
  {key: 'NATURE', name: 'Nature', ability: 'INTELLIGENCE'},
  {key: 'PERCEPTION', name: 'Perception', ability: 'WISDOM'},
  {key: 'PERFORMANCE', name: 'Performance', ability: 'CHARISMA'},
  {key: 'PERSUASION', name: 'Persuasion', ability: 'CHARISMA'},
  {key: 'RELIGION', name: 'Religion', ability: 'INTELLIGENCE'},
  {key: 'SLIGHT_OF_HAND', name: 'Sleight of Hand', ability: 'DEXTERITY'},
  {key: 'STEALTH', name: 'Stealth', ability: 'DEXTERITY'},
  {key: 'SURVIVAL', name: 'Survival', ability: 'WISDOM'},
];


// ===============================================================
// REMOVED & DEPRECATED CONSTANTS
// This data should now be fetched from the backend API.
// export const CLASSES: string[] = [ ... ];
// export const RACES: string[] = [ ... ];
// ===============================================================


// ===============================================================
// OTHER UI CONSTANTS
// ===============================================================

export const ALIGNMENTS: readonly CharacterAlignment[] = [
  'LAWFUL_GOOD', 'NEUTRAL_GOOD', 'CHAOTIC_GOOD',
  'LAWFUL_NEUTRAL', 'TRUE_NEUTRAL', 'CHAOTIC_NEUTRAL',
  'LAWFUL_EVIL', 'NEUTRAL_EVIL', 'CHAOTIC_EVIL',
];

export const DICE: readonly string[] = ['d4', 'd6', 'd8', 'd10', 'd12', 'd20', 'd100'];


// ===============================================================
// EMPTY CHARACTER TEMPLATE (CORRECTED)
// This is now correctly structured to match the 'Character' type.
// ===============================================================

export const EMPTY_CHARACTER: Character = {
  id: 0, // Use 0 or a negative number to indicate a new character
  name: 'New Character',
  raceName: '',
  alignment: 'TRUE_NEUTRAL',
  background: '',
  experiencePoints: 0,
  portraitUrl: '',
  classesInfo: [],
  totalLevel: 1,
  abilityScores: {
    strength: 10, strengthModifier: 0,
    dexterity: 10, dexterityModifier: 0,
    constitution: 10, constitutionModifier: 0,
    intelligence: 10, intelligenceModifier: 0,
    wisdom: 10, wisdomModifier: 0,
    charisma: 10, charismaModifier: 0,
  },
  maxHitPoints: 10,
  currentHitPoints: 10,
  temporaryHitPoints: 0,
  armorClass: 10,
  initiative: 0,
  speed: 30,
  proficiencyBonus: 2,
  hitDice: '1d8',
  deathSaveSuccesses: 0,
  deathSaveFailures: 0,
  skills: [], // Skills will be populated by the backend
  savingThrowProficiencies: [], // An empty array of proficient saves
  equipment: [],
  currency: {
    copper: 0,
    silver: 0,
    electrum: 0,
    gold: 0,
    platinum: 0,
  },
  spells: [],
  spellcastingAbility: undefined, // Or a default like 'INTELLIGENCE'
  featuresAndTraits: '',
  backstory: '',
  personalityTraits: '',
  ideals: '',
  bonds: '',
  flaws: '',
  notes: '',
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};
