import { useState } from 'react';
import { getAbilityModifier, formatModifier, validateAbilityScore } from '../../utils/dndCalculations';
import { Input } from '../common/Input';
import styles from './AbilityScore.module.css';

export function AbilityScore({ 
  ability, 
  value, 
  onChange,
  proficient = false,
  onProficiencyChange
}) {
  const [error, setError] = useState(null);
  const modifier = getAbilityModifier(value);

  const handleChange = (e) => {
    const newValue = e.target.value;
    
    // Allow empty for typing
    if (newValue === '') {
      onChange(e.target.name, '');
      setError(null);
      return;
    }

    const validation = validateAbilityScore(newValue);
    if (validation.valid) {
      onChange(e.target.name, validation.value);
      setError(null);
    } else {
      setError(validation.message);
    }
  };

  const handleBlur = () => {
    // If empty on blur, set to default
    if (value === '' || value < 1) {
      onChange(ability.key, 10);
      setError(null);
    }
  };

  return (
    <div className={styles.abilityScore}>
      <div className={styles.header}>
        <label className={styles.label}>
          {ability.name}
          <span className={styles.abbr}>({ability.abbr})</span>
        </label>
        {onProficiencyChange && (
          <button
            type="button"
            className={`${styles.proficiency} ${proficient ? styles.proficient : ''}`}
            onClick={() => onProficiencyChange(ability.key)}
            title={`Saving throw ${proficient ? 'proficient' : 'not proficient'}`}
          >
            ●
          </button>
        )}
      </div>

      <div className={styles.scoreWrapper}>
        <Input
          name={ability.key}
          type="number"
          value={value}
          onChange={handleChange}
          onBlur={handleBlur}
          error={error}
          className={styles.scoreInput}
          min="1"
          max="30"
        />
        
        <div className={styles.modifier} data-positive={modifier >= 0}>
          {formatModifier(modifier)}
        </div>
      </div>
    </div>
  );
}
