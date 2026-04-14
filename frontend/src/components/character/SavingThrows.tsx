// src/components/character/SavingThrows.tsx

import {ABILITIES} from '@/utils/constants';
import {formatModifier, getAbilityModifier} from '@/utils/helpers';
import {useCharacter} from '@/context/CharacterContext';
import type {AbilityName} from '@/types';
import styles from './SavingThrows.module.css';

export function SavingThrows() {
  const {currentCharacter} = useCharacter();

  if (!currentCharacter) {
    return null;
  }

  // CORRECT: Destructure the correct properties from the character
  const {abilityScores, savingThrowProficiencies, proficiencyBonus} = currentCharacter;

  return (
      <div className={styles.savingThrows}>
        <h3 className={styles.title}>Saving Throws</h3>

        <div className={styles.list}>
          {ABILITIES.map((abilityInfo) => {
            // CORRECT: Access properties using the new data structure
            const score = abilityScores[abilityInfo.key];
            const abilityName = abilityInfo.key.toUpperCase() as AbilityName;
            const isProficient = savingThrowProficiencies.includes(abilityName);

            const baseModifier = getAbilityModifier(score);
            const totalModifier = baseModifier + (isProficient ? proficiencyBonus : 0);

            return (
                <div key={abilityInfo.key} className={styles.row}>
                  <button
                      type="button"
                      className={`${styles.checkbox} ${isProficient ? styles.checked : ''}`}
                      // onClick logic needs to be added later with a proper update function
                  >
                    {isProficient ? '●' : '○'}
                  </button>

                  <span className={styles.modifier}>{formatModifier(totalModifier)}</span>
                  <span className={styles.name}>{abilityInfo.name}</span>
                </div>
            );
          })}
        </div>
      </div>
  );
}
