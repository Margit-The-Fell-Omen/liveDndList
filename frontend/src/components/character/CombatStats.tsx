// src/components/character/CombatStats.tsx

import {type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CombatStats.module.css';
import {Card} from '@/components/common/Card';

export function CombatStats({className}: { className?: string }) {
  const {currentCharacter, updateCharacter} = useCharacter();

  // Create a debounced version of the update function for performance
  const debouncedUpdate = useDebouncedCallback(
      (key: 'armorClass' | 'initiative' | 'speed', value: number) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, {[key]: value});
        }
      },
      500 // 500ms delay
  );

  if (!currentCharacter) {
    return null;
  }

  // Destructure the correct properties
  const {armorClass, initiative, speed, hitDice} = currentCharacter;

  // Generic handler for number inputs
  const handleChange = (
      e: ChangeEvent<HTMLInputElement>,
      key: 'armorClass' | 'initiative' | 'speed'
  ) => {
    const value = parseInt(e.target.value, 10) || 0;
    // We can update the UI immediately if we use local state,
    // but for simple fields like this, a debounced API call is fine.
    debouncedUpdate(key, value);
  };

  return (
      <Card title="Combat Stats" className={className}>

        <div className={styles.grid}>
          {/* Armor Class */}
          <div className={styles.stat}>
            <label className={styles.label}>Armor Class</label>
            <Input
                type="number"
                defaultValue={armorClass} // Use defaultValue for uncontrolled debounced input
                onChange={(e) => handleChange(e, 'armorClass')}
                min={0}
                className={styles.input}
            />
          </div>

          {/* Initiative */}
          <div className={styles.stat}>
            <label className={styles.label}>Initiative</label>
            <Input
                type="number"
                defaultValue={initiative} // Use defaultValue
                onChange={(e) => handleChange(e, 'initiative')}
                className={styles.input}
            />
          </div>

          {/* Speed */}
          <div className={styles.stat}>
            <label className={styles.label}>Speed</label>
            <div className={styles.speedInput}>
              <Input
                  type="number"
                  defaultValue={speed} // Use defaultValue
                  onChange={(e) => handleChange(e, 'speed')}
                  min={0}
                  className={styles.input}
              />
              <span className={styles.unit}>ft</span>
            </div>
          </div>
        </div>

        {/* Hit Dice Display */}
        {/* FIX: This section is now display-only, as the backend provides the full string */}
        <div className={styles.hitDice}>
          <div className={styles.stat}>
            <label className={styles.label}>Hit Dice</label>
            <span className={styles.hitDiceValue}>{hitDice || 'N/A'}</span>
          </div>
        </div>
      </Card>
  );
}
