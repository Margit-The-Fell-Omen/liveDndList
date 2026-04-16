// src/components/character/HitPoints.tsx

import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import {DeathSaves} from './DeathSaves';
import {Card} from '@/components/common/Card';
import styles from './HitPoints.module.css';

type HpStatus = 'healthy' | 'injured' | 'critical';

// --- THE FIX IS HERE ---
// Destructure className from the props object.
export function HitPoints({className}: { className?: string }) {
  const {currentCharacter, updateCharacter} = useCharacter();

  const debouncedUpdate = useDebouncedCallback(
      (key: 'currentHitPoints' | 'maxHitPoints' | 'temporaryHitPoints', value: number) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, {[key]: value});
        }
      },
      300
  );

  if (!currentCharacter) {
    return null;
  }

  const {currentHitPoints, maxHitPoints, temporaryHitPoints} = currentCharacter;
  const percentage = maxHitPoints > 0 ? (currentHitPoints / maxHitPoints) * 100 : 0;

  const getStatus = (): HpStatus => {
    if (percentage > 50) return 'healthy';
    if (percentage > 25) return 'injured';
    return 'critical';
  };

  return (
      // The `className` variable is now correctly defined and can be used here.
      <Card title="Hit Points" className={className}>
        <div className={styles.hpBar}>
          <div
              className={styles.hpFill}
              style={{width: `${Math.max(0, Math.min(100, percentage))}%`}}
              data-status={getStatus()}
          />
          <div className={styles.hpText}>
            <span className={styles.current}>{currentHitPoints}</span>
            <span className={styles.separator}>/</span>
            <span className={styles.maximum}>{maxHitPoints}</span>
          </div>
        </div>

        <div className={styles.inputs}>
          <Input label="Current HP" type="number" defaultValue={currentHitPoints}
                 onChange={(e) => debouncedUpdate('currentHitPoints', parseInt(e.target.value, 10) || 0)}/>
          <Input label="Max HP" type="number" defaultValue={maxHitPoints}
                 onChange={(e) => debouncedUpdate('maxHitPoints', parseInt(e.target.value, 10) || 1)}/>
          <Input label="Temp HP" type="number" defaultValue={temporaryHitPoints}
                 onChange={(e) => debouncedUpdate('temporaryHitPoints', parseInt(e.target.value, 10) || 0)}/>
        </div>

        {temporaryHitPoints > 0 && (
            <div className={styles.tempHpIndicator}>+{temporaryHitPoints} Temporary HP</div>
        )}

        <DeathSaves/>
      </Card>
  );
}
