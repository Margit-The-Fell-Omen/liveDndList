import {useCharacter} from '../../context/CharacterContext';
import styles from './DeathSaves.module.css';

export function DeathSaves() {
  const {currentCharacter, updateNestedCharacter} = useCharacter();

  if (!currentCharacter) {
    return null;
  }

  const {deathSaves} = currentCharacter;

  const handleSuccessClick = (index) => {
    const newValue = index < deathSaves.successes ? index : index + 1;
    updateNestedCharacter('deathSaves.successes', newValue);
  };

  const handleFailureClick = (index) => {
    const newValue = index < deathSaves.failures ? index : index + 1;
    updateNestedCharacter('deathSaves.failures', newValue);
  };

  const resetDeathSaves = () => {
    updateNestedCharacter('deathSaves', {successes: 0, failures: 0});
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
            ↺
          </button>
        </div>

        <div className={styles.saves}>
          <div className={styles.saveRow}>
            <span className={styles.label}>Successes</span>
            <div className={styles.circles}>
              {[0, 1, 2].map(index => (
                  <button
                      key={index}
                      type="button"
                      className={`${styles.circle} ${styles.success} ${
                          index < deathSaves.successes ? styles.filled : ''
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
              {[0, 1, 2].map(index => (
                  <button
                      key={index}
                      type="button"
                      className={`${styles.circle} ${styles.failure} ${
                          index < deathSaves.failures ? styles.filled : ''
                      }`}
                      onClick={() => handleFailureClick(index)}
                      aria-label={`Failure ${index + 1}`}
                  />
              ))}
            </div>
          </div>
        </div>

        {deathSaves.successes >= 3 && (
            <div className={styles.statusMessage + ' ' + styles.stable}>
              Stabilized! 🎉
            </div>
        )}

        {deathSaves.failures >= 3 && (
            <div className={styles.statusMessage + ' ' + styles.dead}>
              Character has died 💀
            </div>
        )}
      </div>
  );
}