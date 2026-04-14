// src/hooks/useLocalStorage.ts

import {useEffect, useState} from 'react';

/**
 * A custom hook to manage state in local storage.
 * It syncs with local storage and provides a setter function like `useState`.
 *
 * @param key The key to use in local storage.
 * @param initialValue The initial value to use if nothing is in local storage.
 * @returns A stateful value and a function to update it.
 */
export function useLocalStorage<T>(key: string, initialValue: T): [T, (value: T | ((val: T) => T)) => void] {
  // Get initial value from local storage or use the provided initial value
  const [storedValue, setStoredValue] = useState<T>(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error(error);
      return initialValue;
    }
  });

  // useEffect to update local storage when the state changes
  useEffect(() => {
    try {
      const valueToStore = storedValue instanceof Function ? storedValue(storedValue) : storedValue;
      window.localStorage.setItem(key, JSON.stringify(valueToStore));
    } catch (error) {
      console.error(error);
    }
  }, [key, storedValue]);

  return [storedValue, setStoredValue];
}
