import { useState } from 'react';
import styles from './Input.module.css';

export function Input({
  label,
  type = 'text',
  error,
  hint,
  icon,
  fullWidth = false,
  className = '',
  ...props
}) {
  const [showPassword, setShowPassword] = useState(false);
  const inputType = type === 'password' && showPassword ? 'text' : type;

  return (
    <div className={`${styles.wrapper} ${fullWidth ? styles.fullWidth : ''} ${className}`}>
      {label && (
        <label className={styles.label}>
          {label}
          {props.required && <span className={styles.required}>*</span>}
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
  ...props
}) {
  const handleInput = (e) => {
    if (autoResize) {
      e.target.style.height = 'auto';
      e.target.style.height = `${e.target.scrollHeight}px`;
    }
    props.onInput?.(e);
  };

  return (
    <div className={`${styles.wrapper} ${fullWidth ? styles.fullWidth : ''} ${className}`}>
      {label && (
        <label className={styles.label}>
          {label}
          {props.required && <span className={styles.required}>*</span>}
        </label>
      )}
      
      <textarea
        className={`${styles.textarea} ${error ? styles.error : ''}`}
        onInput={handleInput}
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
  ...props
}) {
  return (
    <div className={`${styles.wrapper} ${fullWidth ? styles.fullWidth : ''} ${className}`}>
      {label && (
        <label className={styles.label}>
          {label}
          {props.required && <span className={styles.required}>*</span>}
        </label>
      )}
      
      <select
        className={`${styles.select} ${error ? styles.error : ''}`}
        {...props}
      >
        <option value="">{placeholder}</option>
        {options.map(option => (
          <option 
            key={option.value || option} 
            value={option.value || option}
          >
            {option.label || option}
          </option>
        ))}
      </select>
      
      {error && <span className={styles.errorMessage}>{error}</span>}
    </div>
  );
}
