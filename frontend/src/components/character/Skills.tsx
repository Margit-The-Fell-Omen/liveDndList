import {ABILITIES, SKILLS} from '@/utils/constants';
import {formatModifier, getAbilityModifier} from '@/utils/helpers';
import {useCharacter} from '@/context/CharacterContext';
import type {SkillName} from '@/types';
import {Card} from '@/components/common/Card';
import styles from './Skills.module.css';

export function Skills({className}: { className?: string }) {
  const {currentCharacter, toggleSkillProficiency, toggleSkillExpertise} = useCharacter();

  if (!currentCharacter || !currentCharacter.skills || !currentCharacter.abilityScores) {
    return (
        <Card title="Skills" className={className}>
        </Card>
    );
  }

  const {skills: characterSkills, abilityScores, proficiencyBonus} = currentCharacter;

  return (
      <Card title="Skills" className={className}>
        <div className={styles.skillList}>
          {SKILLS.map((skillInfo) => {
            const skillData = characterSkills.find(s => s.skillType === skillInfo.key);
            const abilityKeyForScore = skillInfo.ability.toLowerCase() as keyof typeof abilityScores;
            const scoreValue = abilityScores[abilityKeyForScore] ?? 10;
            const baseModifier = getAbilityModifier(scoreValue);

            let totalBonus = baseModifier;
            const isProficient = skillData?.proficient ?? false;
            const hasExpertise = skillData?.expertise ?? false;
            const skillId = skillData?.id;

            if (isProficient) {
              totalBonus += proficiencyBonus;
            }
            if (hasExpertise) {
              totalBonus += proficiencyBonus;
            }

            return (
                <div key={skillInfo.key} className={styles.skillRow}>
                  <div className={styles.checkboxes}>
                    <button
                        type="button"
                        className={`${styles.checkbox} ${isProficient ? styles.checked : ''}`}
                        onClick={() => skillId && toggleSkillProficiency(skillId, !isProficient)}
                        disabled={!skillId} // Disable if we don't have an ID
                        title="Proficient"
                    >
                      {isProficient && '●'}
                    </button>
                    <button
                        type="button"
                        className={`${styles.checkbox} ${hasExpertise ? styles.checked : ''}`}
                        onClick={() => skillId && toggleSkillExpertise(skillId, !hasExpertise)}
                        disabled={!isProficient || !skillId} // Also disable if not proficient
                        title="Expertise"
                    >
                      {hasExpertise && '◆'}
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
      </Card>
  );
}
