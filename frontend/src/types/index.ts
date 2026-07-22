// types.ts

// ═══════════════════════════════════════════════════════════════
// UTILITY & PAGINATION TYPES
// ═══════════════════════════════════════════════════════════════

export interface PageResponse<T> {
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
  refreshToken?: string;
  user: User;
}

// ═══════════════════════════════════════════════════════════════
// REFERENCE DATA TYPES (Matches backend Open5e DTOs)
// ═══════════════════════════════════════════════════════════════

export interface DocumentInfo {
  name: string;
  key: string;
  type?: string;
  display_name?: string;
  publisher?: Open5eReference;
  gamesystem?: Open5eReference;
  permalink?: string;
}

export interface RaceTrait {
  name: string;
  desc: string;
  type: string | null;
  order: number | null;
}

export interface Race {
  id?: number;
  name: string;
  key: string;
  desc: string;
  subspecies: boolean;
  subraceOf: string | null;
  subraceOfThis: string[];
  traits: RaceTrait[] | null;
  document?: DocumentInfo;
}

export interface Open5eReference {
  name: string;
  key: string;
}

export interface Open5eGainedAt {
  level: number;
  detail: string;
}

export interface Open5eDataForClassTable {
  level: number;
  columnValue: string;
}

export interface ClassFeature {
  name: string;
  key: string;
  desc: string;
  featureType: FeatureType;
  gainedAt: Open5eGainedAt[];
  dataForClassTable: Open5eDataForClassTable[];
}

export interface CharacterClass {
  name: string;
  key: string;
  desc: string;
  hit_dice?: string;
  hitDice?: string;
  hitDiceName?: string;
  hitPointsOn1stLevel?: string;
  hitPointsOnHigherLevels?: string;
  savingThrows: string[];
  subclassOf: Open5eReference | null;
  subclasses: Open5eReference[];
  features: ClassFeature[];
  document?: DocumentInfo;
}

export interface BackgroundBenefit {
  name: string;
  type: string;
  desc: string;
}

export interface Background {
  name: string;
  key: string;
  desc: string;
  benefits: BackgroundBenefit[];
  document?: DocumentInfo;
}

// ═══════════════════════════════════════════════════════════════
// CHARACTER & SUB-COMPONENT TYPES (Matches backend DTOs)
// ═══════════════════════════════════════════════════════════════

export type CharacterAlignment =
    | 'LAWFUL_GOOD' | 'NEUTRAL_GOOD' | 'CHAOTIC_GOOD'
    | 'LAWFUL_NEUTRAL' | 'TRUE_NEUTRAL' | 'CHAOTIC_NEUTRAL'
    | 'LAWFUL_EVIL' | 'NEUTRAL_EVIL' | 'CHAOTIC_EVIL' | 'UNALIGNED';

export type FeatureType =
    | 'CLASS_TABLE_DATA' | 'PROFICIENCY_BONUS' | 'CLASS_LEVEL_FEATURE'
    | 'PROFICIENCIES' | 'STARTING_EQUIPMENT' | 'CLASS_FEATURE_OPTION_LIST'
    | 'SPELL_SLOTS' | 'CORE_TRAITS_TABLE'

export type AbilityType =
    | 'STRENGTH' | 'DEXTERITY' | 'CONSTITUTION'
    | 'INTELLIGENCE' | 'WISDOM' | 'CHARISMA';

export type SkillName =
    | 'ACROBATICS' | 'ANIMAL_HANDLING' | 'ARCANA' | 'ATHLETICS' | 'DECEPTION'
    | 'HISTORY' | 'INSIGHT' | 'INTIMIDATION' | 'INVESTIGATION' | 'MEDICINE'
    | 'NATURE' | 'PERCEPTION' | 'PERFORMANCE' | 'PERSUASION' | 'RELIGION'
    | 'SLEIGHT_OF_HAND' | 'STEALTH' | 'SURVIVAL';

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
  abilityType: AbilityType;
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
  raceKey: string;
  subraceKey?: string;
  backgroundKey: string;
  alignment?: CharacterAlignment;
  classKey: string;
  abilityScores: AbilityScores;
  maxHitPoints: number;
  portraitUrl?: string;
  spellcastingAbility?: AbilityType;
}


export interface CharacterSummary {
  id: number;
  name: string;
  raceKey: string;
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
  raceKey: string;
  alignment: CharacterAlignment;
  backgroundKey: string;
  experiencePoints: number;
  portraitUrl?: string;
  classesInfo: DndClassLevel[];
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
  savingThrowProficiencies: AbilityType[];
  equipment: EquipmentResponse[];
  currency: DndCurrencyResponse;
  spells: SpellResponse[];
  spellcastingAbility?: AbilityType;
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

export interface DndClassLevel {
  level: number;
  classKey: string;
}

export interface CharacterUpdateRequest {
  name?: string;
  raceKey?: string;
  alignment?: CharacterAlignment;
  backgroundKey?: string;
  abilityScores?: AbilityScores;
  maxHitPoints?: number;
  currentHitPoints?: number;
  temporaryHitPoints?: number;
  armorClass?: number;
  speed?: number;
  portraitUrl?: string;
  spellcastingAbility?: AbilityType;
  backstory?: string;
  personalityTraits?: string;
  ideals?: string;
  bonds?: string;
  flaws?: string;
  notes?: string;
  featuresAndTraits?: string;
  deathSaveSuccesses?: number;
  deathSaveFailures?: number;
  savingThrowProficiencies?: AbilityType[];
  skills?: SkillUpdateRequest[];
  initiative?: number;
  dndClassLevels?: DndClassLevel[];
  experiencePoints?: number;
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
  backgrounds: Background[];
  fetchCharacters: () => Promise<void>;
  fetchReferenceData: () => Promise<void>;
  selectCharacter: (id: number) => Promise<void>;
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
  toggleSavingThrowProficiency: (ability: AbilityType) => Promise<void>;
  toggleSkillProficiency: (skillId: number, isNowProficient: boolean) => Promise<void>;
  toggleSkillExpertise: (skillId: number, isNowExpert: boolean) => Promise<void>;
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
