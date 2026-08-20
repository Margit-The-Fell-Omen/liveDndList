import {ABILITIES} from '@/utils/constants';

import {formatModifier} from '@/utils/helpers';
import {useCharacter} from '@/context/CharacterContext';
import {Card} from '@/components/common/Card';
import styles from './SavingThrows.module.css';

export function SavingThrows({className}: { className?: string }) {
  const {currentCharacter} = useCharacter();

  if (!currentCharacter) return null;

  const {proficiencyBonus, savingThrowProficiencies = [], abilityScores} = currentCharacter;

  const grantedSet = new Set(savingThrowProficiencies);

  return (
      <Card title="Saving Throws" className={className}>
        <div className={styles.list}>
          {ABILITIES.map(abilityInfo => {
            const scoreKey = abilityInfo.key as keyof typeof abilityScores;
            const score = abilityScores[scoreKey] ?? 10;
            const abilityName = abilityInfo.name.toUpperCase() as typeof savingThrowProficiencies[number];
            const isProficient = grantedSet.has(abilityName);
            const baseModifier = Math.floor((Number(score) - 10) / 2);
            const totalModifier = baseModifier + (isProficient ? proficiencyBonus : 0);

            return (
                <div key={abilityInfo.key} className={styles.row}>
                  {/* Read-only indicator — pipeline owns save proficiencies */}
                  <span
                      className={`${styles.checkbox} ${isProficient ? styles.checked : ''}`}
                      title={isProficient ? 'Proficiency granted by class feature' : 'Not proficient'}
                      aria-label={`${abilityInfo.name} saving throw ${isProficient ? 'proficient' : 'not proficient'}`}
                  >
                {isProficient ? '●' : '○'}
              </span>

                  <span className={styles.modifier}>{formatModifier(totalModifier)}</span>
                  <span className={styles.name}>{abilityInfo.name}</span>

                  {isProficient && (
                      <span className={styles.grantedTag} title="Granted by class feature">
                  ✦
                </span>
                  )}
                </div>
            );
          })}
        </div>
        <p className={styles.hint}>
          Saving throw proficiencies are granted by your class and features.
        </p>
      </Card>
  );
}
