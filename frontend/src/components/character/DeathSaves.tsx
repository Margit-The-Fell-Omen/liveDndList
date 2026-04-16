import {useCharacter} from '@/context/CharacterContext';
import styles from './DeathSaves.module.css';

export function DeathSaves() {
  const {currentCharacter, updateCharacter} = useCharacter();

  if (!currentCharacter) {
    return null;
  }

  const {deathSaveSuccesses, deathSaveFailures} = currentCharacter;

  const handleUpdate = (field: 'deathSaveSuccesses' | 'deathSaveFailures', value: number) => {
    if (value < 0 || value > 3) return;
    updateCharacter(currentCharacter.id, {[field]: value});
  };

  const handleReset = () => {
    updateCharacter(currentCharacter.id, {
      deathSaveSuccesses: 0,
      deathSaveFailures: 0
    });
  };

  const renderCircles = (count: number, total: number, type: 'success' | 'failure') => {
    return Array.from({length: total}, (_, i) => (
        <button
            key={i}
            className={`${styles.circle} ${i < count ? styles[type] : ''}`}
            onClick={() => handleUpdate(type === 'success' ? 'deathSaveSuccesses' : 'deathSaveFailures', i < count ? i : i + 1)}
            aria-label={`${type} ${i + 1}`}
        />
    ));
  };

  return (
      <div className={styles.deathSaves}>
        <div className={styles.header}>
          <h4 className={styles.title}>Death Saves</h4>
          <button onClick={handleReset} className={styles.resetButton}>Reset</button>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>Successes</span>
          <div className={styles.circles}>
            {renderCircles(deathSaveSuccesses, 3, 'success')}
          </div>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>Failures</span>
          <div className={styles.circles}>
            {renderCircles(deathSaveFailures, 3, 'failure')}
          </div>
        </div>
      </div>
  );
}
