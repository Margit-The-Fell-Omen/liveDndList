import { SKILLS, ABILITIES } from '../../utils/constants';
import { getSkillModifier, formatModifier } from '../../utils/dndCalculations';
import { useCharacter } from '../../context/CharacterContext';
import styles from './Skills.module.css';

export function Skills() {
  const { currentCharacter, updateNestedCharacter } = useCharacter();
  
  if (!currentCharacter) return null;

  const toggleProficiency = (skillKey) => {
    const current = currentCharacter.skills[skillKey];
    updateNestedCharacter(`skills.${skillKey}.proficient`, !current.proficient);
  };

  const toggleExpertise = (skillKey) => {
    const current = currentCharacter.skills[skillKey];
    if (!current.proficient) return; // Can't have expertise without proficiency
    
    updateNestedCharacter(`skills.${skillKey}.expertise`, !current.expertise);
  };

  return (
    <div className={styles.skills}>
      <h3 className={styles.title}>Skills</h3>
      
      <div className={styles.skillList}>
        {SKILLS.map(skill => {
          const abilityScore = currentCharacter.abilities[skill.ability];
          const skillData = currentCharacter.skills[skill.key];
          const modifier = getSkillModifier(
            abilityScore,
            currentCharacter.proficiencyBonus,
            skillData.proficient,
            skillData.expertise
          );

          return (
            <div key={skill.key} className={styles.skillRow}>
              <div className={styles.checkboxes}>
                <button
                  type="button"
                  className={`${styles.checkbox} ${skillData.proficient ? styles.checked : ''}`}
                  onClick={() => toggleProficiency(skill.key)}
                  title="Proficient"
                >
                  ●
                </button>
                <button
                  type="button"
                  className={`${styles.checkbox} ${skillData.expertise ? styles.checked : ''}`}
                  onClick={() => toggleExpertise(skill.key)}
                  disabled={!skillData.proficient}
                  title="Expertise"
                >
                  ◆
                </button>
              </div>

              <div className={styles.modifier}>
                {formatModifier(modifier)}
              </div>

              <div className={styles.skillInfo}>
                <span className={styles.skillName}>{skill.name}</span>
                <span className={styles.skillAbility}>
                  ({ABILITIES.find(a => a.key === skill.ability)?.abbr})
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
