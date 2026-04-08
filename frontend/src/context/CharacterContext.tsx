import {createContext, type ReactNode, useCallback, useContext, useEffect, useState} from 'react';
import {charactersApi, referenceDataApi} from '@/services/api';
import {useAuth} from './AuthContext';
import type {
  Archetype,
  Character,
  CharacterClass,
  CharacterContextType,
  CharacterCreateRequest,
  CharacterSummary,
  CharacterUpdateRequest,
  Race,
} from '@/types';

const CharacterContext = createContext<CharacterContextType | undefined>(undefined);

interface CharacterProviderProps {
  children: ReactNode;
}

export function CharacterProvider({children}: CharacterProviderProps) {
  const {isAuthenticated} = useAuth();

  const [characters, setCharacters] = useState<CharacterSummary[]>([]);
  const [currentCharacter, setCurrentCharacter] = useState<Character | null>(null);

  const [races, setRaces] = useState<Race[]>([]);
  const [classes, setClasses] = useState<CharacterClass[]>([]);
  const [archetypes, setArchetypes] = useState<Archetype[]>([]);

  const [loading, setLoading] = useState<boolean>(false);
  const [saving, setSaving] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // FIX: This useCallback has no external dependencies. The array should be empty.
  const fetchReferenceData = useCallback(async (): Promise<void> => {
    try {
      // FIX: Use explicit tuple typing for the result of Promise.all. This is the most robust way to solve the type error.
      const [racesData, classesData, archetypesData]: [Race[], CharacterClass[], Archetype[]] = await Promise.all([
        referenceDataApi.getRaces(),
        referenceDataApi.getClasses(),
        referenceDataApi.getArchetypes(),
      ]);

      setRaces(racesData);
      setClasses(classesData);
      setArchetypes(archetypesData);
    } catch (err) {
      console.error('Failed to fetch reference data:', err);
    }
  }, []);

  // FIX: This useCallback should not depend on `currentCharacter`. Its only job is to fetch the list.
  const fetchCharacters = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const page = await charactersApi.getSummaries({sort: 'updatedAt,desc'});
      setCharacters(page.content);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to fetch characters';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  // FIX: The main useEffect hook now correctly includes the functions it calls in its dependency array.
  useEffect(() => {
    if (isAuthenticated) {
      fetchReferenceData();
      fetchCharacters();
    } else {
      // Clear all state on logout
      setCharacters([]);
      setCurrentCharacter(null);
      setRaces([]);
      setClasses([]);
      setArchetypes([]);
    }
  }, [isAuthenticated, fetchCharacters, fetchReferenceData]);


  const selectCharacter = useCallback(async (id: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const character = await charactersApi.getById(id);
      setCurrentCharacter(character);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load character';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  // FIX: `createCharacter` and other mutation functions must depend on `fetchCharacters` since they call it.
  const createCharacter = useCallback(async (data: CharacterCreateRequest): Promise<Character> => {
    setSaving(true);
    setError(null);
    try {
      const newCharacter = await charactersApi.create(data);
      await fetchCharacters(); // Re-fetch the list
      setCurrentCharacter(newCharacter);
      return newCharacter;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to create character';
      setError(message);
      throw err;
    } finally {
      setSaving(false);
    }
  }, [fetchCharacters]);

  const updateCharacter = useCallback(async (id: number, data: CharacterUpdateRequest): Promise<Character> => {
    setSaving(true);
    setError(null);
    try {
      const updated = await charactersApi.update(id, data);
      await fetchCharacters();
      if (currentCharacter?.id === id) {
        setCurrentCharacter(updated);
      }
      return updated;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to update character';
      setError(message);
      throw err;
    } finally {
      setSaving(false);
    }
  }, [currentCharacter, fetchCharacters]);

  const deleteCharacter = useCallback(async (id: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await charactersApi.delete(id);
      if (currentCharacter?.id === id) {
        setCurrentCharacter(null);
      }
      await fetchCharacters();
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to delete character';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [currentCharacter, fetchCharacters]);

  const value: CharacterContextType = {
    characters,
    currentCharacter,
    loading,
    saving,
    error,
    races,
    classes,
    archetypes,
    fetchCharacters,
    fetchReferenceData,
    selectCharacter,
    createCharacter,
    updateCharacter,
    deleteCharacter,
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
