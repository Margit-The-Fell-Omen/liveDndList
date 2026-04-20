// src/components/character/AbilityScore.tsx
import {useCharacter} from '@/context/CharacterContext';
import {useDebouncedCallback} from '@/hooks/useDebounce';
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
  const {currentCharacter, updateCharacter} = useCharacter();

  const debouncedUpdate = useDebouncedCallback((newScore: number) => {
    if (currentCharacter) {
      const abilityKey = ability.key as keyof typeof currentCharacter.abilityScores;
      // Construct the payload for the nested abilityScores object
      const payload = {
        abilityScores: {
          ...currentCharacter.abilityScores,
          [abilityKey]: newScore,
        },
      };
      updateCharacter(currentCharacter.id, payload);
    }
  }, 500);

  const handleScoreChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newScore = parseInt(e.target.value, 10) || 0;
    debouncedUpdate(newScore);
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
