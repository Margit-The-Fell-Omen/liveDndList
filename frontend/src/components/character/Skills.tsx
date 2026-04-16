// src/components/character/Skills.tsx

import {ABILITIES, SKILLS} from '@/utils/constants';
import {formatModifier, getAbilityModifier} from '@/utils/helpers';
import {useCharacter} from '@/context/CharacterContext';
import type {SkillName} from '@/types';
import styles from './Skills.module.css';

export function Skills() {
  const {currentCharacter} = useCharacter();

  if (!currentCharacter) {
    return null;
  }

  // --- THE FIX ---
  // Use fallback empty arrays and default objects to prevent crashes if data is null.
  const characterSkills = currentCharacter.skills || [];
  const abilityScores = currentCharacter.abilityScores || {
    strength: 10, strengthModifier: 0, dexterity: 10, dexterityModifier: 0,
    constitution: 10, constitutionModifier: 0, intelligence: 10, intelligenceModifier: 0,
    wisdom: 10, wisdomModifier: 0, charisma: 10, charismaModifier: 0,
  };
  // --- END OF FIX ---


  const toggleProficiency = (skillName: SkillName) => {
    console.log(`Toggling proficiency for ${skillName}`);
    console.warn("Updating skills requires backend implementation.");
  };

  const toggleExpertise = (skillName: SkillName) => {
    console.log(`Toggling expertise for ${skillName}`);
    console.warn("Updating skills requires backend implementation.");
  };

  return (
      <div className={styles.skills}>
        <h3 className={styles.title}>Skills</h3>

        <div className={styles.skillList}>
          {SKILLS.map((skillInfo) => {
            // This line is now safe because `characterSkills` is guaranteed to be an array.
            const skillData = characterSkills.find(s => s.skillType === skillInfo.key);

            const abilityKeyForScore = skillInfo.ability.toLowerCase() as keyof typeof abilityScores;
            const scoreValue = abilityScores[abilityKeyForScore] ?? 10;
            const baseModifier = getAbilityModifier(scoreValue);

            const isProficient = skillData?.proficient ?? false;
            const hasExpertise = skillData?.expertise ?? false;
            const totalBonus = skillData?.totalBonus ?? baseModifier;

            return (
                <div key={skillInfo.key} className={styles.skillRow}>
                  <div className={styles.checkboxes}>
                    <button
                        type="button"
                        className={`${styles.checkbox} ${isProficient ? styles.checked : ''}`}
                        onClick={() => toggleProficiency(skillInfo.key)}
                        title="Proficient"
                    >
                      {isProficient ? '●' : '○'}
                    </button>
                    <button
                        type="button"
                        className={`${styles.checkbox} ${hasExpertise ? styles.checked : ''}`}
                        onClick={() => toggleExpertise(skillInfo.key)}
                        disabled={!isProficient}
                        title="Expertise"
                    >
                      ◆
                    </button>
                  </div>

                  <div className={styles.modifier}>{formatModifier(totalBonus)}</div>

                  <div className={styles.skillInfo}>
                    <span className={styles.skillName}>{skillInfo.name}</span>
                    <span className={styles.skillAbility}>
                  ({ABILITIES.find((a) => a.key.toUpperCase() === skillInfo.ability)?.abbr})
                </span>
                  </div>
                </div>
            );
          })}
        </div>
      </div>
  );
}
