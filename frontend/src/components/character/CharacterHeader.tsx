import { type ChangeEvent } from 'react';
import { useCharacter } from '@/context/CharacterContext';
import { Input, Select } from '@/components/common/Input';
import { CLASSES, RACES, ALIGNMENTS } from '@/utils/constants';
import { getProficiencyBonus } from '@/utils/dndCalculations';
import styles from './CharacterHeader.module.css';

export function CharacterHeader() {
  const { currentCharacter, updateCharacter } = useCharacter();

  if (!currentCharacter) return null;

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>): void => {
    const { name, value } = e.target;
    updateCharacter({ [name]: value });
  };

  const handleLevelChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const level = parseInt(e.target.value, 10) || 1;
    const proficiencyBonus = getProficiencyBonus(level);
    updateCharacter({
      level,
      proficiencyBonus,
    });
  };

  return (
    <div className={styles.header}>
      <div className={styles.mainInfo}>
        <Input
          name="name"
          value={currentCharacter.name}
          onChange={handleChange}
          placeholder="Character Name"
          className={styles.nameInput}
        />

        <div className={styles.basicInfo}>
          <Select
            name="class"
            value={currentCharacter.class}
            onChange={handleChange}
            options={CLASSES}
            placeholder="Class"
          />

          <Select
            name="race"
            value={currentCharacter.race}
            onChange={handleChange}
            options={RACES}
            placeholder="Race"
          />

          <Input
            name="level"
            type="number"
            value={currentCharacter.level}
            onChange={handleLevelChange}
            placeholder="Level"
            min={1}
            max={20}
          />
        </div>
      </div>

      <div className={styles.secondaryInfo}>
        <Input
          name="background"
          value={currentCharacter.background}
          onChange={handleChange}
          placeholder="Background"
        />

        <Select
          name="alignment"
          value={currentCharacter.alignment}
          onChange={handleChange}
          options={ALIGNMENTS}
          placeholder="Alignment"
        />

        <Input
          name="experiencePoints"
          type="number"
          value={currentCharacter.experiencePoints}
          onChange={(e) =>
            updateCharacter({ experiencePoints: parseInt(e.target.value, 10) || 0 })
          }
          placeholder="XP"
        />
      </div>

      <div className={styles.stats}>
        <div className={styles.stat}>
          <span className={styles.statLabel}>Proficiency Bonus</span>
          <span className={styles.statValue}>+{currentCharacter.proficiencyBonus}</span>
        </div>

        <div className={styles.stat}>
          <span className={styles.statLabel}>Initiative</span>
          <Input
            name="initiative"
            type="number"
            value={currentCharacter.initiative}
            onChange={(e) => updateCharacter({ initiative: parseInt(e.target.value, 10) || 0 })}
            className={styles.statInput}
          />
        </div>

        <div className={styles.stat}>
          <span className={styles.statLabel}>Speed</span>
          <Input
            name="speed"
            type="number"
            value={currentCharacter.speed}
            onChange={(e) => updateCharacter({ speed: parseInt(e.target.value, 10) || 30 })}
            className={styles.statInput}
          />
        </div>

        <div className={styles.stat}>
          <span className={styles.statLabel}>Armor Class</span>
          <Input
            name="armorClass"
            type="number"
            value={currentCharacter.armorClass}
            onChange={(e) => updateCharacter({ armorClass: parseInt(e.target.value, 10) || 10 })}
            className={styles.statInput}
          />
        </div>
      </div>
    </div>
  );
}
