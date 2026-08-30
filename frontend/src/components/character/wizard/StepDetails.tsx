import {useEffect, useState} from 'react';
import type {AbilityScores, CharacterAlignment} from '@/types';
import {Input} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import styles from './StepDetails.module.css';

const ALIGNMENTS: { value: CharacterAlignment; label: string }[] = [
  {value: 'LAWFUL_GOOD', label: 'Lawful Good'},
  {value: 'NEUTRAL_GOOD', label: 'Neutral Good'},
  {value: 'CHAOTIC_GOOD', label: 'Chaotic Good'},
  {value: 'LAWFUL_NEUTRAL', label: 'Lawful Neutral'},
  {value: 'TRUE_NEUTRAL', label: 'True Neutral'},
  {value: 'CHAOTIC_NEUTRAL', label: 'Chaotic Neutral'},
  {value: 'LAWFUL_EVIL', label: 'Lawful Evil'},
  {value: 'NEUTRAL_EVIL', label: 'Neutral Evil'},
  {value: 'CHAOTIC_EVIL', label: 'Chaotic Evil'},
];

const ABILITY_KEYS: (keyof AbilityScores)[] = [
  'strength', 'dexterity', 'constitution',
  'intelligence', 'wisdom', 'charisma',
];

const STANDARD_ARRAY = [15, 14, 13, 12, 10, 8];
const POINT_BUY_COSTS: Record<number, number> = {
  8: 0,
  9: 1,
  10: 2,
  11: 3,
  12: 4,
  13: 5,
  14: 7,
  15: 9
};

type GenMode = 'MANUAL' | 'STANDARD' | 'POINT_BUY' | 'ROLL';

interface StepDetailsProps {
  name: string;
  onNameChange: (v: string) => void;
  alignment: CharacterAlignment | '';
  onAlignmentChange: (v: CharacterAlignment | '') => void;
  abilityScores: AbilityScores;
  onScoreChange: (key: keyof AbilityScores, value: string) => void;
  hitPoints: number;
}

export function StepDetails({
                              name,
                              onNameChange,
                              alignment,
                              onAlignmentChange,
                              abilityScores,
                              onScoreChange,
                              hitPoints,
                            }: StepDetailsProps) {
  const [mode, setMode] = useState<GenMode>('STANDARD');

  useEffect(() => {
    if (mode === 'STANDARD' && abilityScores.strength === 10) {
      ABILITY_KEYS.forEach(key => onScoreChange(key, '0'));
    }
  }, []);


  // --- Point Buy Logic ---
  const calculatePointsRemaining = () => {
    let spent = 0;
    for (const key of ABILITY_KEYS) {
      const score = abilityScores[key];
      if (score < 8 || score > 15) return -1;
      spent += POINT_BUY_COSTS[score] || 0;
    }
    return 27 - spent;
  };

  // --- Dice Rolling Logic ---
  const roll4d6DropLowest = () => {
    ABILITY_KEYS.forEach((key) => {
      const rolls = Array.from({length: 4}, () => Math.floor(Math.random() * 6) + 1);
      rolls.sort((a, b) => a - b);
      const total = rolls[1] + rolls[2] + rolls[3];
      onScoreChange(key, String(total));
    });
  };

  const rollWildMagic = () => {
    ABILITY_KEYS.forEach((key) => {
      const total = Math.floor(Math.random() * 20) + 1;
      onScoreChange(key, String(total));
    });
  };

  // --- Standard Array Logic ---
  const getUnusedStandardValues = () => {
    const currentValues = Object.values(abilityScores);
    const unused = [...STANDARD_ARRAY];
    currentValues.forEach(val => {
      const idx = unused.indexOf(val);
      if (idx !== -1) unused.splice(idx, 1);
    });
    return unused;
  };

  const unusedStandard = getUnusedStandardValues();

  // Mode switcher helper
  const handleModeChange = (newMode: GenMode) => {
    setMode(newMode);
    if (newMode === 'STANDARD') {
      ABILITY_KEYS.forEach(key => onScoreChange(key, '0')); // 0 means "--"
    } else if (newMode === 'POINT_BUY') {
      ABILITY_KEYS.forEach(key => onScoreChange(key, '8'));
    } else if (newMode === 'MANUAL') {
      ABILITY_KEYS.forEach(key => onScoreChange(key, '10'));
    }
  };

  return (
      <div className={styles.container}>
        <div className={styles.section}>
          <h3 className={styles.sectionTitle}>Identity</h3>
          <div className={styles.identityGrid}>
            <Input
                label="Character Name"
                value={name}
                onChange={e => onNameChange(e.target.value)}
                required
                fullWidth
            />
            <div className={styles.selectWrapper}>
              <label className={styles.selectLabel}>Alignment</label>
              <select
                  className={styles.select}
                  value={alignment}
                  onChange={e => onAlignmentChange(e.target.value as CharacterAlignment | '')}
              >
                <option value="">— None —</option>
                {ALIGNMENTS.map(a => (
                    <option key={a.value} value={a.value}>{a.label}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        <div className={styles.section}>
          <div className={styles.sectionTitleRow}>
            <h3 className={styles.sectionTitle}>
              Ability Scores
              <span className={styles.hpPreview}>→ Starting HP: {hitPoints}</span>
            </h3>
            <div className={styles.modeTabs}>
              <button type="button" className={mode === 'STANDARD' ? styles.tabActive : styles.tab}
                      onClick={() => handleModeChange('STANDARD')}>Standard Array
              </button>
              <button type="button" className={mode === 'POINT_BUY' ? styles.tabActive : styles.tab}
                      onClick={() => handleModeChange('POINT_BUY')}>Point Buy
              </button>
              <button type="button" className={mode === 'ROLL' ? styles.tabActive : styles.tab}
                      onClick={() => handleModeChange('ROLL')}>Roll
              </button>
              <button type="button" className={mode === 'MANUAL' ? styles.tabActive : styles.tab}
                      onClick={() => handleModeChange('MANUAL')}>Manual
              </button>
            </div>
          </div>

          {/* Mode-specific Header/Controls */}
          <div className={styles.modeControls}>
            {mode === 'POINT_BUY' && (
                <div className={styles.pointBuyHeader}>
                  Points Remaining: <span
                    className={calculatePointsRemaining() < 0 ? styles.errorText : styles.highlightText}>{calculatePointsRemaining()}</span> /
                  27
                </div>
            )}
            {mode === 'STANDARD' && (
                <div className={styles.standardHeader}>
                  Available:
                  {STANDARD_ARRAY.map((val, idx) => {
                    const countInArray = STANDARD_ARRAY.filter(v => v === val).length;
                    const countInUsed = Object.values(abilityScores).filter(v => v === val).length;
                    const isUsed = countInUsed >= countInArray && !unusedStandard.includes(val);

                    return (
                        <span key={`${val}-${idx}`}
                              className={isUsed ? styles.chipUsed : styles.chipAvailable}>
                          {val}
                        </span>
                    )
                  })}
                </div>
            )}
            {mode === 'ROLL' && (
                <div className={styles.rollHeader}>
                  <Button size="small" onClick={roll4d6DropLowest}>Roll 4d6 (Drop Lowest)</Button>
                  <Button size="small" variant="danger" onClick={rollWildMagic}>1d20 Wild Magic
                    🎲</Button>
                </div>
            )}
          </div>

          <div className={styles.scoresGrid}>
            {ABILITY_KEYS.map(key => {
              const val = abilityScores[key];
              const mod = Math.floor((val - 10) / 2);

              return (
                  <div key={key} className={styles.scoreItem}>
                    {mode === 'STANDARD' ? (
                        <div className={styles.standardSelectWrapper}>
                          <label
                              className={styles.selectLabel}>{key.charAt(0).toUpperCase() + key.slice(1)}</label>
                          <select
                              className={styles.selectScore}
                              value={val === 0 ? '' : val}
                              onChange={e => onScoreChange(key, e.target.value)}
                          >
                            <option value="" disabled>--</option>
                            {val !== 0 && <option value={val}>{val}</option>}
                            {unusedStandard.map((uVal, i) => (
                                <option key={`opt-${uVal}-${i}`} value={uVal}>{uVal}</option>
                            ))}
                          </select>
                        </div>
                    ) : (
                        <Input
                            label={key.charAt(0).toUpperCase() + key.slice(1)}
                            type="number"
                            value={val === 0 ? '' : val} // Fallback just in case
                            min={mode === 'POINT_BUY' ? 8 : 1}
                            max={mode === 'POINT_BUY' ? 15 : 30}
                            onChange={e => onScoreChange(key, e.target.value)}
                            disabled={mode === 'ROLL'}
                            required
                        />
                    )}
                    <span className={styles.modifier}>
                      {val === 0 ? '--' : (mod >= 0 ? `+${mod}` : `${mod}`)}
                    </span>
                  </div>
              )
            })}
          </div>
        </div>
      </div>
  );
}
