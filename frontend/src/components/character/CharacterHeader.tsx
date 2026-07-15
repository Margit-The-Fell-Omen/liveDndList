import {type ChangeEvent, useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input, Select} from '@/components/common/Input';
import {ALIGNMENT_OPTIONS} from '@/utils/constants';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import {RacePickerModal} from './RacePickerModal';
import {getRaceDisplayName} from '@/utils/races';
import styles from './CharacterHeader.module.css';

export function CharacterHeader({className}: { className?: string }) {
  const {currentCharacter, backgrounds, races, updateCharacter, saving} = useCharacter();

  const [characterName, setCharacterName] = useState('');
  const [backgroundKey, setBackgroundKey] = useState<string>('');
  const [alignment, setAlignment] = useState<string>('');
  const [experience, setExperience] = useState(0);
  const [isRacePickerOpen, setIsRacePickerOpen] = useState(false);

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

  const backgroundOptions = backgrounds.map(background => ({
    value: background.key,
    label: background.name,
  }));

  const raceDisplay =
      getRaceDisplayName(races, currentCharacter.raceKey) || currentCharacter.raceKey || 'N/A';

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

  const handleRaceConfirm = async (raceKey: string) => {
    await updateCharacter(currentCharacter.id, {raceKey});
    setIsRacePickerOpen(false);
  };

  return (
      <>
        <div className={`${styles.header} ${className ?? ''}`}>
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
            <div className={styles.raceField}>
              <label className={styles.raceLabel}>Race</label>
              <button
                  type="button"
                  className={styles.raceButton}
                  onClick={() => setIsRacePickerOpen(true)}
              >
                <span className={styles.raceValue}>{raceDisplay}</span>
                <span className={styles.raceEditIcon} aria-hidden="true">✎</span>
              </button>
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

        <RacePickerModal
            isOpen={isRacePickerOpen}
            onClose={() => setIsRacePickerOpen(false)}
            races={races}
            initialRaceKey={currentCharacter.raceKey}
            onConfirm={handleRaceConfirm}
            saving={saving}
        />
      </>
  );
}
