import { type ChangeEvent } from 'react';
import { useCharacter } from '@/context/CharacterContext';
import { TextArea } from '@/components/common/Input';
import styles from './Background.module.css';

export function Background() {
  const { currentCharacter, updateNestedCharacter } = useCharacter();

  if (!currentCharacter) return null;

  const { personality } = currentCharacter;

  return (
    <div className={styles.background}>
      <h3 className={styles.title}>Personality</h3>

      <div className={styles.grid}>
        <TextArea
          label="Personality Traits"
          value={personality.traits}
          onChange={(e: ChangeEvent<HTMLTextAreaElement>) =>
            updateNestedCharacter('personality.traits', e.target.value)
          }
          placeholder="Enter your character's personality traits..."
          rows={3}
          autoResize
          fullWidth
        />

        <TextArea
          label="Ideals"
          value={personality.ideals}
          onChange={(e: ChangeEvent<HTMLTextAreaElement>) =>
            updateNestedCharacter('personality.ideals', e.target.value)
          }
          placeholder="What ideals does your character believe in?"
          rows={3}
          autoResize
          fullWidth
        />

        <TextArea
          label="Bonds"
          value={personality.bonds}
          onChange={(e: ChangeEvent<HTMLTextAreaElement>) =>
            updateNestedCharacter('personality.bonds', e.target.value)
          }
          placeholder="What connections does your character have?"
          rows={3}
          autoResize
          fullWidth
        />

        <TextArea
          label="Flaws"
          value={personality.flaws}
          onChange={(e: ChangeEvent<HTMLTextAreaElement>) =>
            updateNestedCharacter('personality.flaws', e.target.value)
          }
          placeholder="What are your character's flaws or weaknesses?"
          rows={3}
          autoResize
          fullWidth
        />
      </div>

      <div className={styles.notes}>
        <TextArea
          label="Notes & Backstory"
          value={currentCharacter.notes}
          onChange={(e: ChangeEvent<HTMLTextAreaElement>) =>
            updateNestedCharacter('notes', e.target.value)
          }
          placeholder="Additional notes, backstory, and other information..."
          rows={6}
          autoResize
          fullWidth
        />
      </div>
    </div>
  );
}
