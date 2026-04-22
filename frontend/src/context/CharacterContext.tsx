// src/context/CharacterContext.tsx

import {createContext, type ReactNode, useCallback, useContext, useEffect, useState} from 'react';
import {charactersApi, equipmentApi, referenceDataApi} from '@/services/api';
import {useAuth} from './AuthContext';
import type {
  AbilityName,
  Archetype,
  Character,
  CharacterClass,
  CharacterContextType,
  CharacterCreateRequest,
  CharacterSummary,
  CharacterUpdateRequest,
  EquipmentData,
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

  const fetchReferenceData = useCallback(async (): Promise<void> => {
    try {
      const racesData = await referenceDataApi.getRaces();
      const classesData = await referenceDataApi.getClasses();
      setRaces(racesData);
      setClasses(classesData);
    } catch (err) {
      console.error('Failed to fetch reference data:', err);
      setError(err instanceof Error ? err.message : 'Could not load game data.');
    }
  }, []);

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
        await Promise.all([
          fetchReferenceData(),
          fetchCharacters()
        ]);
        setLoading(false);
      };
      loadData();
    } else {
      setCharacters([]);
      setCurrentCharacter(null);
      setRaces([]);
      setClasses([]);
      setLoading(false);
    }
  }, [isAuthenticated, fetchReferenceData, fetchCharacters]);

  const createCharacter = async (data: CharacterCreateRequest) => {
    setSaving(true);
    try {
      const newChar = await charactersApi.create(data);
      await fetchCharacters();
      await selectCharacter(newChar.id);
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
        return {
          ...prevCharacter,
          ...updatedChar,
          skills: updatedChar.skills ?? prevCharacter.skills,
          equipment: updatedChar.equipment ?? prevCharacter.equipment,
          spells: updatedChar.spells ?? prevCharacter.spells,
          savingThrowProficiencies: updatedChar.savingThrowProficiencies ?? prevCharacter.savingThrowProficiencies,
          classesInfo: updatedChar.classesInfo ?? prevCharacter.classesInfo,
        };
      });

      setCharacters(prev =>
          prev.map(summary => {
            if (summary.id === id) {
              return {
                ...summary,
                name: updatedChar.name,
                totalLevel: updatedChar.totalLevel,
                currentHitPoints: updatedChar.currentHitPoints,
                maxHitPoints: updatedChar.maxHitPoints,
                portraitUrl: updatedChar.portraitUrl,
                updatedAt: updatedChar.updatedAt,
              };
            }
            return summary;
          })
      );

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

  const addEquipment = async (data: EquipmentData): Promise<void> => {
    if (!currentCharacter) throw new Error("No character selected.");
    setSaving(true);
    try {
      const updatedChar = await charactersApi.addEquipment(currentCharacter.id, data);
      setCurrentCharacter(updatedChar);
    } finally {
      setSaving(false);
    }
  };

  const removeEquipment = async (itemId: number): Promise<void> => {
    if (!currentCharacter) throw new Error("No character selected.");
    setSaving(true);
    try {
      const updatedChar = await charactersApi.removeEquipment(currentCharacter.id, itemId);
      setCurrentCharacter(updatedChar);
    } finally {
      setSaving(false);
    }
  };

  const updateEquipment = async (itemId: number, data: EquipmentData): Promise<void> => {
    if (!currentCharacter) throw new Error("No character selected.");
    setSaving(true);
    try {
      const updatedItem = await equipmentApi.update(itemId, data);

      setCurrentCharacter(prevChar => {
        if (!prevChar) return null;

        const newEquipmentList = prevChar.equipment.map(item =>
            item.id === itemId ? updatedItem : item
        );

        return {...prevChar, equipment: newEquipmentList};
      });
    } finally {
      setSaving(false);
    }
  };


  const toggleEquipmentEquipped = async (itemId: number): Promise<void> => {
    if (!currentCharacter) throw new Error("No character selected.");

    const itemToToggle = currentCharacter.equipment.find(item => item.id === itemId);
    if (!itemToToggle) throw new Error("Equipment item not found.");

    const updatedItemData: EquipmentData = {
      name: itemToToggle.name,
      description: itemToToggle.description,
      quantity: itemToToggle.quantity,
      weight: itemToToggle.weight,
      type: itemToToggle.type,
      equipped: !itemToToggle.equipped,
      damage: itemToToggle.damage,
      damageType: itemToToggle.damageType,
      properties: itemToToggle.properties,
    };

    await updateEquipment(itemId, updatedItemData);
  };

  const addSpellToCharacter = async (spellId: number): Promise<void> => {
    if (!currentCharacter) throw new Error("No character selected.");
    setSaving(true);
    try {
      await charactersApi.addSpell(currentCharacter.id, spellId);

      const freshCharacter = await charactersApi.getById(currentCharacter.id);
      setCurrentCharacter(freshCharacter);

    } finally {
      setSaving(false);
    }
  };

  const removeSpellFromCharacter = async (spellId: number): Promise<void> => {
    if (!currentCharacter) throw new Error("No character selected.");
    setSaving(true);
    try {
      await charactersApi.removeSpell(currentCharacter.id, spellId);

      const freshCharacter = await charactersApi.getById(currentCharacter.id);
      setCurrentCharacter(freshCharacter);
    } finally {
      setSaving(false);
    }
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

  const toggleSavingThrowProficiency = async (ability: AbilityName) => {
    if (!currentCharacter) return;

    const currentProfs = currentCharacter.savingThrowProficiencies || [];
    const newProfs = currentProfs.includes(ability)
        ? currentProfs.filter(p => p !== ability) // Remove it
        : [...currentProfs, ability]; // Add it

    await updateCharacter(currentCharacter.id, {savingThrowProficiencies: newProfs});
  };

  const toggleSkillProficiency = async (skillId: number, isNowProficient: boolean) => {
    if (!currentCharacter) return;
    await updateCharacter(currentCharacter.id, {
      skills: [{id: skillId, proficient: isNowProficient}],
    });
  };

  const toggleSkillExpertise = async (skillId: number, isNowExpert: boolean) => {
    if (!currentCharacter) return;
    await updateCharacter(currentCharacter.id, {
      skills: [{id: skillId, expertise: isNowExpert}],
    });
  };

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
    addEquipment,
    updateEquipment,
    toggleEquipmentEquipped,
    removeEquipment,
    addSpellToCharacter,
    removeSpellFromCharacter,
    toggleSavingThrowProficiency,
    toggleSkillProficiency,
    toggleSkillExpertise,
  };

  return <CharacterContext.Provider value={value}>{children}</CharacterContext.Provider>;
}

export function useCharacter(): CharacterContextTypeWithArchetypes {
  const context = useContext(CharacterContext);
  if (!context) throw new Error('useCharacter must be used within a CharacterProvider');
  return context;
}
