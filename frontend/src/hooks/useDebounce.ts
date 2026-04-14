// src/hooks/useDebounce.ts

import {useCallback, useEffect, useRef, useState} from 'react';

/**
 * A custom hook that debounces a value.
 * @param value The value to debounce.
 * @param delay The debounce delay in milliseconds.
 * @returns The debounced value.
 */
export function useDebounce<T>(value: T, delay: number = 500): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
}


// =================================================================
// DEBOUNCED CALLBACK HOOK (FINAL CORRECTED VERSION)
// =================================================================
/**
 * A custom hook that returns a debounced version of a callback function.
 * This version is designed to be highly type-safe and avoid issues with stale closures.
 *
 * @param callback The function to debounce.
 * @param delay The debounce delay in milliseconds.
 * @returns A memoized, debounced version of the callback.
 */
export function useDebouncedCallback<A extends any[], R>(
    callback: (...args: A) => R,
    delay: number = 500
): (...args: A) => void {

  const timeoutRef = useRef<ReturnType<typeof setTimeout>>();

  // Cleanup effect for when the component unmounts
  useEffect(() => () => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
  }, []);

  return useCallback(
      (...args: A) => {
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current);
        }

        timeoutRef.current = setTimeout(() => {
          callback(...args);
        }, delay);
      },
      [callback, delay] // The callback and delay are dependencies
  );
}