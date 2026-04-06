import { ABILITIES } from '../../utils/constants';
import { getAbilityModifier, formatModifier } from '../../utils/dndCalculations';
import { useCharacter } from '../../context/CharacterContext';
import styles from './SavingThrows.module.css';

export function SavingThrows() {
  const { currentCharacter, updateNestedCharacter } = useCharacter();

  if (!currentCharacter) return null;

  const toggleProficiency = (abilityKey) => {
    const current = currentCharacter.savingThrows[abilityKey];
    updateNestedCharacter(`savingThrows.${abilityKey}`, !current);
  };

  return (
    <div className={styles.savingThrows}>
      <h3 className={styles.title}>Saving Throws</h3>

      <div className={styles.list}>
        {ABILITIES.map(ability => {
          const score = currentCharacter.abilities[ability.key];
          const isProficient = currentCharacter.savingThrows[ability.key];
          const modifier = getAbilityModifier(score) + 
            (isProficient ? currentCharacter.proficiencyBonus : 0);

          return (
            <div key={ability.key} className={styles.row}>
              <button
                type="button"
                className={`${styles.checkbox} ${isProficient ? styles.checked : ''}`}
                onClick={() => toggleProficiency(ability.key)}
                aria-label={`${ability.name} saving throw ${isProficient ? 'proficient' : 'not proficient'}`}
              >
                ●
              </button>

              <span className={styles.modifier}>
                {formatModifier(modifier)}
              </span>

              <span className={styles.name}>{ability.name}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
