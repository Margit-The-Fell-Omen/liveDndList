import {createContext, type ReactNode, useCallback, useContext, useEffect, useState} from 'react';
import {charactersApi, referenceDataApi} from '@/services/api';
import {useAuth} from './AuthContext';
import type {
  Archetype,
  Character,
  CharacterClass,
  CharacterContextType,
  CharacterCreateRequest,
  CharacterUpdateRequest,
  Race,
} from '@/types';

const CharacterContext = createContext<CharacterContextType | undefined>(undefined);

interface CharacterProviderProps {
  children: ReactNode;
}

export function CharacterProvider({children}: CharacterProviderProps) {
  const {isAuthenticated} = useAuth();

  // Characters
  const [characters, setCharacters] = useState<Character[]>([]);
  const [currentCharacter, setCurrentCharacter] = useState<Character | null>(null);

  // Reference data
  const [races, setRaces] = useState<Race[]>([]);
  const [classes, setClasses] = useState<CharacterClass[]>([]);
  const [archetypes, setArchetypes] = useState<Archetype[]>([]);

  // State
  const [loading, setLoading] = useState<boolean>(false);
  const [saving, setSaving] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch reference data and characters when authenticated
  useEffect(() => {
    if (isAuthenticated) {
      fetchReferenceData();
      fetchCharacters();
    } else {
      setCharacters([]);
      setCurrentCharacter(null);
      setRaces([]);
      setClasses([]);
      setArchetypes([]);
    }
  }, [isAuthenticated]);

  const fetchReferenceData = useCallback(async (): Promise<void> => {
    try {
      const [racesData, classesData, archetypesData] = await Promise.all([
        referenceDataApi.getRaces(),
        referenceDataApi.getClasses(),
        referenceDataApi.getArchetypes(),
      ]);

      setRaces(racesData);
      setClasses(classesData);
      setArchetypes(archetypesData);

      console.log('Reference data loaded:', {
        races: racesData.length,
        classes: classesData.length,
        archetypes: archetypesData.length,
      });
    } catch (err) {
      console.error('Failed to fetch reference data:', err);
      // Don't set error state for reference data - it's not critical
    }
  }, []);

  const fetchCharacters = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const data = await charactersApi.getAll();
      setCharacters(data);

      // Select first character if available
      if (data.length > 0 && !currentCharacter) {
        setCurrentCharacter(data[0]);
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to fetch characters';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [currentCharacter]);

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

  const createCharacter = useCallback(async (data: CharacterCreateRequest): Promise<Character> => {
    setSaving(true);
    setError(null);
    try {
      const newCharacter = await charactersApi.create(data);
      setCharacters((prev) => [...prev, newCharacter]);
      setCurrentCharacter(newCharacter);
      return newCharacter;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to create character';
      setError(message);
      throw err;
    } finally {
      setSaving(false);
    }
  }, []);

  const updateCharacter = useCallback(async (id: number, data: CharacterUpdateRequest): Promise<Character> => {
    setSaving(true);
    setError(null);
    try {
      const updated = await charactersApi.update(id, data);
      setCharacters((prev) => prev.map((c) => (c.id === id ? updated : c)));
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
  }, [currentCharacter]);

  const deleteCharacter = useCallback(async (id: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await charactersApi.delete(id);
      setCharacters((prev) => prev.filter((c) => c.id !== id));

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
  }, [currentCharacter, characters]);

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
