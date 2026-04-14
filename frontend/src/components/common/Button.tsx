// src/components/common/Button.tsx
import React from 'react';

// Define the props for our Button component
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'small' | 'medium' | 'large';
  fullWidth?: boolean;
}

export function Button({children, ...props}: ButtonProps) {
  // A basic button implementation for now
  return <button {...props}>{children}</button>;
}
