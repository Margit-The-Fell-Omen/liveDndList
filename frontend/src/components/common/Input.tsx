import { useState, type ChangeEvent } from 'react';
import type { InputProps, TextAreaProps, SelectProps, SelectOption } from '@/types';
import styles from './Input.module.css';

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
        <label className={styles.label}>
          {label}
          {required && <span className={styles.required}>*</span>}
        </label>
      )}

      <div className={styles.inputWrapper}>
        {icon && <span className={styles.icon}>{icon}</span>}

        <input
          type={inputType}
          className={`
            ${styles.input}
            ${error ? styles.error : ''}
            ${icon ? styles.withIcon : ''}
          `}
          required={required}
          {...props}
        />

        {type === 'password' && (
          <button
            type="button"
            className={styles.passwordToggle}
            onClick={() => setShowPassword(!showPassword)}
            tabIndex={-1}
          >
            {showPassword ? '👁️' : '👁️‍🗨️'}
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
  fullWidth = false,
  autoResize = false,
  className = '',
  required,
  onInput,
  ...props
}: TextAreaProps) {
  const handleInput = (e: ChangeEvent<HTMLTextAreaElement>) => {
    if (autoResize) {
      e.target.style.height = 'auto';
      e.target.style.height = `${e.target.scrollHeight}px`;
    }
    // Note: onInput is passed via props spread
  };

  return (
    <div className={`${styles.wrapper} ${fullWidth ? styles.fullWidth : ''} ${className}`}>
      {label && (
        <label className={styles.label}>
          {label}
          {required && <span className={styles.required}>*</span>}
        </label>
      )}

      <textarea
        className={`${styles.textarea} ${error ? styles.error : ''}`}
        onInput={handleInput}
        required={required}
        {...props}
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
  placeholder = 'Select...',
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
        <label className={styles.label}>
          {label}
          {required && <span className={styles.required}>*</span>}
        </label>
      )}

      <select 
        className={`${styles.select} ${error ? styles.error : ''}`} 
        required={required}
        {...props}
      >
        <option value="">{placeholder}</option>
        {options.map((option) => (
          <option key={getOptionValue(option)} value={getOptionValue(option)}>
            {getOptionLabel(option)}
          </option>
        ))}
      </select>

      {error && <span className={styles.errorMessage}>{error}</span>}
    </div>
  );
}
