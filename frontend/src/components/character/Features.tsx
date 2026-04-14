// src/components/character/Features.tsx

import {useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import {TextArea} from '@/components/common/Input';
import styles from './Features.module.css';

export function Features() {
  const {currentCharacter, updateCharacter, saving} = useCharacter();

  // Use local state to manage the text area's content for a responsive UI.
  // This avoids calling the update function on every single keystroke.
  const [localFeatures, setLocalFeatures] = useState(currentCharacter?.featuresAndTraits || '');

  // When the currentCharacter changes (e.g., user selects a different one),
  // update the local state to match the new character's data.
  useEffect(() => {
    setLocalFeatures(currentCharacter?.featuresAndTraits || '');
  }, [currentCharacter?.id, currentCharacter?.featuresAndTraits]);

  // Create a debounced version of the update function.
  // This will wait 500ms after the user stops typing before calling the API.
  const debouncedUpdate = useDebouncedCallback(
      (newText: string) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, {featuresAndTraits: newText});
        }
      },
      500 // 500ms delay
  );

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newText = e.target.value;
    setLocalFeatures(newText); // Update the UI immediately
    debouncedUpdate(newText);   // Schedule the API call
  };

  // If there's no character, render nothing.
  if (!currentCharacter) {
    return null;
  }

  return (
      <div className={styles.features}>
        <div className={styles.header}>
          <h3 className={styles.title}>Features & Traits</h3>
          {/* A subtle saving indicator */}
          {saving && <span className={styles.savingIndicator}>Saving...</span>}
        </div>

        <TextArea
            value={localFeatures}
            onChange={handleChange}
            placeholder="Add your class features, racial traits, and feats here. Each on a new line."
            rows={10}
            fullWidth
            autoResize
            className={styles.featureTextarea}
        />
      </div>
  );
}
