// src/components/common/Button.tsx
import React from 'react';
// Assuming your button styles are in a CSS module
import styles from './Button.module.css';

// Define the props for our Button component
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'small' | 'medium' | 'large';
  fullWidth?: boolean;
  loading?: boolean; // Add loading prop
}

export function Button({
                         children,
                         variant = 'primary', // Default variant
                         size = 'medium',   // Default size
                         fullWidth = false,
                         loading = false,
                         className = '',
                         ...props // All other standard button props (like onClick, disabled, etc.)
                       }: ButtonProps) {

  // FIX: Combine CSS module classes based on props
  const buttonClassName = `
    ${styles.button}
    ${styles[variant]}
    ${styles[size]}
    ${fullWidth ? styles.fullWidth : ''}
    ${loading ? styles.loading : ''}
    ${className}
  `.trim();

  return (
      <button className={buttonClassName} {...props}>
        {loading && <div className={styles.spinner}/>}
        <span className={loading ? styles.hiddenText : ''}>
        {children}
      </span>
      </button>
  );
}
