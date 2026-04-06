import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { charactersApi } from '../services/api';
import { useAuth } from './AuthContext';
import { useDebounce } from '../hooks/useDebounce';
import { EMPTY_CHARACTER } from '../utils/constants';

const CharacterContext = createContext();

export function CharacterProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [characters, setCharacters] = useState([]);
  const [currentCharacter, setCurrentCharacter] = useState(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

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
  }, [debouncedCharacter]);

  const fetchCharacters = useCallback(async () => {
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
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const selectCharacter = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      const character = await charactersApi.getById(id);
      setCurrentCharacter(character);
      setHasUnsavedChanges(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const createCharacter = useCallback(async (name = 'New Character') => {
    setLoading(true);
    setError(null);
    try {
      const newCharacter = await charactersApi.create({
        ...EMPTY_CHARACTER,
        name,
      });
      setCharacters(prev => [...prev, newCharacter]);
      setCurrentCharacter(newCharacter);
      setHasUnsavedChanges(false);
      return newCharacter;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const updateCharacter = useCallback((updates) => {
    setCurrentCharacter(prev => {
      if (!prev) return prev;
      return { ...prev, ...updates };
    });
    setHasUnsavedChanges(true);
  }, []);

  const updateNestedCharacter = useCallback((path, value) => {
    setCurrentCharacter(prev => {
      if (!prev) return prev;
      
      const keys = path.split('.');
      const newCharacter = { ...prev };
      let current = newCharacter;
      
      for (let i = 0; i < keys.length - 1; i++) {
        current[keys[i]] = { ...current[keys[i]] };
        current = current[keys[i]];
      }
      
      current[keys[keys.length - 1]] = value;
      return newCharacter;
    });
    setHasUnsavedChanges(true);
  }, []);

  const saveCharacter = useCallback(async (characterToSave = currentCharacter) => {
    if (!characterToSave?.id) return;
    
    setSaving(true);
    setError(null);
    try {
      const saved = await charactersApi.update(characterToSave.id, characterToSave);
      setCharacters(prev => 
        prev.map(c => c.id === saved.id ? saved : c)
      );
      setCurrentCharacter(saved);
      setHasUnsavedChanges(false);
      return saved;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setSaving(false);
    }
  }, [currentCharacter]);

  const deleteCharacter = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      await charactersApi.delete(id);
      setCharacters(prev => prev.filter(c => c.id !== id));
      
      // If deleted character was current, switch to another or null
      if (currentCharacter?.id === id) {
        const remaining = characters.filter(c => c.id !== id);
        setCurrentCharacter(remaining.length > 0 ? remaining[0] : null);
      }
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [currentCharacter, characters]);

  const duplicateCharacter = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      const duplicated = await charactersApi.duplicate(id);
      setCharacters(prev => [...prev, duplicated]);
      setCurrentCharacter(duplicated);
      return duplicated;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const value = {
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

  return (
    <CharacterContext.Provider value={value}>
      {children}
    </CharacterContext.Provider>
  );
}

export function useCharacter() {
  const context = useContext(CharacterContext);
  if (!context) {
    throw new Error('useCharacter must be used within a CharacterProvider');
  }
  return context;
}
