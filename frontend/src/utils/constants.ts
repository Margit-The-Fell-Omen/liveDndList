import type { AbilityInfo, SkillInfo, Character, Skills } from '@/types';

export const ABILITIES: AbilityInfo[] = [
  { key: 'strength', name: 'Strength', abbr: 'STR' },
  { key: 'dexterity', name: 'Dexterity', abbr: 'DEX' },
  { key: 'constitution', name: 'Constitution', abbr: 'CON' },
  { key: 'intelligence', name: 'Intelligence', abbr: 'INT' },
  { key: 'wisdom', name: 'Wisdom', abbr: 'WIS' },
  { key: 'charisma', name: 'Charisma', abbr: 'CHA' },
];

export const SKILLS: SkillInfo[] = [
  { key: 'acrobatics', name: 'Acrobatics', ability: 'dexterity' },
  { key: 'animalHandling', name: 'Animal Handling', ability: 'wisdom' },
  { key: 'arcana', name: 'Arcana', ability: 'intelligence' },
  { key: 'athletics', name: 'Athletics', ability: 'strength' },
  { key: 'deception', name: 'Deception', ability: 'charisma' },
  { key: 'history', name: 'History', ability: 'intelligence' },
  { key: 'insight', name: 'Insight', ability: 'wisdom' },
  { key: 'intimidation', name: 'Intimidation', ability: 'charisma' },
  { key: 'investigation', name: 'Investigation', ability: 'intelligence' },
  { key: 'medicine', name: 'Medicine', ability: 'wisdom' },
  { key: 'nature', name: 'Nature', ability: 'intelligence' },
  { key: 'perception', name: 'Perception', ability: 'wisdom' },
  { key: 'performance', name: 'Performance', ability: 'charisma' },
  { key: 'persuasion', name: 'Persuasion', ability: 'charisma' },
  { key: 'religion', name: 'Religion', ability: 'intelligence' },
  { key: 'sleightOfHand', name: 'Sleight of Hand', ability: 'dexterity' },
  { key: 'stealth', name: 'Stealth', ability: 'dexterity' },
  { key: 'survival', name: 'Survival', ability: 'wisdom' },
];

export const CLASSES: string[] = [
  'Barbarian', 'Bard', 'Cleric', 'Druid', 'Fighter', 'Monk',
  'Paladin', 'Ranger', 'Rogue', 'Sorcerer', 'Warlock', 'Wizard',
  'Artificer', 'Blood Hunter',
];

export const RACES: string[] = [
  'Dragonborn', 'Dwarf', 'Elf', 'Gnome', 'Half-Elf', 'Halfling',
  'Half-Orc', 'Human', 'Tiefling', 'Aarakocra', 'Genasi', 'Goliath',
  'Tabaxi', 'Triton', 'Firbolg', 'Kenku', 'Lizardfolk', 'Tortle',
];

export const ALIGNMENTS: string[] = [
  'Lawful Good', 'Neutral Good', 'Chaotic Good',
  'Lawful Neutral', 'True Neutral', 'Chaotic Neutral',
  'Lawful Evil', 'Neutral Evil', 'Chaotic Evil',
];

export const DICE: string[] = ['d4', 'd6', 'd8', 'd10', 'd12', 'd20', 'd100'];

export const HIT_DICE_BY_CLASS: Record<string, string> = {
  Barbarian: 'd12',
  Bard: 'd8',
  Cleric: 'd8',
  Druid: 'd8',
  Fighter: 'd10',
  Monk: 'd8',
  Paladin: 'd10',
  Ranger: 'd10',
  Rogue: 'd8',
  Sorcerer: 'd6',
  Warlock: 'd8',
  Wizard: 'd6',
  Artificer: 'd8',
  'Blood Hunter': 'd10',
};

const createEmptySkills = (): Skills => {
  return SKILLS.reduce((acc, skill) => {
    acc[skill.key] = { proficient: false, expertise: false };
    return acc;
  }, {} as Skills);
};

export const EMPTY_CHARACTER: Character = {
  id: null,
  name: '',
  race: '',
  class: '',
  level: 1,
  background: '',
  alignment: '',
  experiencePoints: 0,

  abilities: {
    strength: 10,
    dexterity: 10,
    constitution: 10,
    intelligence: 10,
    wisdom: 10,
    charisma: 10,
  },

  savingThrows: {
    strength: false,
    dexterity: false,
    constitution: false,
    intelligence: false,
    wisdom: false,
    charisma: false,
  },

  skills: createEmptySkills(),

  armorClass: 10,
  initiative: 0,
  speed: 30,

  hitPoints: {
    maximum: 10,
    current: 10,
    temporary: 0,
  },

  hitDice: {
    total: 1,
    current: 1,
  },

  deathSaves: {
    successes: 0,
    failures: 0,
  },

  proficiencyBonus: 2,
  inspiration: false,

  personality: {
    traits: '',
    ideals: '',
    bonds: '',
    flaws: '',
  },

  features: [],
  equipment: [],
  attacks: [],
  spells: {
    spellcastingAbility: '',
    spellSaveDC: 0,
    spellAttackBonus: 0,
    cantrips: [],
    slots: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0, 6: 0, 7: 0, 8: 0, 9: 0 },
    slotsUsed: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0, 6: 0, 7: 0, 8: 0, 9: 0 },
    known: [],
  },

  currency: {
    copper: 0,
    silver: 0,
    electrum: 0,
    gold: 0,
    platinum: 0,
  },

  notes: '',
};
