// src/components/character/HitPoints.tsx

import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './HitPoints.module.css';

type HpStatus = 'healthy' | 'injured' | 'critical';

export function HitPoints() {
  // Use the correct updateCharacter function
  const {currentCharacter, updateCharacter} = useCharacter();

  const debouncedUpdate = useDebouncedCallback(
      (key: 'currentHitPoints' | 'maxHitPoints' | 'temporaryHitPoints', value: number) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, {[key]: value});
        }
      },
      300 // A shorter delay is good for HP
  );

  if (!currentCharacter) {
    return null;
  }

  // FIX 1: Destructure the correct top-level properties
  const {currentHitPoints, maxHitPoints, temporaryHitPoints} = currentCharacter;

  // Calculate percentage based on the correct properties
  const percentage = maxHitPoints > 0 ? (currentHitPoints / maxHitPoints) * 100 : 0;

  const getStatus = (): HpStatus => {
    if (percentage > 50) return 'healthy';
    if (percentage > 25) return 'injured';
    return 'critical';
  };

  return (
      <div className={styles.hitPoints}>
        <h3 className={styles.title}>Hit Points</h3>

        <div className={styles.hpBar}>
          <div
              className={styles.hpFill}
              style={{width: `${Math.max(0, Math.min(100, percentage))}%`}}
              data-status={getStatus()}
          />
          <div className={styles.hpText}>
            {/* FIX 2: Display the correct properties */}
            <span className={styles.current}>{currentHitPoints}</span>
            <span className={styles.separator}>/</span>
            <span className={styles.maximum}>{maxHitPoints}</span>
          </div>
        </div>

        <div className={styles.inputs}>
          <Input
              label="Current HP"
              type="number"
              // Use defaultValue for debounced inputs
              defaultValue={currentHitPoints}
              onChange={(e) =>
                  debouncedUpdate('currentHitPoints', parseInt(e.target.value, 10) || 0)
              }
              min={0}
          />

          <Input
              label="Max HP"
              type="number"
              defaultValue={maxHitPoints}
              onChange={(e) =>
                  debouncedUpdate('maxHitPoints', parseInt(e.target.value, 10) || 1)
              }
              min={1}
          />

          <Input
              label="Temp HP"
              type="number"
              defaultValue={temporaryHitPoints}
              onChange={(e) =>
                  debouncedUpdate('temporaryHitPoints', parseInt(e.target.value, 10) || 0)
              }
              min={0}
          />
        </div>

        {/* FIX 3: Check the correct temporaryHitPoints property */}
        {temporaryHitPoints > 0 && (
            <div className={styles.tempHpIndicator}>+{temporaryHitPoints} Temporary HP</div>
        )}
      </div>
  );
}
