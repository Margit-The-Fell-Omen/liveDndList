import { useCharacter } from '../../context/CharacterContext';
import { Input } from '../common/Input';
import styles from './HitPoints.module.css';

export function HitPoints() {
  const { currentCharacter, updateNestedCharacter } = useCharacter();

  if (!currentCharacter) return null;

  const { hitPoints } = currentCharacter;
  const percentage = (hitPoints.current / hitPoints.maximum) * 100;

  return (
    <div className={styles.hitPoints}>
      <h3 className={styles.title}>Hit Points</h3>
      
      <div className={styles.hpBar}>
        <div 
          className={styles.hpFill} 
          style={{ width: `${Math.max(0, Math.min(100, percentage))}%` }}
          data-status={
            percentage > 50 ? 'healthy' : percentage > 25 ? 'injured' : 'critical'
          }
        />
        <div className={styles.hpText}>
          <span className={styles.current}>{hitPoints.current}</span>
          <span className={styles.separator}>/</span>
          <span className={styles.maximum}>{hitPoints.maximum}</span>
        </div>
      </div>

      <div className={styles.inputs}>
        <Input
          label="Current HP"
          type="number"
          value={hitPoints.current}
          onChange={(e) => updateNestedCharacter('hitPoints.current', parseInt(e.target.value, 10) || 0)}
          min="0"
        />
        
        <Input
          label="Max HP"
          type="number"
          value={hitPoints.maximum}
          onChange={(e) => updateNestedCharacter('hitPoints.maximum', parseInt(e.target.value, 10) || 1)}
          min="1"
        />
        
        <Input
          label="Temp HP"
          type="number"
          value={hitPoints.temporary}
          onChange={(e) => updateNestedCharacter('hitPoints.temporary', parseInt(e.target.value, 10) || 0)}
          min="0"
        />
      </div>

      {hitPoints.temporary > 0 && (
        <div className={styles.tempHpIndicator}>
          +{hitPoints.temporary} Temporary HP
        </div>
      )}
    </div>
  );
}
