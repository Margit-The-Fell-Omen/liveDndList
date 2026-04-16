// src/components/character/Background.tsx

import {type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import {TextArea} from '@/components/common/Input';
import {Card} from '@/components/common/Card';
import styles from './Background.module.css';

// A type helper for the keys we'll be updating
type BackgroundField = 'personalityTraits' | 'ideals' | 'bonds' | 'flaws' | 'backstory' | 'notes';

export function Background({className}: { className?: string }) {

  const {currentCharacter, updateCharacter} = useCharacter();

  const debouncedUpdate = useDebouncedCallback(
      (key: BackgroundField, value: string) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, {[key]: value});
        }
      },
      500 // 500ms delay
  );

  if (!currentCharacter) {
    return null;
  }

  // FIX 1: Destructure the correct top-level properties.
  const {
    personalityTraits,
    ideals,
    bonds,
    flaws,
    backstory,
    notes
  } = currentCharacter;

  // Generic change handler for all text areas
  const handleChange = (e: ChangeEvent<HTMLTextAreaElement>, key: BackgroundField) => {
    debouncedUpdate(key, e.target.value);
  };

  return (
      <Card title="Personality & Backstory" className={className}>
        <div className={styles.grid}>
          {/* FIX 2: Bind each TextArea to its correct top-level property */}
          <TextArea
              label="Personality Traits"
              defaultValue={personalityTraits}
              onChange={(e) => handleChange(e, 'personalityTraits')}
              placeholder="Enter your character's personality traits..."
              rows={3}
              autoResize
              fullWidth
          />

          <TextArea
              label="Ideals"
              defaultValue={ideals}
              onChange={(e) => handleChange(e, 'ideals')}
              placeholder="What ideals does your character believe in?"
              rows={3}
              autoResize
              fullWidth
          />

          <TextArea
              label="Bonds"
              defaultValue={bonds}
              onChange={(e) => handleChange(e, 'bonds')}
              placeholder="What connections does your character have?"
              rows={3}
              autoResize
              fullWidth
          />

          <TextArea
              label="Flaws"
              defaultValue={flaws}
              onChange={(e) => handleChange(e, 'flaws')}
              placeholder="What are your character's flaws or weaknesses?"
              rows={3}
              autoResize
              fullWidth
          />
        </div>

        <div className={styles.notes}>
          <TextArea
              label="Backstory"
              defaultValue={backstory}
              onChange={(e) => handleChange(e, 'backstory')}
              placeholder="Your character's history..."
              rows={6}
              autoResize
              fullWidth
          />
          <TextArea
              label="Notes"
              defaultValue={notes}
              onChange={(e) => handleChange(e, 'notes')}
              placeholder="Additional notes, session logs, etc."
              rows={6}
              autoResize
              fullWidth
          />
        </div>
      </Card>
  );
}
