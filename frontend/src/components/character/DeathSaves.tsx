import {useCharacter} from '@/context/CharacterContext';
import {useDebouncedCallback} from '@/hooks/useDebounce'; // Import debounce hook
import styles from './DeathSaves.module.css';

export function DeathSaves() {
  const {currentCharacter, updateCharacter} = useCharacter();

  const debouncedUpdate = useDebouncedCallback(
      (payload: { deathSaveSuccesses?: number; deathSaveFailures?: number }) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, payload);
        }
      },
      500
  );

  if (!currentCharacter) {
    return null;
  }

  const {deathSaveSuccesses, deathSaveFailures} = currentCharacter;

  const handleCircleClick = (
      field: 'deathSaveSuccesses' | 'deathSaveFailures',
      currentValue: number,
      index: number
  ) => {
    const clickedValue = index + 1;
    const newValue = (clickedValue === currentValue) ? currentValue - 1 : clickedValue;

    debouncedUpdate({[field]: newValue});
  };

  const handleReset = () => {
    debouncedUpdate({
      deathSaveSuccesses: 0,
      deathSaveFailures: 0,
    });
  };

  const renderCircles = (
      count: number,
      type: 'success' | 'failure'
  ) => {
    const fieldName = type === 'success' ? 'deathSaveSuccesses' : 'deathSaveFailures';
    return Array.from({length: 3}, (_, i) => (
        <button
            key={i}
            className={`${styles.circle} ${i < count ? styles[type] : ''}`}
            onClick={() => handleCircleClick(fieldName, count, i)}
            aria-label={`${type} ${i + 1} ${i < count ? 'filled' : 'empty'}`}
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
            {renderCircles(deathSaveSuccesses, 'success')}
          </div>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>Failures</span>
          <div className={styles.circles}>
            {renderCircles(deathSaveFailures, 'failure')}
          </div>
        </div>
      </div>
  );
}
