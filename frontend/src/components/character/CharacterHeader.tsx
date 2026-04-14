// src/components/character/CharacterHeader.tsx

import {type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input, Select} from '@/components/common/Input';
import {ALIGNMENTS} from '@/utils/constants';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CharacterHeader.module.css';

export function CharacterHeader() {
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

  return (
      <div className={styles.header}>
        <div className={styles.mainInfo}>
          <Input
              name="name"
              defaultValue={currentCharacter.name}
              onChange={handleChange}
              placeholder="Character Name"
              className={styles.nameInput}
          />

          <div className={styles.basicInfo}>
            {/* FIX: Race and Class are now display-only based on backend data */}
            <div className={styles.infoBlock}>
              <label>Race</label>
              <span>{currentCharacter.raceName || 'N/A'}</span>
            </div>

            <div className={styles.infoBlock}>
              <label>Class & Level</label>
              {/* Join the array of class strings */}
              <span>{currentCharacter.classesInfo.join(' / ') || 'N/A'}</span>
            </div>
          </div>
        </div>

        <div className={styles.secondaryInfo}>
          <Input
              name="background"
              defaultValue={currentCharacter.background}
              onChange={handleChange}
              placeholder="Background"
          />

          <Select
              name="alignment"
              defaultValue={currentCharacter.alignment}
              onChange={handleChange}
              options={ALIGNMENTS}
              placeholder="Alignment"
          />

          <Input
              name="experiencePoints"
              type="number"
              defaultValue={currentCharacter.experiencePoints}
              onChange={handleChange}
              placeholder="XP"
          />
        </div>

        <div className={styles.stats}>
          {/* FIX: This section is now display-only. The stats are calculated or
            set in other more specific components (like CombatStats.tsx) */}
          <div className={styles.stat}>
            <span className={styles.statLabel}>Proficiency</span>
            <span className={styles.statValue}>
            +{currentCharacter.proficiencyBonus}
          </span>
          </div>

          <div className={styles.stat}>
            <span className={styles.statLabel}>Initiative</span>
            <span className={styles.statValue}>
            {currentCharacter.initiative >= 0 ? '+' : ''}{currentCharacter.initiative}
          </span>
          </div>

          <div className={styles.stat}>
            <span className={styles.statLabel}>Speed</span>
            <span className={styles.statValue}>
            {currentCharacter.speed}ft
          </span>
          </div>

          <div className={styles.stat}>
            <span className={styles.statLabel}>Armor Class</span>
            <span className={styles.statValue}>
            {currentCharacter.armorClass}
          </span>
          </div>
        </div>
      </div>
  );
}
