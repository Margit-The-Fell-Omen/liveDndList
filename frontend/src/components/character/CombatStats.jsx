import { useCharacter } from '../../context/CharacterContext';
import { Input } from '../common/Input';
import { HIT_DICE_BY_CLASS } from '../../utils/constants';
import styles from './CombatStats.module.css';

export function CombatStats() {
  const { currentCharacter, updateCharacter, updateNestedCharacter } = useCharacter();

  if (!currentCharacter) return null;

  const hitDie = HIT_DICE_BY_CLASS[currentCharacter.class] || 'd8';

  return (
    <div className={styles.combatStats}>
      <h3 className={styles.title}>Combat</h3>

      <div className={styles.grid}>
        <div className={styles.stat}>
          <label className={styles.label}>Armor Class</label>
          <Input
            type="number"
            value={currentCharacter.armorClass}
            onChange={(e) => updateCharacter({ 
              armorClass: parseInt(e.target.value, 10) || 10 
            })}
            min="0"
            className={styles.input}
          />
        </div>

        <div className={styles.stat}>
          <label className={styles.label}>Initiative</label>
          <Input
            type="number"
            value={currentCharacter.initiative}
            onChange={(e) => updateCharacter({ 
              initiative: parseInt(e.target.value, 10) || 0 
            })}
            className={styles.input}
          />
        </div>

        <div className={styles.stat}>
          <label className={styles.label}>Speed</label>
          <div className={styles.speedInput}>
            <Input
              type="number"
              value={currentCharacter.speed}
              onChange={(e) => updateCharacter({ 
                speed: parseInt(e.target.value, 10) || 30 
              })}
              min="0"
              className={styles.input}
            />
            <span className={styles.unit}>ft</span>
          </div>
        </div>
      </div>

      <div className={styles.hitDice}>
        <div className={styles.hitDiceHeader}>
          <span className={styles.label}>Hit Dice</span>
          <span className={styles.hitDieType}>{hitDie}</span>
        </div>
        
        <div className={styles.hitDiceInputs}>
          <Input
            type="number"
            value={currentCharacter.hitDice.current}
            onChange={(e) => updateNestedCharacter(
              'hitDice.current', 
              Math.min(
                parseInt(e.target.value, 10) || 0,
                currentCharacter.hitDice.total
              )
            )}
            min="0"
            max={currentCharacter.hitDice.total}
          />
          <span className={styles.separator}>/</span>
          <Input
            type="number"
            value={currentCharacter.hitDice.total}
            onChange={(e) => updateNestedCharacter(
              'hitDice.total', 
              parseInt(e.target.value, 10) || 1
            )}
            min="1"
          />
        </div>
      </div>
    </div>
  );
}
