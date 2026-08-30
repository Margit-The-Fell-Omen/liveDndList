// src/components/character/FeatureChoiceModal.tsx
import {useMemo, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {Character, PendingChoiceResponse} from '@/types';
import {SKILLS} from '@/utils/constants';
import styles from './FeatureChoiceModal.module.css';

interface FeatureChoiceModalProps {
  isOpen: boolean;
  choice: PendingChoiceResponse;
  character: Character;
  onSubmit: (selectedValues: unknown[]) => Promise<void>;
  onClear: () => void;
  onClose: () => void;
  saving?: boolean;
}

// ── Option resolution ─────────────────────────────────────────

interface OptionItem {
  value: string;
  label: string;
  disabled?: boolean;
  disabledReason?: string;
}

function extractValidOptions(
    choice: PendingChoiceResponse,
    dictionary: { key: string; name: string }[]
): string[] | undefined {
  const filter = (choice.optionsFilter as any) ?? {};

  if (Array.isArray(filter.fromList)) {
    return filter.fromList;
  }

  if (choice.description) {
    const desc = choice.description.toLowerCase();
    const found = dictionary
        .filter(item => desc.includes(item.name.toLowerCase()))
        .map(item => item.key);

    if (found.length > 0) {
      return found;
    }
  }

  return undefined;
}

function resolveOptions(
    choice: PendingChoiceResponse,
    character: Character
): OptionItem[] {
  const filter = (choice.optionsFilter as Record<string, unknown> | null) ?? {};

  switch (choice.optionsSource) {
    case 'INLINE': {
      const opts = (filter['options'] as string[] | undefined) ?? [];
      return opts.map(o => ({value: o, label: formatOptionLabel(o)}));
    }

    case 'SKILL_LIST': {
      const fromList = extractValidOptions(choice, SKILLS);
      const onlyProficient = filter['onlyProficient'] as boolean | undefined;
      const excludeChosen = filter['excludeChosen'] as boolean | undefined;

      const grantedProficiencies = new Set(
          character.skills.filter(s => s.proficient).map(s => s.skillType)
      );
      const grantedExpertise = new Set(
          character.skills.filter(s => s.expertise).map(s => s.skillType)
      );

      const alreadyChosenOnFeature = collectAlreadyChosenValues(choice, character);

      const filtered = SKILLS.filter(s => {
        if (fromList && !fromList.includes(s.key)) return false;
        return true;
      });

      return filtered.map(s => {
        let disabled = false;
        let disabledReason: string | undefined;

        if (onlyProficient && !grantedProficiencies.has(s.key)) {
          disabled = true;
          disabledReason = 'Not proficient';
        }

        if (onlyProficient && grantedExpertise.has(s.key)) {
          disabled = true;
          disabledReason = 'Already has expertise';
        }

        if (!onlyProficient && grantedProficiencies.has(s.key)) {
          disabled = true;
          disabledReason = 'Already proficient';
        }

        if (excludeChosen && alreadyChosenOnFeature.has(s.key)) {
          disabled = true;
          disabledReason = 'Already chosen';
        }

        return {
          value: s.key,
          label: s.name,
          disabled,
          disabledReason,
        };
      });
    }

    case 'ABILITY_LIST': {
      const ABILITIES = [
        {key: 'STR', name: 'Strength'},
        {key: 'DEX', name: 'Dexterity'},
        {key: 'CON', name: 'Constitution'},
        {key: 'INT', name: 'Intelligence'},
        {key: 'WIS', name: 'Wisdom'},
        {key: 'CHA', name: 'Charisma'},
      ];

      const fromList = extractValidOptions(choice, ABILITIES);

      const filtered = ABILITIES.filter(a => {
        if (fromList && !fromList.includes(a.key)) return false;
        return true;
      });

      return filtered.map(a => ({value: a.key, label: a.name}));
    }

    case 'LANGUAGE_LIST':
    case 'TOOL_LIST':
      return []; // freeform mode

    case 'FEAT_LIST':
    case 'WEAPON_LIST':
    case 'ARMOR_LIST':
    case 'SPELL_LIST':
      return [];

    default:
      return [];
  }
}

function collectAlreadyChosenValues(
    currentChoice: PendingChoiceResponse,
    character: Character
): Set<string> {
  const already = new Set<string>();

  const feature = character.features?.find(
      f => f.id === currentChoice.characterFeatureId
  );
  if (!feature) return already;

  for (const answer of feature.choices) {
    if (answer.choiceKey === currentChoice.choiceKey) continue;
    const values = answer.selectedValues;
    if (Array.isArray(values)) {
      for (const v of values) {
        if (typeof v === 'string') {
          already.add(v);
        }
      }
    }
  }

  return already;
}

function formatOptionLabel(value: string): string {
  return value
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
}

// ── Specialized 2024 ASI Distribution Picker ──────────────────

function AsiDistributionPicker({
                                 options,
                                 distributions,
                                 selected,
                                 onChange,
                               }: {
  options: OptionItem[];
  distributions: string[];
  selected: Record<string, any>[];
  onChange: (val: Record<string, any>[]) => void;
}) {
  const is2_1 = selected.some(s => s.amount === 2);
  const activeDist = is2_1 ? '2_1' : (selected.length === 3 ? '1_1_1' : distributions[0]);
  const slots = activeDist === '2_1' ? [2, 1] : [1, 1, 1];

  return (
      <div style={{display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem'}}>
        {distributions.length > 1 && (
            <div style={{display: 'flex', flexDirection: 'column', gap: '0.5rem'}}>
              <label style={{fontSize: 'var(--font-size-sm)', fontWeight: 500}}>Distribution
                Pattern:</label>
              <select
                  className={styles.freeformInput}
                  value={activeDist}
                  onChange={e => onChange([])}
              >
                {distributions.includes('2_1') &&
                    <option value="2_1">+2 to one, +1 to another</option>}
                {distributions.includes('1_1_1') &&
                    <option value="1_1_1">+1 to three different</option>}
              </select>
            </div>
        )}
        <div style={{display: 'flex', flexDirection: 'column', gap: '0.5rem'}}>
          {slots.map((amt, idx) => {
            const currentVal = selected[idx]?.ability || '';
            return (
                <div key={idx} style={{display: 'flex', alignItems: 'center', gap: '1rem'}}>
                  <span style={{
                    fontWeight: 'bold',
                    width: '40px',
                    color: 'var(--color-accent-primary)'
                  }}>+{amt}</span>
                  <select
                      className={styles.freeformInput}
                      style={{flex: 1}}
                      value={currentVal}
                      onChange={e => {
                        const val = e.target.value;
                        const next = slots.map((a, i) => ({
                          ability: i === idx ? val : (selected[i]?.ability || ''),
                          amount: a
                        }));
                        onChange(next.filter(n => n.ability !== ''));
                      }}
                  >
                    <option value="">— Select Ability —</option>
                    {options.map(opt => {
                      const isUsed = selected.some((s, i) => i !== idx && s.ability === opt.value);
                      return (
                          <option key={opt.value} value={opt.value} disabled={isUsed}>
                            {opt.label}
                          </option>
                      );
                    })}
                  </select>
                </div>
            );
          })}
        </div>
      </div>
  );
}

// ── Multi-select picker ───────────────────────────────────────

function OptionPicker({
                        options,
                        selected,
                        chooseCount,
                        onToggle,
                      }: {
  options: OptionItem[];
  selected: string[];
  chooseCount: number;
  onToggle: (value: string) => void;
}) {
  const enabledCount = options.filter(o => !o.disabled).length;
  const atLimit = selected.length >= chooseCount;

  return (
      <div className={styles.optionGrid}>
        {options.map(opt => {
          const isSelected = selected.includes(opt.value);
          const isDisabled = opt.disabled || (!isSelected && atLimit);

          return (
              <button
                  key={opt.value}
                  type="button"
                  className={`${styles.optionButton} ${isSelected ? styles.optionSelected : ''} ${isDisabled ? styles.optionDisabled : ''}`}
                  onClick={() => {
                    if (!isDisabled || isSelected) {
                      onToggle(opt.value);
                    }
                  }}
                  disabled={isDisabled && !isSelected}
                  aria-pressed={isSelected}
                  title={opt.disabledReason || undefined}
              >
            <span
                className={`${styles.optionCheck} ${isSelected ? styles.optionCheckSelected : ''}`}
                aria-hidden="true">
              {isSelected ? '✓' : ' '}
            </span>
                <span className={styles.optionLabel}>{opt.label}</span>
                {opt.disabled && opt.disabledReason && (
                    <span className={styles.disabledReason}>{opt.disabledReason}</span>
                )}
              </button>
          );
        })}

        {enabledCount === 0 && (
            <p className={styles.noEligible}>
              No eligible options available. You may need to gain proficiencies from other features
              first.
            </p>
        )}
      </div>
  );
}

// ── Freeform text input ───────────────────────────────────────

function FreeformPicker({
                          chooseCount,
                          selected,
                          onChange,
                          knownValues,
                          label,
                        }: {
  chooseCount: number;
  selected: string[];
  onChange: (values: string[]) => void;
  knownValues?: Set<string>;
  label?: string;
}) {
  const slots = Array.from({length: chooseCount}, (_, i) => i);

  const handleSlotChange = (index: number, value: string) => {
    const next = [...selected];
    next[index] = value;
    onChange(next);
  };

  return (
      <div className={styles.freeformInputs}>
        {slots.map(i => {
          const val = selected[i] ?? '';
          const isDuplicate = val.trim() !== '' && selected.filter(v => v.trim() === val.trim()).length > 1;
          const isAlreadyKnown = knownValues && val.trim() !== '' && knownValues.has(val.trim());

          return (
              <div key={i} className={styles.freeformRow}>
                <input
                    type="text"
                    className={`${styles.freeformInput} ${isDuplicate || isAlreadyKnown ? styles.freeformInputError : ''}`}
                    placeholder={`Enter ${label || 'option'} ${i + 1}`}
                    value={val}
                    onChange={e => handleSlotChange(i, e.target.value)}
                />
                {isDuplicate && (
                    <span className={styles.freeformError}>Duplicate</span>
                )}
                {isAlreadyKnown && !isDuplicate && (
                    <span className={styles.freeformWarning}>Already known</span>
                )}
              </div>
          );
        })}
      </div>
  );
}

// ── Main modal ────────────────────────────────────────────────

export function FeatureChoiceModal({
                                     isOpen,
                                     choice,
                                     character,
                                     onSubmit,
                                     onClear,
                                     onClose,
                                     saving = false,
                                   }: FeatureChoiceModalProps) {
  const options = useMemo(() => resolveOptions(choice, character), [choice, character]);
  const filter = (choice.optionsFilter as Record<string, unknown> | null) ?? {};

  const isFreeform = options.length === 0 && (
      choice.optionsSource === 'LANGUAGE_LIST' ||
      choice.optionsSource === 'TOOL_LIST'
  );

  const distributions = filter['distributions'] as string[] | undefined;
  const isAsiDistribution = choice.optionsSource === 'ABILITY_LIST' && Array.isArray(distributions);

  const knownValues = useMemo(() => {
    if (choice.optionsSource === 'LANGUAGE_LIST') {
      return new Set(character.proficiencies?.languages ?? []);
    }
    if (choice.optionsSource === 'TOOL_LIST') {
      return new Set(character.proficiencies?.tools ?? []);
    }
    return undefined;
  }, [choice.optionsSource, character.proficiencies]);

  const freeformLabel = choice.optionsSource === 'LANGUAGE_LIST' ? 'language' : 'tool';

  // State for normal strings (Languages, Tools, standard Abilities)
  const [selected, setSelected] = useState<string[]>(() => {
    if (Array.isArray(choice.currentSelection) && typeof choice.currentSelection[0] === 'string') {
      return choice.currentSelection as string[];
    }
    return [];
  });

  // State for complex objects (2024 ASI rules)
  const [asiSelected, setAsiSelected] = useState<Record<string, any>[]>(() => {
    if (Array.isArray(choice.currentSelection) && typeof choice.currentSelection[0] === 'object') {
      return choice.currentSelection as Record<string, any>[];
    }
    return [];
  });

  const handleToggle = (value: string) => {
    setSelected(prev => {
      if (prev.includes(value)) return prev.filter(v => v !== value);
      if (prev.length >= choice.chooseCount) return prev;
      return [...prev, value];
    });
  };

  const nonBlankSelected = selected.filter(v => v.trim() !== '');
  const hasDuplicates = new Set(nonBlankSelected).size !== nonBlankSelected.length;
  const hasAlreadyKnown = isFreeform && knownValues
      ? nonBlankSelected.some(v => knownValues.has(v.trim()))
      : false;

  const canSubmit = useMemo(() => {
    if (saving) return false;

    // Custom validation logic if resolving an ASI matrix
    if (isAsiDistribution) {
      const is2_1 = asiSelected.some(s => s.amount === 2);
      const expectedCount = is2_1 ? 2 : 3;
      if (asiSelected.length !== expectedCount) return false;
      const uniqueAbilities = new Set(asiSelected.map(s => s.ability));
      return uniqueAbilities.size === expectedCount && !asiSelected.some(s => s.ability === '');
    }

    // Standard string validation logic
    return nonBlankSelected.length === choice.chooseCount && !hasDuplicates && !hasAlreadyKnown;
  }, [saving, isAsiDistribution, asiSelected, nonBlankSelected.length, choice.chooseCount, hasDuplicates, hasAlreadyKnown]);

  const handleSubmit = async () => {
    if (!canSubmit) return;
    if (isAsiDistribution) {
      await onSubmit(asiSelected); // Submit the array of objects!
    } else {
      await onSubmit(nonBlankSelected.map(v => v.trim())); // Submit the array of strings
    }
  };

  const footer = (
      <div className={styles.footer}>
        <div className={styles.footerLeft}>
          {choice.currentSelection && (
              <Button variant="ghost" size="small" onClick={onClear} disabled={saving}>
                Clear Answer
              </Button>
          )}
        </div>
        <div className={styles.footerRight}>
          <Button variant="secondary" onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={!canSubmit} loading={saving}>
            {saving ? 'Saving...' : 'Confirm'}
          </Button>
        </div>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title={choice.name}
          size="medium"
          footer={footer}
      >
        <div className={styles.body}>
          {choice.description && (
              <p className={styles.description}>{choice.description}</p>
          )}

          {!isAsiDistribution && (
              <p className={styles.instruction}>
                Choose <strong>{choice.chooseCount}</strong> option
                {choice.chooseCount > 1 ? 's' : ''}.{' '}
                <span className={styles.selectionCount}>
              ({nonBlankSelected.length}/{choice.chooseCount} selected)
            </span>
              </p>
          )}

          {isAsiDistribution ? (
              <AsiDistributionPicker
                  options={options}
                  distributions={distributions!}
                  selected={asiSelected}
                  onChange={setAsiSelected}
              />
          ) : isFreeform ? (
              <FreeformPicker
                  chooseCount={choice.chooseCount}
                  selected={selected}
                  onChange={setSelected}
                  knownValues={knownValues}
                  label={freeformLabel}
              />
          ) : options.length > 0 ? (
              <OptionPicker
                  options={options}
                  selected={selected}
                  chooseCount={choice.chooseCount}
                  onToggle={handleToggle}
              />
          ) : (
              <div className={styles.noOptions}>
                <p>
                  No options available for this choice type ({choice.optionsSource}).
                </p>
              </div>
          )}
        </div>
      </Modal>
  );
}
