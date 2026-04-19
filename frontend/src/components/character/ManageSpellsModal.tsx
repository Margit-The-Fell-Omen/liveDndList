import {useCallback, useEffect, useState} from 'react'; // Add useCallback import
import {useDebounce} from '@/hooks/useDebounce';
import {spellsApi} from '@/services/api';
import {useCharacter} from '@/context/CharacterContext';
import {Modal} from '@/components/common/Modal';
import {Input} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import type {SpellResponse} from '@/types';
import styles from './ManageSpellsModal.module.css';

interface ManageSpellsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function ManageSpellsModal({isOpen, onClose}: ManageSpellsModalProps) {
  const {currentCharacter, addSpellToCharacter, saving} = useCharacter();
  const [searchQuery, setSearchQuery] = useState('');
  const [results, setResults] = useState<SpellResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const debouncedQuery = useDebounce(searchQuery, 300);

  // THE FIX: Wrap the fetch logic in useCallback
  const fetchSpells = useCallback(async () => {
    if (debouncedQuery.length < 2) {
      setResults([]);
      return;
    }
    setIsLoading(true);
    try {
      const foundSpells = await spellsApi.search(debouncedQuery);
      setResults(foundSpells);
    } catch (error) {
      console.error("Failed to search for spells:", error);
    } finally {
      setIsLoading(false);
    }
  }, [debouncedQuery]); // Dependency on the debounced query

  useEffect(() => {
    if (isOpen) {
      fetchSpells();
    } else {
      // Reset state when modal closes
      setSearchQuery('');
      setResults([]);
    }
  }, [isOpen, fetchSpells]); // Depend on isOpen and the stable fetchSpells function

  const knownSpellIds = new Set(currentCharacter?.spells.map(s => s.id));

  return (
      <Modal isOpen={isOpen} onClose={onClose} title="Add Spells to Spellbook" size="large">
        <div className={styles.container}>
          <Input
              placeholder="Search for a spell..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
          />
          <div className={styles.resultsContainer}>
            {isLoading && <p>Searching...</p>}
            {!isLoading && results.length === 0 && debouncedQuery.length > 1 &&
                <p>No spells found.</p>}
            {results.map(spell => (
                <div key={spell.id} className={styles.spellRow}>
                  <div className={styles.spellInfo}>
                    <span className={styles.spellName}>{spell.name}</span>
                    <span className={styles.spellMeta}>Lvl {spell.level} {spell.school}</span>
                  </div>
                  <Button
                      size="small"
                      disabled={knownSpellIds.has(spell.id) || saving}
                      onClick={() => addSpellToCharacter(spell.id)}
                  >
                    {knownSpellIds.has(spell.id) ? 'Known' : 'Add'}
                  </Button>
                </div>
            ))}
          </div>
        </div>
      </Modal>
  );
}
