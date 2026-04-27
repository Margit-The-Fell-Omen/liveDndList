import {useState, useEffect, type ChangeEvent} from 'react'; // Import useState and useEffect
import {useCharacter} from '@/context/CharacterContext';
import {Input, Select} from '@/components/common/Input';
import {ALIGNMENTS} from '@/utils/constants';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CharacterHeader.module.css';

export function CharacterHeader({className}: { className?: string }) {
  const {currentCharacter, updateCharacter} = useCharacter();

  const [characterName, setCharacterName] = useState('');

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
    }
  }, [currentCharacter]);

  if (!currentCharacter) {
    return null;
  }

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

          <Input
              label="Background"
              name="background"
              defaultValue={currentCharacter.background}
              onChange={handleDebouncedChange}
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
