// src/utils/validators.ts

// FIX: Define the ValidatorFn type locally. It represents a function
// that takes a string and returns either an error string or null.
export type ValidatorFn = (value: string) => string | null;


export const validators = {
  required: (value: string): string | null => {
    if (!value || (typeof value === 'string' && !value.trim())) {
      return 'This field is required';
    }
    return null;
  },

  email: (value: string): string | null => {
    if (!value) return null;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(value)) {
      return 'Please enter a valid email address';
    }
    return null;
  },

  minLength: (min: number): ValidatorFn => (value: string): string | null => {
    if (!value) return null;
    if (value.length < min) {
      return `Must be at least ${min} characters`;
    }
    return null;
  },

  maxLength: (max: number): ValidatorFn => (value: string): string | null => {
    if (!value) return null;
    if (value.length > max) {
      return `Must be no more than ${max} characters`;
    }
    return null;
  },

  password: (value: string): string | null => {
    if (!value) return null;
    if (value.length < 8) {
      return 'Password must be at least 8 characters';
    }
    if (!/[A-Z]/.test(value)) {
      return 'Password must contain an uppercase letter';
    }
    if (!/[a-z]/.test(value)) {
      return 'Password must contain a lowercase letter';
    }
    if (!/[0-9]/.test(value)) {
      return 'Password must contain a number';
    }
    return null;
  },

  username: (value: string): string | null => {
    if (!value) return null;
    if (value.length < 3) {
      return 'Username must be at least 3 characters';
    }
    if (!/^[a-zA-Z0-9_]+$/.test(value)) {
      return 'Username can only contain letters, numbers, and underscores';
    }
    return null;
  },

  number: (min?: number, max?: number): ValidatorFn => (value: string): string | null => {
    if (!value || !value.trim()) return null; // Don't validate empty strings, `required` validator should handle that
    const num = Number(value);
    if (isNaN(num)) {
      return 'Must be a number';
    }
    if (min !== undefined && num < min) {
      return `Minimum value is ${min}`;
    }
    if (max !== undefined && num > max) {
      return `Maximum value is ${max}`;
    }
    return null;
  },

  match: (fieldName: string, getValue: () => string): ValidatorFn => (value: string): string | null => {
    if (value !== getValue()) {
      return `Must match ${fieldName}`;
    }
    return null;
  },
};

/**
 * A helper function to run multiple validators on a single value.
 * Stops and returns the first error found.
 *
 * @param value The value to validate.
 * @param validatorFns An array of validator functions to run.
 * @returns The first error message string, or null if all validators pass.
 */
export function validate(value: string, ...validatorFns: ValidatorFn[]): string | null {
  for (const validator of validatorFns) {
    const error = validator(value);
    if (error) return error;
  }
  return null;
}
