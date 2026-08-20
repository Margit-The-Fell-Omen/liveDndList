import {ABILITIES, SKILLS} from '@/utils/constants';
import {formatModifier} from '@/utils/helpers';
import {useCharacter} from '@/context/CharacterContext';
import {Card} from '@/components/common/Card';
import styles from './Skills.module.css';

export function Skills({className}: { className?: string }) {
  const {currentCharacter} = useCharacter();

  if (!currentCharacter?.skills) {
    return <Card title="Skills" className={className}/>;
  }

  const {skills: characterSkills, proficiencyBonus} = currentCharacter;

  const skillMap = new Map(characterSkills.map(s => [s.skillType, s]));

  const grantedProficiencies = new Set(
      characterSkills.filter(s => s.proficient).map(s => s.skillType)
  );
  const grantedExpertise = new Set(
      characterSkills.filter(s => s.expertise).map(s => s.skillType)
  );

  return (
      <Card title="Skills" className={className}>
        <div className={styles.skillList}>
          {SKILLS.map(skillInfo => {
            const skillData = skillMap.get(skillInfo.key);
            const totalBonus = skillData?.totalBonus ?? 0;
            const isProficient = skillData?.proficient ?? false;
            const hasExpertise = skillData?.expertise ?? false;

            const isGranted = grantedProficiencies.has(skillInfo.key);
            const isExpertiseGranted = grantedExpertise.has(skillInfo.key);

            const abilityAbbr =
                ABILITIES.find(a => a.name.toUpperCase() === skillInfo.ability)?.abbr ?? '???';

            return (
                <div key={skillInfo.key} className={styles.skillRow}>
                  <div className={styles.checkboxes}>
                    <span
                        className={`${styles.checkbox} ${isProficient ? styles.checked : ''} ${isGranted ? styles.granted : ''}`}
                        title={isGranted ? 'Granted by feature (locked)' : isProficient ? 'Proficient' : 'Not proficient'}
                        aria-label={`${skillInfo.name} proficiency ${isProficient ? 'active' : 'inactive'}${isGranted ? ' (granted)' : ''}`}
                    >
                  {isProficient && '●'}
                </span>

                    <span
                        className={`${styles.checkbox} ${styles.expertiseBox} ${hasExpertise ? styles.checked : ''} ${isExpertiseGranted ? styles.granted : ''}`}
                        title={isExpertiseGranted ? 'Expertise granted by feature (locked)' : hasExpertise ? 'Expertise' : 'No expertise'}
                        aria-label={`${skillInfo.name} expertise ${hasExpertise ? 'active' : 'inactive'}`}
                    >
                  {hasExpertise && '◆'}
                </span>
                  </div>

                  <div className={styles.modifier}>{formatModifier(totalBonus)}</div>

                  <div className={styles.skillInfo}>
                    <span className={styles.skillName}>{skillInfo.name}</span>
                    <span className={styles.skillAbility}>({abilityAbbr})</span>
                  </div>

                  {isGranted && (
                      <span className={styles.grantedTag}
                            title="Granted by class/race/background feature">
                  ✦
                </span>
                  )}
                </div>
            );
          })}
        </div>
      </Card>
  );
}
