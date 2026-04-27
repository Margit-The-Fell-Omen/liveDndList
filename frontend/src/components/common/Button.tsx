// src/components/common/Button.tsx
import React from 'react';
import styles from './Button.module.css';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'small' | 'medium' | 'large';
  fullWidth?: boolean;
  loading?: boolean; // Add loading prop
}

export function Button({
                         children,
                         variant = 'primary',
                         size = 'medium',
                         fullWidth = false,
                         loading = false,
                         className = '',
                         ...props
                       }: ButtonProps) {

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
