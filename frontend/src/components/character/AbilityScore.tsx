// src/components/character/AbilityScore.tsx
import {AbilityInfo} from '@/utils/constants';
import {formatModifier} from '@/utils/helpers';
import {Input} from '@/components/common/Input';
import styles from './AbilityScore.module.css';

interface AbilityScoreProps {
  ability: AbilityInfo;
  score: number;
  modifier: number;
}

export function AbilityScore({ability, score, modifier}: AbilityScoreProps) {
  // A debounced update function would go here
  const handleScoreChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    console.log(`Updating ${ability.name} to ${e.target.value}`);
    // Call debounced updateCharacter here
  };

  return (
      <div className={styles.abilityScore}>
        <label className={styles.label}>{ability.name}</label>
        <div className={styles.modifier}>{formatModifier(modifier)}</div>
        <div className={styles.scoreInputWrapper}>
          <Input
              type="number"
              defaultValue={score}
              onChange={handleScoreChange}
              className={styles.scoreInput}
              aria-label={`${ability.name} score`}
          />
        </div>
      </div>
  );
}
