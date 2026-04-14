// src/hooks/useToast.ts

import {useCallback, useState} from 'react';

// ===============================================================
// LOCAL TYPE DEFINITIONS
// ===============================================================

// FIX 1: Define the types needed by this hook directly in the file.
export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastData {
  id: number;
  message: string;
  type: ToastType;
}

interface UseToastReturn {
  toasts: ToastData[];
  toast: {
    success: (message: string, duration?: number) => number;
    error: (message: string, duration?: number) => number;
    warning: (message: string, duration?: number) => number;
    info: (message: string, duration?: number) => number;
  };
  removeToast: (id: number) => void;
}


// ===============================================================
// THE CUSTOM HOOK
// ===============================================================

export function useToast(): UseToastReturn {
  const [toasts, setToasts] = useState<ToastData[]>([]);

  const removeToast = useCallback((id: number) => {
    setToasts((prevToasts) => prevToasts.filter((toast) => toast.id !== id));
  }, []);

  const addToast = useCallback(
      (message: string, type: ToastType = 'info', duration: number = 5000): number => {
        const id = Date.now() + Math.random();

        // FIX 2: Create the new toast object with the correct type before setting state.
        // This helps TypeScript understand that the object conforms to the ToastData interface.
        const newToast: ToastData = {id, message, type};

        setToasts((prevToasts) => [...prevToasts, newToast]);

        if (duration > 0) {
          // Use a stable reference to removeToast inside the timeout
          setTimeout(() => {
            setToasts((currentToasts) => currentToasts.filter((t) => t.id !== id));
          }, duration);
        }

        return id;
      },
      [] // removeToast is not needed as a dependency if we update state functionally like above
  );

  const toast = {
    success: (message: string, duration?: number) => addToast(message, 'success', duration),
    error: (message: string, duration?: number) => addToast(message, 'error', duration),
    warning: (message: string, duration?: number) => addToast(message, 'warning', duration),
    info: (message: string, duration?: number) => addToast(message, 'info', duration),
  };

  return {toasts, toast, removeToast};
}
