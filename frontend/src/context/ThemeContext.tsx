// src/context/ThemeContext.tsx

import {createContext, type ReactNode, useContext, useEffect} from 'react';
import {useLocalStorage} from '@/hooks/useLocalStorage';

// ===============================================================
// LOCAL TYPE DEFINITIONS
// ===============================================================

export type Theme = 'light' | 'dark' | 'system';

export interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  toggleTheme: () => void;
}


// ===============================================================
// CONTEXT PROVIDER
// ===============================================================

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

interface ThemeProviderProps {
  children: ReactNode;
}

export function ThemeProvider({children}: ThemeProviderProps) {
  // useLocalStorage returns a stateful value and a setter, just like useState
  const [theme, setTheme] = useLocalStorage<Theme>('theme', 'system');

  useEffect(() => {
    const root = window.document.documentElement;

    const handleSystemThemeChange = (e: MediaQueryListEvent) => {
      // Only update if the current theme is 'system'
      if (theme === 'system') {
        root.setAttribute('data-theme', e.matches ? 'dark' : 'light');
      }
    };

    // Set the initial theme
    if (theme === 'system') {
      const systemIsDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      root.setAttribute('data-theme', systemIsDark ? 'dark' : 'light');
    } else {
      root.setAttribute('data-theme', theme);
    }

    // Add a listener for when the user changes their OS theme preference
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    mediaQuery.addEventListener('change', handleSystemThemeChange);

    // Cleanup the listener when the component unmounts or theme changes
    return () => {
      mediaQuery.removeEventListener('change', handleSystemThemeChange);
    };
  }, [theme]);

  const toggleTheme = () => {
    // Cycle through: light -> dark -> system -> light
    setTheme((prevTheme) => {
      if (prevTheme === 'light') return 'dark';
      if (prevTheme === 'dark') return 'system';
      return 'light'; // from 'system' or any other state
    });
  };

  const value: ThemeContextType = {
    theme,
    // The setter from useLocalStorage might have a complex type, so we wrap it
    // to ensure it matches our simple ThemeContextType.
    setTheme: (newTheme: Theme) => setTheme(newTheme),
    toggleTheme,
  };

  return (
      <ThemeContext.Provider value={value}>
        {children}
      </ThemeContext.Provider>
  );
}


// ===============================================================
// CUSTOM HOOK
// ===============================================================

export function useTheme(): ThemeContextType {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}
