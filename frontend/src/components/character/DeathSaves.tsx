// src/components/character/DeathSaves.tsx

import {useCharacter} from '@/context/CharacterContext';
import styles from './DeathSaves.module.css';

export function DeathSaves() {
  // Use the correct updateCharacter function
  const {currentCharacter, updateCharacter} = useCharacter();

  if (!currentCharacter) {
    return null;
  }

  // FIX 1: Destructure the correct top-level properties
  const {deathSaveSuccesses, deathSaveFailures} = currentCharacter;

  // --- Event Handlers ---
  const handleSuccessClick = (index: number) => {
    // If you click the same circle or a previous one, it resets to that many successes.
    // If you click an empty one, it sets it to that many + 1.
    const newValue = index < deathSaveSuccesses ? index : index + 1;
    updateCharacter(currentCharacter.id, {deathSaveSuccesses: newValue});
  };

  const handleFailureClick = (index: number) => {
    const newValue = index < deathSaveFailures ? index : index + 1;
    updateCharacter(currentCharacter.id, {deathSaveFailures: newValue});
  };

  const resetDeathSaves = () => {
    // Send both properties in a single update call
    updateCharacter(currentCharacter.id, {
      deathSaveSuccesses: 0,
      deathSaveFailures: 0,
    });
  };

  return (
      <div className={styles.deathSaves}>
        <div className={styles.header}>
          <h3 className={styles.title}>Death Saves</h3>
          <button
              type="button"
              className={styles.resetButton}
              onClick={resetDeathSaves}
              title="Reset death saves"
          >
            Reset
          </button>
        </div>

        <div className={styles.saves}>
          <div className={styles.saveRow}>
            <span className={styles.label}>Successes</span>
            <div className={styles.circles}>
              {/* FIX 2: Check against the correct top-level property */}
              {[0, 1, 2].map((index) => (
                  <button
                      key={`success-${index}`}
                      type="button"
                      className={`${styles.circle} ${styles.success} ${
                          index < deathSaveSuccesses ? styles.filled : ''
                      }`}
                      onClick={() => handleSuccessClick(index)}
                      aria-label={`Success ${index + 1}`}
                  />
              ))}
            </div>
          </div>

          <div className={styles.saveRow}>
            <span className={styles.label}>Failures</span>
            <div className={styles.circles}>
              {[0, 1, 2].map((index) => (
                  <button
                      key={`failure-${index}`}
                      type="button"
                      className={`${styles.circle} ${styles.failure} ${
                          index < deathSaveFailures ? styles.filled : ''
                      }`}
                      onClick={() => handleFailureClick(index)}
                      aria-label={`Failure ${index + 1}`}
                  />
              ))}
            </div>
          </div>
        </div>

        {/* FIX 3: Check against the correct properties for status messages */}
        {deathSaveSuccesses >= 3 && (
            <div className={`${styles.statusMessage} ${styles.stable}`}>Stabilized! 🎉</div>
        )}

        {deathSaveFailures >= 3 && (
            <div className={`${styles.statusMessage} ${styles.dead}`}>Character has died 💀</div>
        )}
      </div>
  );
}
