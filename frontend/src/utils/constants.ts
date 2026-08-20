// src/utils/constants.ts
import type {AbilityType, Character, CharacterAlignment, SkillName} from '@/types';

export interface AbilityInfo {
  key: keyof Character['abilityScores']; // e.g. 'strength'
  name: string;
  abbr: string;
}

export interface SkillInfo {
  key: SkillName;
  name: string;
  ability: AbilityType;
}

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
  {key: 'SLEIGHT_OF_HAND', name: 'Sleight of Hand', ability: 'DEXTERITY'},
  {key: 'STEALTH', name: 'Stealth', ability: 'DEXTERITY'},
  {key: 'SURVIVAL', name: 'Survival', ability: 'WISDOM'},
];

export const ALIGNMENT_OPTIONS: readonly { value: CharacterAlignment; label: string }[] = [
  {value: 'LAWFUL_GOOD', label: 'Lawful Good'},
  {value: 'NEUTRAL_GOOD', label: 'Neutral Good'},
  {value: 'CHAOTIC_GOOD', label: 'Chaotic Good'},
  {value: 'LAWFUL_NEUTRAL', label: 'Lawful Neutral'},
  {value: 'TRUE_NEUTRAL', label: 'True Neutral'},
  {value: 'CHAOTIC_NEUTRAL', label: 'Chaotic Neutral'},
  {value: 'LAWFUL_EVIL', label: 'Lawful Evil'},
  {value: 'NEUTRAL_EVIL', label: 'Neutral Evil'},
  {value: 'CHAOTIC_EVIL', label: 'Chaotic Evil'},
  {value: 'UNALIGNED', label: 'Unaligned'},
];

export const DICE: readonly string[] = ['d4', 'd6', 'd8', 'd10', 'd12', 'd20', 'd100'];
