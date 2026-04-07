// ═══════════════════════════════════════════════════════════════
// USER & AUTH TYPES
// ═══════════════════════════════════════════════════════════════

export interface User {
  id: number;
  username: string;
  email: string;
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
// CHARACTER TYPES
// ═══════════════════════════════════════════════════════════════

export interface Abilities {
  strength: number;
  dexterity: number;
  constitution: number;
  intelligence: number;
  wisdom: number;
  charisma: number;
}

export type AbilityKey = keyof Abilities;

export interface SavingThrows {
  strength: boolean;
  dexterity: boolean;
  constitution: boolean;
  intelligence: boolean;
  wisdom: boolean;
  charisma: boolean;
}

export interface SkillProficiency {
  proficient: boolean;
  expertise: boolean;
}

export interface Skills {
  acrobatics: SkillProficiency;
  animalHandling: SkillProficiency;
  arcana: SkillProficiency;
  athletics: SkillProficiency;
  deception: SkillProficiency;
  history: SkillProficiency;
  insight: SkillProficiency;
  intimidation: SkillProficiency;
  investigation: SkillProficiency;
  medicine: SkillProficiency;
  nature: SkillProficiency;
  perception: SkillProficiency;
  performance: SkillProficiency;
  persuasion: SkillProficiency;
  religion: SkillProficiency;
  sleightOfHand: SkillProficiency;
  stealth: SkillProficiency;
  survival: SkillProficiency;
}

export type SkillKey = keyof Skills;

export interface HitPoints {
  maximum: number;
  current: number;
  temporary: number;
}

export interface HitDice {
  total: number;
  current: number;
}

export interface DeathSaves {
  successes: number;
  failures: number;
}

export interface Personality {
  traits: string;
  ideals: string;
  bonds: string;
  flaws: string;
}

export interface Currency {
  copper: number;
  silver: number;
  electrum: number;
  gold: number;
  platinum: number;
}

export interface EquipmentItem {
  id: number;
  name: string;
  quantity: number;
  description?: string;
}

export interface Feature {
  id: number;
  name: string;
  description: string;
  source?: string;
}

export interface Attack {
  id: number;
  name: string;
  attackBonus: number;
  damage: string;
  damageType: string;
  range?: string;
  notes?: string;
}

export interface Spell {
  id: number;
  name: string;
  level: number;
  description?: string;
  castingTime?: string;
  range?: string;
  components?: string;
  duration?: string;
  school?: string;
  prepared?: boolean;
}

export interface SpellSlots {
  1: number;
  2: number;
  3: number;
  4: number;
  5: number;
  6: number;
  7: number;
  8: number;
  9: number;
}

export interface Spells {
  spellcastingAbility: AbilityKey | '';
  spellSaveDC: number;
  spellAttackBonus: number;
  cantrips: Spell[];
  slots: SpellSlots;
  slotsUsed: SpellSlots;
  known: Spell[];
}

export interface Character {
  id: number | null;
  name: string;
  race: string;
  class: string;
  level: number;
  background: string;
  alignment: string;
  experiencePoints: number;

  abilities: Abilities;
  savingThrows: SavingThrows;
  skills: Skills;

  armorClass: number;
  initiative: number;
  speed: number;

  hitPoints: HitPoints;
  hitDice: HitDice;
  deathSaves: DeathSaves;

  proficiencyBonus: number;
  inspiration: boolean;

  personality: Personality;
  features: Feature[];
  equipment: EquipmentItem[];
  attacks: Attack[];
  spells: Spells;
  currency: Currency;
  notes: string;
}

// ═══════════════════════════════════════════════════════════════
// COMPONENT PROP TYPES
// ═══════════════════════════════════════════════════════════════

export interface ButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
  loading?: boolean;
  fullWidth?: boolean;
  type?: 'button' | 'submit' | 'reset';
  onClick?: () => void;
  className?: string;
}

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string | null;
  hint?: string;
  icon?: React.ReactNode;
  fullWidth?: boolean;
}

export interface TextAreaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string | null;
  hint?: string;
  fullWidth?: boolean;
  autoResize?: boolean;
}

export interface SelectOption {
  value: string | number;
  label: string;
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string | null;
  options: (SelectOption | string)[];
  placeholder?: string;
  fullWidth?: boolean;
}

export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  size?: 'small' | 'medium' | 'large';
  closeOnOverlay?: boolean;
}

export interface ConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: 'primary' | 'danger';
  loading?: boolean;
}

export interface TooltipProps {
  children: React.ReactNode;
  content: string;
  position?: 'top' | 'bottom' | 'left' | 'right';
  delay?: number;
}

export interface ToastData {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

// ═══════════════════════════════════════════════════════════════
// CONTEXT TYPES
// ═══════════════════════════════════════════════════════════════

export interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials) => Promise<AuthResponse>;
  register: (data: RegisterData) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  clearError: () => void;
}

export interface ThemeContextType {
  theme: 'light' | 'dark' | 'system';
  setTheme: (theme: 'light' | 'dark' | 'system') => void;
  toggleTheme: () => void;
}

export interface CharacterContextType {
  characters: Character[];
  currentCharacter: Character | null;
  loading: boolean;
  saving: boolean;
  error: string | null;
  hasUnsavedChanges: boolean;
  fetchCharacters: () => Promise<void>;
  selectCharacter: (id: number) => Promise<void>;
  createCharacter: (name?: string) => Promise<Character>;
  updateCharacter: (updates: Partial<Character>) => void;
  updateNestedCharacter: (path: string, value: unknown) => void;
  saveCharacter: (character?: Character) => Promise<Character | undefined>;
  deleteCharacter: (id: number) => Promise<void>;
  duplicateCharacter: (id: number) => Promise<Character>;
  clearError: () => void;
}

// ═══════════════════════════════════════════════════════════════
// UTILITY TYPES
// ═══════════════════════════════════════════════════════════════

export interface ValidationResult {
  valid: boolean;
  value?: number;
  message?: string;
}

export interface DiceRoll {
  rolls: number[];
  modifier: number;
  total: number;
}

export interface ParsedDice {
  count: number;
  sides: number;
  modifier: number;
}

export type ValidatorFn = (value: string) => string | null;

// ═══════════════════════════════════════════════════════════════
// CONSTANTS TYPES
// ═══════════════════════════════════════════════════════════════

export interface AbilityInfo {
  key: AbilityKey;
  name: string;
  abbr: string;
}

export interface SkillInfo {
  key: SkillKey;
  name: string;
  ability: AbilityKey;
}
