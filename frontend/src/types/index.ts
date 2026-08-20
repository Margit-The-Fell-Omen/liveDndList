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
  refreshToken?: string;
  user: User;
}

// ═══════════════════════════════════════════════════════════════
// REFERENCE DATA TYPES
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

export type DndFeatType = 'GENERAL' | 'ORIGIN' | 'FIGHTING_STYLE' | 'EPIC_BOON';

export interface DndFeat {
  key: string;
  name: string;
  desc: string;
  type: DndFeatType;
  hasPrerequisite: boolean;
  prerequisite?: string;
  benefits: string[];
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

export type FeatureType =
    | 'CLASS_TABLE_DATA' | 'PROFICIENCY_BONUS' | 'CLASS_LEVEL_FEATURE'
    | 'PROFICIENCIES' | 'STARTING_EQUIPMENT' | 'CLASS_FEATURE_OPTION_LIST'
    | 'SPELL_SLOTS' | 'CORE_TRAITS_TABLE';

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
// PRIMITIVE ENUMS
// ═══════════════════════════════════════════════════════════════

export type CharacterAlignment =
    | 'LAWFUL_GOOD' | 'NEUTRAL_GOOD' | 'CHAOTIC_GOOD'
    | 'LAWFUL_NEUTRAL' | 'TRUE_NEUTRAL' | 'CHAOTIC_NEUTRAL'
    | 'LAWFUL_EVIL' | 'NEUTRAL_EVIL' | 'CHAOTIC_EVIL' | 'UNALIGNED';

export type AbilityType =
    | 'STRENGTH' | 'DEXTERITY' | 'CONSTITUTION'
    | 'INTELLIGENCE' | 'WISDOM' | 'CHARISMA';

export type AbilityAbbr = 'STR' | 'DEX' | 'CON' | 'INT' | 'WIS' | 'CHA';

export type SkillName =
    | 'ACROBATICS' | 'ANIMAL_HANDLING' | 'ARCANA' | 'ATHLETICS' | 'DECEPTION'
    | 'HISTORY' | 'INSIGHT' | 'INTIMIDATION' | 'INVESTIGATION' | 'MEDICINE'
    | 'NATURE' | 'PERCEPTION' | 'PERFORMANCE' | 'PERSUASION' | 'RELIGION'
    | 'SLEIGHT_OF_HAND' | 'STEALTH' | 'SURVIVAL';

// Used only in SavingThrows (legacy reference in constants.ts)
export type AbilityName = AbilityType;

export type EquipmentType = 'WEAPON' | 'ARMOR' | 'GEAR' | 'CONSUMABLE' | 'TOOL' | 'OTHER';

export type SpellSchool =
    | 'ABJURATION' | 'CONJURATION' | 'DIVINATION' | 'ENCHANTMENT'
    | 'EVOCATION' | 'ILLUSION' | 'NECROMANCY' | 'TRANSMUTATION';

export type ArmorCategory = 'LIGHT' | 'MEDIUM' | 'HEAVY' | 'SHIELD';

export type ChoiceOptionsSource =
    | 'INLINE' | 'SKILL_LIST' | 'LANGUAGE_LIST' | 'FEAT_LIST'
    | 'SPELL_LIST' | 'TOOL_LIST' | 'WEAPON_LIST' | 'ARMOR_LIST' | 'ABILITY_LIST';

export type CreatureSize = 'TINY' | 'SMALL' | 'MEDIUM' | 'LARGE' | 'HUGE' | 'GARGANTUAN';

// ═══════════════════════════════════════════════════════════════
// ABILITY SCORES
// ═══════════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════════
// PIPELINE OUTPUT TYPES (from ComputedCharacterState)
// ═══════════════════════════════════════════════════════════════

export interface SenseResponse {
  senseType: string;
  range: number;
}

export interface HitDiceEntryResponse {
  die: string;
  count: number;
}

export interface ProficienciesResponse {
  armor: string[];
  weapons: string[];
  tools: string[];
  languages: string[];
}

export interface ClassSpellcastingResponse {
  classKey: string;
  ability: string;
  casterType: string;
  spellSaveDc: number;
  spellAttackBonus: number;
  spellSlotsTotal: Record<number, number>;
  spellSlotsUsed: Record<number, number>;
  preparedSpellsCount: number | null;
  spellList: string | null;
  ritualCasting: boolean;
}

export interface SpellcastingResponse {
  classes: ClassSpellcastingResponse[];
}

export interface ResourceResponse {
  resourceKey: string;
  displayName: string;
  currentUses: number;
  maxUses: number;
  refreshOn: string;
  sourceFeatureId: number | null;
}

export interface ActionResponse {
  kind: 'ACTION' | 'BONUS_ACTION' | 'REACTION';
  name: string;
  description: string;
  resourceKey: string | null;
  uses: number | null;
  refresh: string | null;
}

export interface AttackModifierResponse {
  amount: number;
  dice: string | null;
  filter: unknown; // JsonNode — opaque on frontend for now
}

export interface FeatureChoiceAnswerResponse {
  choiceKey: string;
  name: string;
  selectedValues: unknown; // JsonNode array
}

export interface CharacterFeatureResponse {
  id: number;
  name: string;
  description: string;
  source: 'CLASS' | 'SUBCLASS' | 'RACE' | 'SUBRACE' | 'BACKGROUND' | 'FEAT' | 'FIGHTING_STYLE' | 'CUSTOM';
  sourceLabel: string;
  sourceContext: unknown; // JsonNode
  choices: FeatureChoiceAnswerResponse[];
}

export interface CustomFeatureResponse {
  id: number;
  name: string;
  description: string;
}

export interface PendingChoiceResponse {
  characterFeatureId: number;
  choiceKey: string;
  name: string;
  description: string;
  chooseCount: number;
  optionsSource: ChoiceOptionsSource;
  optionsFilter: unknown; // JsonNode
  currentSelection: unknown | null;
}

// ═══════════════════════════════════════════════════════════════
// EQUIPMENT & SPELL RESPONSES
// ═══════════════════════════════════════════════════════════════

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
  armorClass?: number;
  damageType?: string;
  properties?: string;
  armorCategory?: ArmorCategory;
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

export interface SkillResponse {
  id: number;
  skillType: SkillName;
  abilityType: AbilityType;
  proficient: boolean;
  expertise: boolean;
  totalBonus: number;
}

// ═══════════════════════════════════════════════════════════════
// CHARACTER TYPES
// ═══════════════════════════════════════════════════════════════

export interface DndClassLevel {
  level: number;
  classKey: string;
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
  armorClassBonus: number;
  initiative: number;
  proficiencyBonus: number;

  speeds: Record<string, number>;

  hitDice: Record<string, HitDiceEntryResponse>;

  size: string;
  creatureType: string;

  deathSaveSuccesses: number;
  deathSaveFailures: number;

  skills: SkillResponse[];

  savingThrowProficiencies: AbilityType[];

  proficiencies: ProficienciesResponse | null;

  senses: SenseResponse[];

  damageResistances: string[];
  damageImmunities: string[];
  damageVulnerabilities: string[];
  conditionImmunities: string[];

  equipment: EquipmentResponse[];
  currency: DndCurrencyResponse;

  spells: SpellResponse[];
  spellcastingAbility?: AbilityType;

  spellcasting: SpellcastingResponse | null;

  resources: ResourceResponse[];

  actions: ActionResponse[];

  attackModifiers: AttackModifierResponse[];

  features: CharacterFeatureResponse[];

  customFeatures: CustomFeatureResponse[];

  pendingChoices: PendingChoiceResponse[];

  backstory: string;
  personalityTraits: string;
  ideals: string;
  bonds: string;
  flaws: string;
  notes: string;

  createdAt: string;
  updatedAt: string;
}

// ═══════════════════════════════════════════════════════════════
// REQUEST TYPES
// ═══════════════════════════════════════════════════════════════

export interface CharacterCreateRequest {
  name: string;
  raceKey: string;
  backgroundKey: string;
  alignment?: CharacterAlignment;
  classKey: string;
  abilityScores: AbilityScores;
  maxHitPoints: number;
  portraitUrl?: string;
  spellcastingAbility?: AbilityType;
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
  armorClassBonus?: number;
  portraitUrl?: string;
  spellcastingAbility?: string;
  backstory?: string;
  personalityTraits?: string;
  ideals?: string;
  bonds?: string;
  flaws?: string;
  notes?: string;
  deathSaveSuccesses?: number;
  deathSaveFailures?: number;
  dndClassLevels?: DndClassLevel[];
  experiencePoints?: number;
  currency?: DndCurrencyResponse;
}

export interface SubmitChoiceRequest {
  selectedValues: unknown[];
}

export interface CustomFeatureRequest {
  name: string;
  description?: string;
}

export interface ResourceUpdateRequest {
  current?: number;
  delta?: number;
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
  feats: DndFeat[];
  classes: CharacterClass[];
  backgrounds: Background[];
  fetchCharacters: () => Promise<void>;
  fetchReferenceData: () => Promise<void>;
  selectCharacter: (id: number) => Promise<void>;
  createCharacter: (data: CharacterCreateRequest) => Promise<Character>;
  updateCharacter: (id: number, data: CharacterUpdateRequest) => Promise<Character>;
  deleteCharacter: (id: number) => Promise<void>;
  clearError: () => void;
  // Equipment
  addEquipment: (data: EquipmentData) => Promise<void>;
  updateEquipment: (itemId: number, data: EquipmentData) => Promise<void>;
  removeEquipment: (itemId: number) => Promise<void>;
  toggleEquipmentEquipped: (itemId: number) => Promise<void>;
  // Spells
  addSpellToCharacter: (spellId: number) => Promise<void>;
  removeSpellFromCharacter: (spellId: number) => Promise<void>;
  // Choices
  submitChoice: (characterFeatureId: number, choiceKey: string, selectedValues: unknown[]) => Promise<void>;
  clearChoice: (characterFeatureId: number, choiceKey: string) => Promise<void>;
  // Custom features
  createCustomFeature: (name: string, description?: string) => Promise<void>;
  updateCustomFeature: (id: number, name: string, description?: string) => Promise<void>;
  deleteCustomFeature: (id: number) => Promise<void>;
  // Resources
  adjustResource: (resourceKey: string, delta: number) => Promise<void>;
}

export interface EquipmentData {
  name: string;
  description?: string;
  quantity: number;
  weight?: number;
  type: EquipmentType;
  equipped?: boolean;
  damage?: string;
  armorClass?: number;
  damageType?: string;
  properties?: string;
  armorCategory?: ArmorCategory;
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
