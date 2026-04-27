// src/components/character/Features.tsx

import {useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import {TextArea} from '@/components/common/Input';
import styles from './Features.module.css';
import {Card} from '@/components/common/Card';

export function Features({className}: { className?: string }) {
  const {currentCharacter, updateCharacter, saving} = useCharacter();

  const [localFeatures, setLocalFeatures] = useState(currentCharacter?.featuresAndTraits || '');

  useEffect(() => {
    setLocalFeatures(currentCharacter?.featuresAndTraits || '');
  }, [currentCharacter?.id, currentCharacter?.featuresAndTraits]);

  const debouncedUpdate = useDebouncedCallback(
      (newText: string) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, {featuresAndTraits: newText});
        }
      },
      500
  );

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newText = e.target.value;
    setLocalFeatures(newText);
    debouncedUpdate(newText);
  };

  if (!currentCharacter) {
    return null;
  }

  return (
      <Card title="Features" className={className}>

        <TextArea
            value={localFeatures}
            onChange={handleChange}
            placeholder="Add your class features, racial traits, and feats here. Each on a new line."
            rows={10}
            fullWidth
            autoResize
            className={styles.featureTextarea}
        />
      </Card>
  );
}
