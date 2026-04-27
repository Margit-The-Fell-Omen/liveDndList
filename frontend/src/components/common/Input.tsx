// src/components/common/Input.tsx

import React, {useState, type ChangeEvent} from 'react';
import styles from './Input.module.css';

export interface InputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size'> {
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

export function Input({
                        label,
                        type = 'text',
                        error,
                        hint,
                        icon,
                        fullWidth,
                        className = '',
                        required,
                        value,
                        defaultValue,
                        ...props
                      }: InputProps) {
  const [showPassword, setShowPassword] = useState(false);
  const inputType = type === 'password' && showPassword ? 'text' : type;
  const wrapperClassName = `${styles.wrapper} ${fullWidth ? styles.fullWidth : ''} ${className}`;

  const inputProps = value !== undefined ? {value} : {defaultValue};

  return (
      <div className={wrapperClassName}>
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
              {...inputProps}
          />
          {type === 'password' && (
              <button
                  type="button"
                  className={styles.passwordToggle}
                  onClick={() => setShowPassword(!showPassword)}
                  tabIndex={-1}
                  aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
          )}
        </div>
        {error && <span className={styles.errorMessage}>{error}</span>}
        {hint && !error && <span className={styles.hint}>{hint}</span>}
      </div>
  );
}

export function TextArea({
                           label,
                           error,
                           hint,
                           fullWidth,
                           autoResize,
                           className = '',
                           required,
                           value,
                           defaultValue,
                           ...props
                         }: TextAreaProps) {
  const handleAutoResize = (e: ChangeEvent<HTMLTextAreaElement>) => {
    if (autoResize) {
      e.target.style.height = 'auto';
      e.target.style.height = `${e.target.scrollHeight}px`;
    }
    if (props.onInput) {
      props.onInput(e);
    }
  };

  const textAreaProps = value !== undefined ? {value} : {defaultValue};

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
            {...textAreaProps}
        />
        {error && <span className={styles.errorMessage}>{error}</span>}
        {hint && !error && <span className={styles.hint}>{hint}</span>}
      </div>
  );
}

export function Select({
                         label,
                         error,
                         options = [],
                         placeholder,
                         fullWidth,
                         className = '',
                         required,
                         value,
                         defaultValue,
                         ...props
                       }: SelectProps) {
  const selectProps = value !== undefined ? {value} : {defaultValue};

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
            {...props}
            {...selectProps}
        >
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
