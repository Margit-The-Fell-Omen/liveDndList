import {type ChangeEvent, useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import {type AbilityInfo} from '@/utils/constants';
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

  const [draft, setDraft] = useState<string>(String(score));

  useEffect(() => {
    setDraft(String(score));
  }, [score, currentCharacter?.id]);

  const debouncedUpdate = useDebouncedCallback((newScore: number) => {
    if (!currentCharacter) return;
    const abilityKey = ability.key as keyof typeof currentCharacter.abilityScores;
    updateCharacter(currentCharacter.id, {
      abilityScores: {
        ...currentCharacter.abilityScores,
        [abilityKey]: newScore,
      },
    });
  }, 500);

  const handleScoreChange = (e: ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value;
    setDraft(raw);
    const parsed = parseInt(raw, 10);
    if (!Number.isNaN(parsed)) {
      debouncedUpdate(parsed);
    }
  };

  return (
      <div className={styles.abilityScore}>
        <label className={styles.label}>{ability.name}</label>
        <div className={styles.modifier}>{formatModifier(modifier)}</div>
        <div className={styles.scoreInputWrapper}>
          <Input
              type="number"
              value={draft}
              onChange={handleScoreChange}
              className={styles.scoreInput}
              aria-label={`${ability.name} score`}
          />
        </div>
      </div>
  );
}
