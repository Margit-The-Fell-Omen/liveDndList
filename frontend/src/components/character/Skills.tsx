// src/components/character/Skills.tsx

import {ABILITIES, SKILLS} from '@/utils/constants';
import {formatModifier, getAbilityModifier} from '@/utils/helpers';
import {useCharacter} from '@/context/CharacterContext';
import type {SkillName} from '@/types';
import styles from './Skills.module.css';

export function Skills() {
  const {currentCharacter, updateCharacter} = useCharacter();

  if (!currentCharacter) {
    return null;
  }

  // Destructure the correct properties
  const {skills: characterSkills, abilityScores} = currentCharacter;

  // These update functions need to be implemented if you want to allow
  // users to change proficiency/expertise from the UI.
  const toggleProficiency = (skillName: SkillName) => {
    console.log(`Toggling proficiency for ${skillName}`);
    console.warn("Updating skills requires a 'skills' array in the backend's CharacterUpdateRequest DTO.");
  };

  const toggleExpertise = (skillName: SkillName) => {
    console.log(`Toggling expertise for ${skillName}`);
    console.warn("Updating skills requires a 'skills' array in the backend's CharacterUpdateRequest DTO.");
  };

  return (
      <div className={styles.skills}>
        <h3 className={styles.title}>Skills</h3>

        <div className={styles.skillList}>
          {/* We map over the master list of ALL skills from constants */}
          {SKILLS.map((skillInfo) => {
            // FIX 1: Find this skill's data within the character's skills array
            const skillData = characterSkills.find(s => s.skillType === skillInfo.key);

            // Get the base ability modifier for display, but use the backend's totalBonus for the main number
            const abilityKey = skillInfo.ability.toLowerCase() as keyof typeof abilityScores;
            const baseModifier = abilityScores[abilityKey.replace('Modifier', '')] ? getAbilityModifier(abilityScores[abilityKey.replace('Modifier', '')]) : 0;

            // Use the data from the backend if it exists, otherwise provide defaults
            const isProficient = skillData?.proficient ?? false;
            const hasExpertise = skillData?.expertise ?? false;
            // Use the pre-calculated bonus from the backend for accuracy
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
                        disabled={!isProficient} // Can't have expertise without proficiency
                        title="Expertise"
                    >
                      ◆
                    </button>
                  </div>

                  {/* FIX 2: Display the totalBonus calculated by the backend */}
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
