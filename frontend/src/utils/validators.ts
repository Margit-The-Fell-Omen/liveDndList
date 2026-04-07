import type { ValidatorFn } from '@/types';

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
    const num = parseInt(value, 10);
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

export function validate(value: string, ...validatorFns: ValidatorFn[]): string | null {
  for (const validator of validatorFns) {
    const error = validator(value);
    if (error) return error;
  }
  return null;
}
