// src/components/common/Input.tsx

import React, {type ChangeEvent, useState} from 'react';
import styles from './Input.module.css';

// ===============================================================
// LOCAL TYPE DEFINITIONS
// ===============================================================

// FIX 1: Define the prop types for each component locally.
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


// ===============================================================
// INPUT COMPONENT
// ===============================================================

export function Input({
                        label,
                        type = 'text',
                        error,
                        hint,
                        icon,
                        fullWidth = false,
                        className = '',
                        required,
                        ...props
                      }: InputProps) {
  const [showPassword, setShowPassword] = useState(false);
  const inputType = type === 'password' && showPassword ? 'text' : type;

  return (
      <div className={`${styles.wrapper} ${fullWidth ? styles.fullWidth : ''} ${className}`}>
        {label && (
            <label htmlFor={props.id || props.name} className={styles.label}>
              {label}
              {required && <span className={styles.required}> *</span>}
            </label>
        )}
        <div className={styles.inputWrapper}>
          {icon && <span className={styles.icon}>{icon}</span>}
          <input
              type={inputType}
              className={`${styles.input} ${error ? styles.error : ''} ${icon ? styles.withIcon : ''}`}
              required={required}
              {...props}
          />
          {type === 'password' && (
              <button
                  type="button"
                  className={styles.passwordToggle}
                  onClick={() => setShowPassword(!showPassword)}
                  tabIndex={-1}
                  aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {/* Using text for better accessibility and consistency */}
                {showPassword ? 'Hide' : 'Show'}
              </button>
          )}
        </div>
        {error && <span className={styles.errorMessage}>{error}</span>}
        {hint && !error && <span className={styles.hint}>{hint}</span>}
      </div>
  );
}


// ===============================================================
// TEXTAREA COMPONENT
// ===============================================================

export function TextArea({
                           label,
                           error,
                           hint,
                           fullWidth = false,
                           autoResize = false,
                           className = '',
                           required,
                           ...props
                         }: TextAreaProps) {

  // FIX 2: Rename `onInput` to avoid conflict with the standard prop.
  const handleAutoResize = (e: ChangeEvent<HTMLTextAreaElement>) => {
    if (autoResize) {
      e.target.style.height = 'auto';
      e.target.style.height = `${e.target.scrollHeight}px`;
    }
    // If the original component passed an onInput function, call it.
    if (props.onInput) {
      props.onInput(e);
    }
  };

  return (
      <div className={`${styles.wrapper} ${fullWidth ? styles.fullWidth : ''} ${className}`}>
        {label && (
            <label htmlFor={props.id || props.name} className={styles.label}>
              {label}
              {required && <span className={styles.required}> *</span>}
            </label>
        )}
        <textarea
            className={`${styles.textarea} ${error ? styles.error : ''}`}
            onInput={handleAutoResize}
            required={required}
            {...props}
        />
        {error && <span className={styles.errorMessage}>{error}</span>}
        {hint && !error && <span className={styles.hint}>{hint}</span>}
      </div>
  );
}


// ===============================================================
// SELECT COMPONENT
// ===============================================================

export function Select({
                         label,
                         error,
                         options = [],
                         placeholder,
                         fullWidth = false,
                         className = '',
                         required,
                         ...props
                       }: SelectProps) {
  const getOptionValue = (option: SelectOption | string): string | number => {
    return typeof option === 'string' ? option : option.value;
  };

  const getOptionLabel = (option: SelectOption | string): string => {
    return typeof option === 'string' ? option : option.label;
  };

  return (
      <div className={`${styles.wrapper} ${fullWidth ? styles.fullWidth : ''} ${className}`}>
        {label && (
            <label htmlFor={props.id || props.name} className={styles.label}>
              {label}
              {required && <span className={styles.required}> *</span>}
            </label>
        )}
        <select
            className={`${styles.select} ${error ? styles.error : ''}`}
            required={required}
            defaultValue="" // Use defaultValue for uncontrolled components
            {...props}
        >
          {/* The placeholder is an option that is disabled and hidden once a selection is made */}
          {placeholder && <option value="" disabled>{placeholder}</option>}

          {options.map((option, index) => (
              <option key={`${getOptionValue(option)}-${index}`} value={getOptionValue(option)}>
                {getOptionLabel(option)}
              </option>
          ))}
        </select>
        {error && <span className={styles.errorMessage}>{error}</span>}
      </div>
  );
}
