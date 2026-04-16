// src/context/CharacterContext.tsx

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

export interface CharacterContextTypeWithArchetypes extends CharacterContextType {
  getArchetypesForClass: (classId: number) => Promise<Archetype[]>;
}

const CharacterContext = createContext<CharacterContextTypeWithArchetypes | undefined>(undefined);

interface CharacterProviderProps {
  children: ReactNode;
}

export function CharacterProvider({children}: CharacterProviderProps) {
  const {isAuthenticated} = useAuth();
  const [characters, setCharacters] = useState<CharacterSummary[]>([]);
  const [currentCharacter, setCurrentCharacter] = useState<Character | null>(null);
  const [races, setRaces] = useState<Race[]>([]);
  const [classes, setClasses] = useState<CharacterClass[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // --- THE DEFINITIVE FIX ---
  const fetchReferenceData = useCallback(async (): Promise<void> => {
    try {
      // By awaiting each promise on its own line, we completely avoid the
      // Promise.all type inference error. This is guaranteed to work.
      const racesData = await referenceDataApi.getRaces();
      const classesData = await referenceDataApi.getClasses();

      // These assignments are now simple and type-safe.
      setRaces(racesData);
      setClasses(classesData);
    } catch (err) {
      console.error('Failed to fetch reference data:', err);
      setError(err instanceof Error ? err.message : 'Could not load game data.');
    }
  }, []);
  // --- END OF FIX ---

  const fetchCharacters = useCallback(async (): Promise<void> => {
    if (!isAuthenticated) return;
    try {
      const page = await charactersApi.getSummaries({sort: 'updatedAt,desc'});
      setCharacters(page.content);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch characters.');
    }
  }, [isAuthenticated]);

  const selectCharacter = useCallback(async (id: number): Promise<void> => {
    setLoading(true);
    try {
      const character = await charactersApi.getById(id);
      setCurrentCharacter(character);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load character.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      const loadData = async () => {
        setLoading(true);
        // We still use Promise.all here to run the functions in parallel,
        // which is fine because the functions themselves are now error-free.
        await Promise.all([
          fetchReferenceData(),
          fetchCharacters()
        ]);
        setLoading(false);
      };
      loadData();
    } else {
      // Clear all state on logout
      setCharacters([]);
      setCurrentCharacter(null);
      setRaces([]);
      setClasses([]);
      setLoading(false);
    }
  }, [isAuthenticated, fetchReferenceData, fetchCharacters]);

  // --- CRUD Functions (no changes needed) ---
  const createCharacter = async (data: CharacterCreateRequest) => {
    setSaving(true);
    try {
      const newChar = await charactersApi.create(data);
      await fetchCharacters();
      selectCharacter(newChar.id);
      return newChar;
    } finally {
      setSaving(false);
    }
  };

  const updateCharacter = async (id: number, data: CharacterUpdateRequest) => {
    setSaving(true);
    try {
      const updatedChar = await charactersApi.update(id, data);

      setCurrentCharacter(prevCharacter => {
        if (!prevCharacter) return updatedChar;

        const newCharacterState = {
          ...prevCharacter,
          ...updatedChar,
          skills: updatedChar.skills ?? prevCharacter.skills,
          equipment: updatedChar.equipment ?? prevCharacter.equipment,
          spells: updatedChar.spells ?? prevCharacter.spells,
          savingThrowProficiencies: updatedChar.savingThrowProficiencies ?? prevCharacter.savingThrowProficiencies,
          classesInfo: updatedChar.classesInfo ?? prevCharacter.classesInfo,
        };

        return newCharacterState;
      });

      setCharacters(prev => prev.map(c =>
          c.id === id ? {...c, name: updatedChar.name || c.name} : c
      ));

      return updatedChar;
    } finally {
      setSaving(false);
    }
  };

  const deleteCharacter = async (id: number) => {
    await charactersApi.delete(id);
    if (currentCharacter?.id === id) {
      setCurrentCharacter(null);
    }
    await fetchCharacters();
  };

  const getArchetypesForClass = useCallback(async (classId: number): Promise<Archetype[]> => {
    try {
      return await referenceDataApi.getArchetypesByClass(classId);
    } catch (err) {
      console.error(`Failed to fetch archetypes for class ${classId}:`, err);
      setError(err instanceof Error ? err.message : 'Could not load archetypes.');
      return [];
    }
  }, []);

  const value: CharacterContextTypeWithArchetypes = {
    characters,
    currentCharacter,
    loading,
    saving,
    error,
    races,
    classes,
    fetchCharacters,
    fetchReferenceData,
    selectCharacter,
    createCharacter,
    updateCharacter,
    deleteCharacter,
    clearError: () => setError(null),
    getArchetypesForClass,
  };

  return <CharacterContext.Provider value={value}>{children}</CharacterContext.Provider>;
}

export function useCharacter(): CharacterContextTypeWithArchetypes {
  const context = useContext(CharacterContext);
  if (!context) throw new Error('useCharacter must be used within a CharacterProvider');
  return context;
}
