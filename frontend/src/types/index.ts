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
// USER & AUTH TYPES (Unchanged as requested)
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
// REFERENCE DATA TYPES (Matches backend Open5e DTOs)
// ═══════════════════════════════════════════════════════════════

interface DocumentInfo {
  document__slug: string;
  document__title: string;
  document__license_url?: string;
  document__url?: string;
}

export interface Asi {
  attributes: string[];
  value: number;
}

export interface Speed {
  walk?: number;
  swim?: number;
  fly?: number;
}

export interface Subrace extends DocumentInfo {
  name: string;
  slug: string;
  desc: string;
  asi: Asi[];
  asi_desc: string;
  traits: string;
}

export interface Race extends DocumentInfo {
  name: string;
  slug: string;
  desc: string;
  asi_desc: string;
  asi: Asi[];
  age: string;
  alignment: string;
  size: string;
  size_raw: string;
  speed: Speed;
  speed_desc: string;
  languages: string;
  vision: string;
  traits: string;
  subraces: Subrace[];
}

export interface Archetype extends DocumentInfo {
  name: string;
  slug: string;
  desc: string;
}

export interface CharacterClass extends DocumentInfo {
  name: string;
  slug: string;
  desc: string;
  hit_dice: string;
  hp_at_1st_level: string;
  hp_at_higher_levels: string;
  prof_armor: string;
  prof_weapons: string;
  prof_tools: string;
  prof_saving_throws: string;
  prof_skills: string;
  equipment: string;
  table: string;
  spellcasting_ability: string;
  subtypes_name: string;
  archetypes: Archetype[];
}

// ═══════════════════════════════════════════════════════════════
// CHARACTER & SUB-COMPONENT TYPES (Matches backend DTOs)
// ═══════════════════════════════════════════════════════════════

export type CharacterAlignment =
    | 'LAWFUL_GOOD' | 'NEUTRAL_GOOD' | 'CHAOTIC_GOOD'
    | 'LAWFUL_NEUTRAL' | 'TRUE_NEUTRAL' | 'CHAOTIC_NEUTRAL'
    | 'LAWFUL_EVIL' | 'NEUTRAL_EVIL' | 'CHAOTIC_EVIL' | 'UNALIGNED';

export type AbilityName =
    | 'STRENGTH' | 'DEXTERITY' | 'CONSTITUTION'
    | 'INTELLIGENCE' | 'WISDOM' | 'CHARISMA';

export type SkillName =
    | 'ACROBATICS' | 'ANIMAL_HANDLING' | 'ARCANA' | 'ATHLETICS' | 'DECEPTION'
    | 'HISTORY' | 'INSIGHT' | 'INTIMIDATION' | 'INVESTIGATION' | 'MEDICINE'
    | 'NATURE' | 'PERCEPTION' | 'PERFORMANCE' | 'PERSUASION' | 'RELIGION'
    | 'SLIGHT_OF_HAND' | 'STEALTH' | 'SURVIVAL';

export type EquipmentType = 'WEAPON' | 'ARMOR' | 'GEAR' | 'CONSUMABLE' | 'TOOL' | 'OTHER';
export type SpellSchool =
    'ABJURATION'
    | 'CONJURATION'
    | 'DIVINATION'
    | 'ENCHANTMENT'
    | 'EVOCATION'
    | 'ILLUSION'
    | 'NECROMANCY'
    | 'TRANSMUTATION';

export interface AbilityScores {
  strength: number;
  dexterity: number;
  constitution: number;
  intelligence: number;
  wisdom: number;
  charisma: number;
}

export interface AbilityScoresResponse {
  strength: number;
  strengthModifier: number;
  dexterity: number;
  dexterityModifier: number;
  constitution: number;
  constitutionModifier: number;
  intelligence: number;
  intelligenceModifier: number;
  wisdom: number;
  wisdomModifier: number;
  charisma: number;
  charismaModifier: number;
}

export interface SkillResponse {
  id: number;
  skillType: SkillName;
  abilityType: AbilityName;
  proficient: boolean;
  expertise: boolean;
  totalBonus: number;
}

export interface EquipmentResponse {
  id: number;
  name: string;
  description: string;
  quantity: number;
  weight: number;
  equipped: boolean;
  attuned: boolean;
  type: EquipmentType;
  damage?: string;
  damageType?: string;
  properties?: string;
}

export interface DndCurrencyResponse {
  copper: number;
  silver: number;
  electrum: number;
  gold: number;
  platinum: number;
}

export interface SpellResponse {
  id: number;
  name: string;
  level: number;
  school: SpellSchool;
  castingTime: string;
  range: string;
  components: string;
  duration: string;
  concentration: boolean;
  ritual: boolean;
  description: string;
  higherLevels?: string;
}

export interface CharacterCreateRequest {
  name: string;
  raceSlug: string;
  alignment?: CharacterAlignment;
  background?: string;
  classSlug: string;
  archetypeSlug?: string;
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
  abilityScores: AbilityScoresResponse;
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
  skills: SkillResponse[];
  savingThrowProficiencies: AbilityName[];
  equipment: EquipmentResponse[]; // Finalized
  currency: DndCurrencyResponse;
  spells: SpellResponse[]; // Finalized
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
  featuresAndTraits?: string;
  deathSaveSuccesses?: number;
  deathSaveFailures?: number;
  experiencePoints?: number;
  savingThrowProficiencies?: AbilityName[];
  skills?: SkillUpdateRequest[];
  initiative?: number;
}

export interface SkillUpdateRequest {
  id: number;
  proficient?: boolean;
  expertise?: boolean;
}

// ═══════════════════════════════════════════════════════════════
// CONTEXT TYPES
// ═══════════════════════════════════════════════════════════════

export interface CharacterContextType {
  characters: CharacterSummary[];
  currentCharacter: Character | null;
  loading: boolean;
  saving: boolean;
  error: string | null;

  races: Race[];
  classes: CharacterClass[];

  fetchCharacters: () => Promise<void>;
  fetchReferenceData: () => Promise<void>;
  selectCharacter: (id: number) => Promise<void>;
  createCharacter: (data: CharacterCreateRequest) => Promise<Character>;
  updateCharacter: (id: number, data: CharacterUpdateRequest) => Promise<Character>;
  deleteCharacter: (id: number) => Promise<void>;
  clearError: () => void;
}

export interface EquipmentData {
  name: string;
  description?: string;
  quantity: number;
  weight?: number;
  type: EquipmentType;
  equipped?: boolean;
  damage?: string;
  damageType?: string;
  properties?: string;
}

export interface SpellData {
  name: string;
  level: number;
  school: SpellSchool;
  castingTime: string;
  range: string;
  components: string;
  duration: string;
  concentration: boolean;
  ritual: boolean;
  description: string;
  higherLevels?: string;
}

export interface CharacterContextType {
  createCharacter: (data: CharacterCreateRequest) => Promise<Character>;
  updateCharacter: (id: number, data: CharacterUpdateRequest) => Promise<Character>;
  deleteCharacter: (id: number) => Promise<void>;
  clearError: () => void;

  addEquipment: (data: EquipmentData) => Promise<void>;
  updateEquipment: (itemId: number, data: EquipmentData) => Promise<void>;
  removeEquipment: (itemId: number) => Promise<void>;
  toggleEquipmentEquipped: (itemId: number) => Promise<void>;

  addSpellToCharacter: (spellId: number) => Promise<void>;
  removeSpellFromCharacter: (spellId: number) => Promise<void>;

  toggleSavingThrowProficiency: (ability: AbilityName) => Promise<void>;
  toggleSkillProficiency: (skillId: number, isNowProficient: boolean) => Promise<void>;
  toggleSkillExpertise: (skillId: number, isNowExpert: boolean) => Promise<void>;
}