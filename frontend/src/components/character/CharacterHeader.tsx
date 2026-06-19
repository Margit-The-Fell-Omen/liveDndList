import {type ChangeEvent, useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input, Select} from '@/components/common/Input';
import {ALIGNMENT_OPTIONS} from '@/utils/constants';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CharacterHeader.module.css';

export function CharacterHeader({className}: { className?: string }) {
  const {currentCharacter, backgrounds, updateCharacter} = useCharacter();

  const [characterName, setCharacterName] = useState('');
  const [backgroundKey, setBackgroundKey] = useState<string>('');
  const [alignment, setAlignment] = useState<string>('');
  const [experience, setExperience] = useState(0);

  const debouncedUpdate = useDebouncedCallback(
      (payload: object) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, payload);
        }
      },
      500
  );

  useEffect(() => {
    if (currentCharacter) {
      setCharacterName(currentCharacter.name);
      setBackgroundKey(currentCharacter.backgroundKey);
      setAlignment(currentCharacter.alignment);
      setExperience(currentCharacter.experiencePoints);
    }
  }, [currentCharacter]);

  if (!currentCharacter) {
    return null;
  }

  const backgroundOptions = backgrounds.map(b => ({value: b.key, label: b.name}));

  const handleNameChange = (e: ChangeEvent<HTMLInputElement>) => {
    setCharacterName(e.target.value);
    debouncedUpdate({name: e.target.value});
  };

  const handleBackgroundChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const newKey = e.target.value;
    setBackgroundKey(newKey);
    debouncedUpdate({backgroundKey: newKey});
  };

  const handleAlignmentChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const newAlignment = e.target.value;
    setAlignment(newAlignment);
    debouncedUpdate({alignment: newAlignment});
  };

  const handleExperienceChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(e.target.value, 10) || 0;
    setExperience(value);
    debouncedUpdate({experiencePoints: value});
  };

  return (
      <div className={`${styles.header} ${className}`}>
        <div className={styles.mainInfo}>
          <Input
              name="name"
              value={characterName}
              onChange={handleNameChange}
              placeholder="Character Name"
              className={styles.nameInput}
          />
        </div>

        <div className={styles.secondaryInfo}>
          <div className={styles.infoBlock}>
            <label>Race</label>
            <span>{currentCharacter.raceName || 'N/A'}</span>
          </div>
          <div className={styles.infoBlock}>
            <label>Class & Level</label>
            <span>
            {currentCharacter.classesInfo.join(' / ') || 'N/A'}
              {` - Level ${currentCharacter.totalLevel}`}
          </span>
          </div>

          <Select
              label="Background"
              name="background"
              value={backgroundKey}
              onChange={handleBackgroundChange}
              options={backgroundOptions}
          />
          <Select
              label="Alignment"
              name="alignment"
              value={alignment}
              onChange={handleAlignmentChange}
              options={ALIGNMENT_OPTIONS}
          />
          <Input
              label="Experience"
              name="experiencePoints"
              type="number"
              value={experience}
              onChange={handleExperienceChange}
          />
        </div>
      </div>
  );
}
