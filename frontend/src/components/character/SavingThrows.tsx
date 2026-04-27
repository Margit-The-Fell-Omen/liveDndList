// src/components/character/SavingThrows.tsx

import {ABILITIES} from '@/utils/constants';
import {formatModifier, getAbilityModifier} from '@/utils/helpers';
import {useCharacter} from '@/context/CharacterContext';
import type {AbilityName} from '@/types';
import styles from './SavingThrows.module.css';
import {Card} from "@components/common/Card.tsx";

export function SavingThrows({className}: { className?: string }) {
  const {currentCharacter, toggleSavingThrowProficiency} = useCharacter();

  if (!currentCharacter) return null;

  const {proficiencyBonus} = currentCharacter;

  return (
      <Card title="Saving Throws" className={className}>
        <div className={styles.list}>
          {ABILITIES.map((abilityInfo) => {
            const score = currentCharacter.abilityScores[abilityInfo.key as keyof typeof currentCharacter.abilityScores];
            const abilityName = abilityInfo.key.toUpperCase() as AbilityName;
            const isProficient = (currentCharacter.savingThrowProficiencies || []).includes(abilityName);
            const baseModifier = getAbilityModifier(score);
            const totalModifier = baseModifier + (isProficient ? proficiencyBonus : 0);

            return (
                <div key={abilityInfo.key} className={styles.row}>
                  <button
                      type="button"
                      className={`${styles.checkbox} ${isProficient ? styles.checked : ''}`}
                      onClick={() => toggleSavingThrowProficiency(abilityName)}
                  >
                    {isProficient ? '●' : '○'}
                  </button>
                  <span className={styles.modifier}>{formatModifier(totalModifier)}</span>
                  <span className={styles.name}>{abilityInfo.name}</span>
                </div>
            );
          })}
        </div>
      </Card>
  );
}
