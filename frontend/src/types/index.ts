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
// REFERENCE DATA TYPES (from your backend)
// ═══════════════════════════════════════════════════════════════

export interface Race {
  id: number;
  name: string;
  description?: string;
  traits?: string[];
  speed?: number;
  // Add other fields your backend returns
}

export interface CharacterClass {
  id: number;
  name: string;
  description?: string;
  hitDie?: string;
  primaryAbility?: string;
  savingThrows?: string[];
  // Add other fields your backend returns
}

export interface Archetype {
  id: number;
  name: string;
  classId: number;
  description?: string;
  // Add other fields your backend returns
}

// ═══════════════════════════════════════════════════════════════
// CHARACTER TYPES
// ═══════════════════════════════════════════════════════════════

export type CharacterAlignment =
    | 'LAWFUL_GOOD'
    | 'NEUTRAL_GOOD'
    | 'CHAOTIC_GOOD'
    | 'LAWFUL_NEUTRAL'
    | 'TRUE_NEUTRAL'
    | 'CHAOTIC_NEUTRAL'
    | 'LAWFUL_EVIL'
    | 'NEUTRAL_EVIL'
    | 'CHAOTIC_EVIL';

export type AbilityName =
    | 'STRENGTH'
    | 'DEXTERITY'
    | 'CONSTITUTION'
    | 'INTELLIGENCE'
    | 'WISDOM'
    | 'CHARISMA';

export interface AbilityScores {
  strength: number;
  dexterity: number;
  constitution: number;
  intelligence: number;
  wisdom: number;
  charisma: number;
}

// Request to create a character (matches your DTO)
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

// Character as returned from the backend (full data)
export interface Character {
  id: number;
  name: string;

  // References
  race: Race;
  characterClass: CharacterClass;
  archetype?: Archetype;

  // Basic info
  level: number;
  experiencePoints: number;
  alignment?: CharacterAlignment;
  background?: string;
  portraitUrl?: string;

  // Ability scores
  abilityScores: AbilityScores;

  // Combat stats
  maxHitPoints: number;
  currentHitPoints: number;
  temporaryHitPoints: number;
  armorClass: number;
  initiative: number;
  speed: number;
  proficiencyBonus: number;

  // Spellcasting
  spellcastingAbility?: AbilityName;

  // Other
  inspiration: boolean;
  notes?: string;

  // Timestamps
  createdAt?: string;
  updatedAt?: string;
}

// For updating character (partial)
export interface CharacterUpdateRequest {
  name?: string;
  raceId?: number;
  alignment?: CharacterAlignment;
  background?: string;
  classId?: number;
  archetypeId?: number;
  abilityScores?: AbilityScores;
  maxHitPoints?: number;
  currentHitPoints?: number;
  temporaryHitPoints?: number;
  armorClass?: number;
  initiative?: number;
  speed?: number;
  portraitUrl?: string;
  spellcastingAbility?: AbilityName;
  inspiration?: boolean;
  notes?: string;
}

// ═══════════════════════════════════════════════════════════════
// FORM STATE TYPES
// ═══════════════════════════════════════════════════════════════

export interface CharacterFormData {
  name: string;
  raceId: number | null;
  classId: number | null;
  archetypeId: number | null;
  alignment: CharacterAlignment | '';
  background: string;
  abilityScores: AbilityScores;
  maxHitPoints: number;
  portraitUrl: string;
  spellcastingAbility: AbilityName | '';
}

export interface CharacterFormErrors {
  name?: string;
  raceId?: string;
  classId?: string;
  abilityScores?: string;
  maxHitPoints?: string;
  general?: string;
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
