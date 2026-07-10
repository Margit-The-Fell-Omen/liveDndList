import {createContext, type ReactNode, useCallback, useContext, useEffect, useState} from 'react';
import {charactersApi, equipmentApi, referenceDataApi} from '@/services/api';
import {useAuth} from './AuthContext';
import type {
  AbilityType,
  Background,
  Character,
  CharacterClass,
  CharacterContextType,
  CharacterCreateRequest,
  CharacterSummary,
  CharacterUpdateRequest,
  EquipmentData,
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
  const [backgrounds, setBackgrounds] = useState<Background[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchReferenceData = useCallback(async (): Promise<void> => {
    try {
      const [racesData, classesData, backgroundsData] = await Promise.all([
        referenceDataApi.getRaces(),
        referenceDataApi.getClasses(),
        referenceDataApi.getBackgrounds(),
      ]);
      setRaces(racesData);
      setClasses(classesData);
      setBackgrounds(backgroundsData);
    } catch (err) {
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
        await Promise.all([fetchReferenceData(), fetchCharacters()]);
        setLoading(false);
      };
      loadData();
    } else {
      setCharacters([]);
      setCurrentCharacter(null);
      setRaces([]);
      setClasses([]);
      setBackgrounds([]);
      setLoading(false);
    }
  }, [isAuthenticated, fetchReferenceData, fetchCharacters]);

  const createCharacter = async (data: CharacterCreateRequest): Promise<Character> => {
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

  const setAndMergeCurrentCharacter = (updatedChar: Partial<Character>) => {
    setCurrentCharacter(prev => {
      if (!prev) return updatedChar as Character;
      return {
        ...prev,
        ...updatedChar,
        skills: updatedChar.skills ?? prev.skills,
        equipment: updatedChar.equipment ?? prev.equipment,
        spells: updatedChar.spells ?? prev.spells,
        savingThrowProficiencies: updatedChar.savingThrowProficiencies ?? prev.savingThrowProficiencies,
        classesInfo: updatedChar.classesInfo ?? prev.classesInfo,
      };
    });

    setCharacters(prev =>
        prev.map(summary =>
            summary.id === updatedChar.id
                ? {
                  ...summary,
                  name: updatedChar.name ?? summary.name,
                  totalLevel: updatedChar.totalLevel ?? summary.totalLevel,
                  currentHitPoints: updatedChar.currentHitPoints ?? summary.currentHitPoints,
                  maxHitPoints: updatedChar.maxHitPoints ?? summary.maxHitPoints,
                  portraitUrl: updatedChar.portraitUrl ?? summary.portraitUrl,
                  updatedAt: updatedChar.updatedAt ?? summary.updatedAt,
                }
                : summary,
        ),
    );
  };

  const updateCharacter = async (id: number, data: CharacterUpdateRequest): Promise<Character> => {
    setSaving(true);
    try {
      const updatedChar = await charactersApi.update(id, data);
      setAndMergeCurrentCharacter(updatedChar);
      return updatedChar;
    } finally {
      setSaving(false);
    }
  };

  const deleteCharacter = async (id: number): Promise<void> => {
    await charactersApi.delete(id);
    if (currentCharacter?.id === id) setCurrentCharacter(null);
    await fetchCharacters();
  };

  const addEquipment = async (data: EquipmentData): Promise<void> => {
    if (!currentCharacter) throw new Error('No character selected.');
    setSaving(true);
    try {
      const updatedChar = await charactersApi.addEquipment(currentCharacter.id, data);
      setAndMergeCurrentCharacter(updatedChar);
    } finally {
      setSaving(false);
    }
  };

  const removeEquipment = async (itemId: number): Promise<void> => {
    if (!currentCharacter) throw new Error('No character selected.');
    setSaving(true);
    try {
      const updatedChar = await charactersApi.removeEquipment(currentCharacter.id, itemId);
      setAndMergeCurrentCharacter(updatedChar);
    } finally {
      setSaving(false);
    }
  };

  const updateEquipment = async (itemId: number, data: EquipmentData): Promise<void> => {
    if (!currentCharacter) throw new Error('No character selected.');
    setSaving(true);
    try {
      const updatedItem = await equipmentApi.update(itemId, data);
      setCurrentCharacter(prev => {
        if (!prev) return null;
        return {
          ...prev,
          equipment: prev.equipment.map(item => (item.id === itemId ? updatedItem : item))
        };
      });
    } finally {
      setSaving(false);
    }
  };

  const toggleEquipmentEquipped = async (itemId: number): Promise<void> => {
    if (!currentCharacter) throw new Error('No character selected.');
    const item = currentCharacter.equipment.find(i => i.id === itemId);
    if (!item) throw new Error('Equipment item not found.');
    await updateEquipment(itemId, {
      name: item.name,
      description: item.description,
      quantity: item.quantity,
      weight: item.weight,
      type: item.type,
      equipped: !item.equipped,
      damage: item.damage,
      damageType: item.damageType,
      properties: item.properties,
    });
  };

  const addSpellToCharacter = async (spellId: number): Promise<void> => {
    if (!currentCharacter) throw new Error('No character selected.');
    setSaving(true);
    try {
      await charactersApi.addSpell(currentCharacter.id, spellId);
      const fresh = await charactersApi.getById(currentCharacter.id);
      setAndMergeCurrentCharacter(fresh);
    } finally {
      setSaving(false);
    }
  };

  const removeSpellFromCharacter = async (spellId: number): Promise<void> => {
    if (!currentCharacter) throw new Error('No character selected.');
    setSaving(true);
    try {
      await charactersApi.removeSpell(currentCharacter.id, spellId);
      const fresh = await charactersApi.getById(currentCharacter.id);
      setAndMergeCurrentCharacter(fresh);
    } finally {
      setSaving(false);
    }
  };

  const toggleSavingThrowProficiency = async (ability: AbilityType): Promise<void> => {
    if (!currentCharacter) return;
    const current = currentCharacter.savingThrowProficiencies ?? [];
    const updated = current.includes(ability)
        ? current.filter(p => p !== ability)
        : [...current, ability];
    await updateCharacter(currentCharacter.id, {savingThrowProficiencies: updated});
  };

  const toggleSkillProficiency = async (skillId: number, isNowProficient: boolean): Promise<void> => {
    if (!currentCharacter) return;
    await updateCharacter(currentCharacter.id, {
      skills: [{
        id: skillId,
        proficient: isNowProficient
      }]
    });
  };

  const toggleSkillExpertise = async (skillId: number, isNowExpert: boolean): Promise<void> => {
    if (!currentCharacter) return;
    await updateCharacter(currentCharacter.id, {skills: [{id: skillId, expertise: isNowExpert}]});
  };

  const value: CharacterContextType = {
    characters,
    currentCharacter,
    loading,
    saving,
    error,
    races,
    classes,
    backgrounds,
    fetchCharacters,
    fetchReferenceData,
    selectCharacter,
    createCharacter,
    updateCharacter,
    deleteCharacter,
    clearError: () => setError(null),
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

export function useCharacter(): CharacterContextType {
  const context = useContext(CharacterContext);
  if (!context) throw new Error('useCharacter must be used within a CharacterProvider');
  return context;
}
