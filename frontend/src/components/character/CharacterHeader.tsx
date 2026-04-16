// src/components/character/CharacterHeader.tsx

import {type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input, Select} from '@/components/common/Input';
import {ALIGNMENTS} from '@/utils/constants';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CharacterHeader.module.css';

export function CharacterHeader({className}: { className?: string }) {

  // FIX: Get `races` and `classes` from the context now
  const {currentCharacter, updateCharacter, races, classes} = useCharacter();

  const debouncedUpdate = useDebouncedCallback(
      (payload: object) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, payload);
        }
      },
      500
  );

  if (!currentCharacter) {
    return null;
  }

  // Generic handler for simple text/number field changes
  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const {name, value} = e.target;
    // For numbers, parse them, otherwise use the string value
    const isNumberField = e.target.type === 'number';
    const finalValue = isNumberField ? parseInt(value, 10) || 0 : value;
    debouncedUpdate({[name]: finalValue});
  };

  if (!currentCharacter) return null;

  return (
      // Pass className for grid placement
      <div className={`${styles.header} ${className}`}>
        <div className={styles.mainInfo}>
          <Input
              name="name"
              defaultValue={currentCharacter.name}
              onChange={handleChange}
              placeholder="Character Name"
              className={styles.nameInput}
          />
        </div>

        <div className={styles.secondaryInfo}>
          {/* Race and Class Info */}
          <div className={styles.infoBlock}>
            <label>Race</label>
            <span>{currentCharacter.raceName || 'N/A'}</span>
          </div>
          <div className={styles.infoBlock}>
            <label>Class & Level</label>
            <span>{currentCharacter.classesInfo.join(' / ') || 'N/A'}</span>
          </div>

          {/* Background, Alignment, XP */}
          <Input
              label="Background"
              name="background"
              defaultValue={currentCharacter.background}
              onChange={handleChange}
          />
          <Select
              label="Alignment"
              name="alignment"
              defaultValue={currentCharacter.alignment}
              onChange={handleChange}
              options={ALIGNMENTS}
          />
          <Input
              label="Experience"
              name="experiencePoints"
              type="number"
              defaultValue={currentCharacter.experiencePoints}
              onChange={handleChange}
          />
        </div>

        {/* --- FIX: REMOVED the entire 'stats' div section --- */}
      </div>
  );
}
