// types.ts

// ═══════════════════════════════════════════════════════════════
// UTILITY & PAGINATION TYPES
// ═══════════════════════════════════════════════════════════════

export interface Page<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ═══════════════════════════════════════════════════════════════
// USER & AUTH TYPES
// ═══════════════════════════════════════════════════════════════

export interface User {
  id: number;
  username: string;
  email: string;
  roles?: string[];
  enabled?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

// ═══════════════════════════════════════════════════════════════
// REFERENCE DATA TYPES
// ═══════════════════════════════════════════════════════════════

export interface Race {
  id: number;
  name: string;
  description?: string;
  traits?: string[];
  speed?: number;
}

export interface CharacterClass {
  id: number;
  name: string;
  description?: string;
  hitDie?: string;
  primaryAbility?: string;
  savingThrows?: string[];
}

export interface Archetype {
  id: number;
  name: string;
  classId: number;
  description?: string;
}

// ═══════════════════════════════════════════════════════════════
// CHARACTER TYPES
// ═══════════════════════════════════════════════════════════════

export type CharacterAlignment =
    | 'LAWFUL_GOOD' | 'NEUTRAL_GOOD' | 'CHAOTIC_GOOD'
    | 'LAWFUL_NEUTRAL' | 'TRUE_NEUTRAL' | 'CHAOTIC_NEUTRAL'
    | 'LAWFUL_EVIL' | 'NEUTRAL_EVIL' | 'CHAOTIC_EVIL';

export type AbilityName =
    | 'STRENGTH' | 'DEXTERITY' | 'CONSTITUTION'
    | 'INTELLIGENCE' | 'WISDOM' | 'CHARISMA';

export interface AbilityScores {
  strength: number;
  dexterity: number;
  constitution: number;
  intelligence: number;
  wisdom: number;
  charisma: number;
}

export interface CharacterCreateRequest {
  name: string;
  raceId: number;
  alignment?: CharacterAlignment;
  background?: string;
  classId: number;
  archetypeId?: number;
  abilityScores: AbilityScores;
  maxHitPoints: number;
  portraitUrl?: string;
  spellcastingAbility?: AbilityName;
}

export interface CharacterSummary {
  id: number;
  name: string;
  raceName: string;
  classDisplay: string;
  totalLevel: number;
  currentHitPoints: number;
  maxHitPoints: number;
  portraitUrl?: string;
  updatedAt: string;
}

export interface Character {
  id: number;
  name: string;
  raceName: string;
  alignment: CharacterAlignment;
  background: string;
  experiencePoints: number;
  portraitUrl?: string;
  classesInfo: string[];
  totalLevel: number;
  abilityScores: {
    strength: number; strengthModifier: number;
    dexterity: number; dexterityModifier: number;
    constitution: number; constitutionModifier: number;
    intelligence: number; intelligenceModifier: number;
    wisdom: number; wisdomModifier: number;
    charisma: number; charismaModifier: number;
  };
  maxHitPoints: number;
  currentHitPoints: number;
  temporaryHitPoints: number;
  armorClass: number;
  initiative: number;
  speed: number;
  proficiencyBonus: number;
  hitDice: string;
  deathSaveSuccesses: number;
  deathSaveFailures: number;
  skills: {
    id: number; skillType: string; abilityType: AbilityName;
    proficient: boolean; expertise: boolean; totalBonus: number;
  }[];
  savingThrowProficiencies: AbilityName[];
  equipment: { id: number; name: string; type: string; }[];
  currency: { copper: number; silver: number; electrum: number; gold: number; platinum: number; };
  spells: { id: number; name: string; level: number; }[];
  spellcastingAbility?: AbilityName;
  featuresAndTraits: string;
  backstory: string;
  personalityTraits: string;
  ideals: string;
  bonds: string;
  flaws: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface CharacterUpdateRequest {
  name?: string;
  raceId?: number;
  alignment?: CharacterAlignment;
  background?: string;
  abilityScores?: AbilityScores;
  maxHitPoints?: number;
  currentHitPoints?: number;
  temporaryHitPoints?: number;
  armorClass?: number;
  speed?: number;
  portraitUrl?: string;
  spellcastingAbility?: AbilityName;
  backstory?: string;
  personalityTraits?: string;
  ideals?: string;
  bonds?: string;
  flaws?: string;
  notes?: string;
}

// ═══════════════════════════════════════════════════════════════
// CONTEXT TYPES
// ═══════════════════════════════════════════════════════════════

export interface CharacterContextType {
  characters: CharacterSummary[]; // Use the summary for lists
  currentCharacter: Character | null; // Use the full object for the selected one
  loading: boolean;
  saving: boolean;
  error: string | null;

  // Reference data
  races: Race[];
  classes: CharacterClass[];
  archetypes: Archetype[];

  // Actions
  fetchCharacters: () => Promise<void>;
  fetchReferenceData: () => Promise<void>;
  selectCharacter: (id: number) => Promise<void>;
  createCharacter: (data: CharacterCreateRequest) => Promise<Character>;
  updateCharacter: (id: number, data: CharacterUpdateRequest) => Promise<Character>;
  deleteCharacter: (id: number) => Promise<void>;
  clearError: () => void;
}
