// src/components/common/Toast.tsx

import {createPortal} from 'react-dom';
// FIX: Import the types from the hook that defines them (`useToast.ts`).
import type {ToastData, ToastType} from '@/hooks/useToast';
import styles from './Toast.module.css';

// Use the imported ToastType for better type safety on the icons record.
const icons: Record<ToastType, string> = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ',
};

interface ToastContainerProps {
  toasts: ToastData[];
  removeToast: (id: number) => void;
}

export function ToastContainer({toasts, removeToast}: ToastContainerProps) {
  if (toasts.length === 0) {
    return null;
  }

  return createPortal(
      <div className={styles.container}>
        {/*
        With the correct import, TypeScript now knows that `toast` is a ToastData object,
        resolving all the "Unresolved variable" errors.
      */}
        {toasts.map((toast) => (
            <div key={toast.id} className={`${styles.toast} ${styles[toast.type]}`} role="alert">
              <span className={styles.icon}>{icons[toast.type]}</span>
              <span className={styles.message}>{toast.message}</span>
              <button
                  className={styles.closeButton}
                  onClick={() => removeToast(toast.id)}
                  aria-label="Dismiss"
              >
                ✕
              </button>
            </div>
        ))}
      </div>,
      document.body
  );
}
