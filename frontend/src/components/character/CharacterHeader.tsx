import {useState, useEffect, type ChangeEvent} from 'react'; // Import useState and useEffect
import {useCharacter} from '@/context/CharacterContext';
import {Input, Select} from '@/components/common/Input';
import {ALIGNMENTS} from '@/utils/constants';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CharacterHeader.module.css';

export function CharacterHeader({className}: { className?: string }) {
  const {currentCharacter, updateCharacter} = useCharacter();

  // --- FIX 1: Local state for the controlled name input ---
  const [characterName, setCharacterName] = useState('');

  const debouncedUpdate = useDebouncedCallback(
      (payload: object) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, payload);
        }
      },
      500
  );

  // --- FIX 2: Sync local state when the character from context changes ---
  useEffect(() => {
    if (currentCharacter) {
      setCharacterName(currentCharacter.name);
    }
  }, [currentCharacter]); // This effect runs whenever `currentCharacter` changes

  if (!currentCharacter) {
    return null; // Or a loading/placeholder component
  }

  // This handler is now only for the other, simpler inputs
  const handleDebouncedChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const {name, value} = e.target;
    const isNumberField = e.target.type === 'number';
    const finalValue = isNumberField ? parseInt(value, 10) || 0 : value;
    debouncedUpdate({[name]: finalValue});
  };

  // --- FIX 3: A new handler specifically for the name input ---
  const handleNameChange = (e: ChangeEvent<HTMLInputElement>) => {
    // Update local state instantly for a responsive UI
    setCharacterName(e.target.value);
    // Debounce the call to the backend
    debouncedUpdate({name: e.target.value});
  };

  return (
      <div className={`${styles.header} ${className}`}>
        <div className={styles.mainInfo}>
          {/* --- FIX 4: Use `value` and the new handler --- */}
          <Input
              name="name"
              value={characterName} // Use the controlled 'value' prop
              onChange={handleNameChange} // Use the new handler
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
            {/* --- FIX 5: Display the level --- */}
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
