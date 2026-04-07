import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react';
import { charactersApi } from '@/services/api';
import { useAuth } from './AuthContext';
import { useDebounce } from '@/hooks/useDebounce';
import { EMPTY_CHARACTER } from '@/utils/constants';
import type { CharacterContextType, Character } from '@/types';

const CharacterContext = createContext<CharacterContextType | undefined>(undefined);

interface CharacterProviderProps {
  children: ReactNode;
}

export function CharacterProvider({ children }: CharacterProviderProps) {
  const { isAuthenticated } = useAuth();
  const [characters, setCharacters] = useState<Character[]>([]);
  const [currentCharacter, setCurrentCharacter] = useState<Character | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [saving, setSaving] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState<boolean>(false);

  // Debounced character for auto-save
  const debouncedCharacter = useDebounce(currentCharacter, 2000);

  // Fetch all characters when authenticated
  useEffect(() => {
    if (isAuthenticated) {
      fetchCharacters();
    } else {
      setCharacters([]);
      setCurrentCharacter(null);
    }
  }, [isAuthenticated]);

  // Auto-save when character changes (debounced)
  useEffect(() => {
    if (debouncedCharacter?.id && hasUnsavedChanges) {
      saveCharacter(debouncedCharacter);
    }
  }, [debouncedCharacter, hasUnsavedChanges]);

  const fetchCharacters = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const data = await charactersApi.getAll();
      setCharacters(data);

      // Load last edited character if available
      if (data.length > 0) {
        try {
          const lastEdited = await charactersApi.getLastEdited();
          setCurrentCharacter(lastEdited);
        } catch {
          // If no last edited, load first character
          setCurrentCharacter(data[0]);
        }
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to fetch characters';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  const selectCharacter = useCallback(async (id: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const character = await charactersApi.getById(id);
      setCurrentCharacter(character);
      setHasUnsavedChanges(false);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load character';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  const createCharacter = useCallback(async (name: string = 'New Character'): Promise<Character> => {
    setLoading(true);
    setError(null);
    try {
      const newCharacter = await charactersApi.create({
        ...EMPTY_CHARACTER,
        name,
      });
      setCharacters((prev) => [...prev, newCharacter]);
      setCurrentCharacter(newCharacter);
      setHasUnsavedChanges(false);
      return newCharacter;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to create character';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const updateCharacter = useCallback((updates: Partial<Character>): void => {
    setCurrentCharacter((prev) => {
      if (!prev) return prev;
      return { ...prev, ...updates };
    });
    setHasUnsavedChanges(true);
  }, []);

  const updateNestedCharacter = useCallback((path: string, value: unknown): void => {
    setCurrentCharacter((prev) => {
      if (!prev) return prev;

      const keys = path.split('.');
      const newCharacter = { ...prev } as Record<string, unknown>;
      let current: Record<string, unknown> = newCharacter;

      for (let i = 0; i < keys.length - 1; i++) {
        current[keys[i]] = { ...(current[keys[i]] as Record<string, unknown>) };
        current = current[keys[i]] as Record<string, unknown>;
      }

      current[keys[keys.length - 1]] = value;
      return newCharacter as Character;
    });
    setHasUnsavedChanges(true);
  }, []);

  const saveCharacter = useCallback(
    async (characterToSave: Character | null = currentCharacter): Promise<Character | undefined> => {
      if (!characterToSave?.id) return;

      setSaving(true);
      setError(null);
      try {
        const saved = await charactersApi.update(characterToSave.id, characterToSave);
        setCharacters((prev) => prev.map((c) => (c.id === saved.id ? saved : c)));
        setCurrentCharacter(saved);
        setHasUnsavedChanges(false);
        return saved;
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Failed to save character';
        setError(message);
        throw err;
      } finally {
        setSaving(false);
      }
    },
    [currentCharacter]
  );

  const deleteCharacter = useCallback(
    async (id: number): Promise<void> => {
      setLoading(true);
      setError(null);
      try {
        await charactersApi.delete(id);
        setCharacters((prev) => prev.filter((c) => c.id !== id));

        // If deleted character was current, switch to another or null
        if (currentCharacter?.id === id) {
          const remaining = characters.filter((c) => c.id !== id);
          setCurrentCharacter(remaining.length > 0 ? remaining[0] : null);
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Failed to delete character';
        setError(message);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [currentCharacter, characters]
  );

  const duplicateCharacter = useCallback(async (id: number): Promise<Character> => {
    setLoading(true);
    setError(null);
    try {
      const duplicated = await charactersApi.duplicate(id);
      setCharacters((prev) => [...prev, duplicated]);
      setCurrentCharacter(duplicated);
      return duplicated;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to duplicate character';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const value: CharacterContextType = {
    characters,
    currentCharacter,
    loading,
    saving,
    error,
    hasUnsavedChanges,
    fetchCharacters,
    selectCharacter,
    createCharacter,
    updateCharacter,
    updateNestedCharacter,
    saveCharacter,
    deleteCharacter,
    duplicateCharacter,
    clearError: () => setError(null),
  };

  return <CharacterContext.Provider value={value}>{children}</CharacterContext.Provider>;
}

export function useCharacter(): CharacterContextType {
  const context = useContext(CharacterContext);
  if (!context) {
    throw new Error('useCharacter must be used within a CharacterProvider');
  }
  return context;
}
