import {type ChangeEvent, useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input, Select} from '@/components/common/Input';
import {ALIGNMENTS} from '@/utils/constants';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CharacterHeader.module.css';

export function CharacterHeader({className}: { className?: string }) {
  const {currentCharacter, backgrounds, updateCharacter} = useCharacter();

  const [characterName, setCharacterName] = useState('');
  const [backgroundKey, setBackgroundKey] = useState<string>('');

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
      setBackgroundKey(currentCharacter.backgroundKey ?? '');
    }
  }, [currentCharacter]);

  if (!currentCharacter) {
    return null;
  }

  const backgroundOptions = backgrounds.map(b => ({value: b.key, label: b.name}));

  const handleDebouncedChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const {name, value} = e.target;
    const isNumberField = e.target.type === 'number';
    const finalValue = isNumberField ? parseInt(value, 10) || 0 : value;
    debouncedUpdate({[name]: finalValue});
  };

  const handleNameChange = (e: ChangeEvent<HTMLInputElement>) => {
    setCharacterName(e.target.value);
    debouncedUpdate({name: e.target.value});
  };

  const handleBackgroundChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const newKey = e.target.value;
    setBackgroundKey(newKey);
    debouncedUpdate({backgroundKey: newKey});
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
              defaultValue={currentCharacter.backgroundKey}
              onChange={handleBackgroundChange}
              options={backgroundOptions}
          />
          <Select
              label="Alignment"
              name="alignment"
              defaultValue={currentCharacter.alignment}
              onChange={handleDebouncedChange}
              options={ALIGNMENTS}
          />
          <Input
              label="Experience"
              name="experiencePoints"
              type="number"
              defaultValue={currentCharacter.experiencePoints}
              onChange={handleDebouncedChange}
          />
        </div>
      </div>
  );
}
