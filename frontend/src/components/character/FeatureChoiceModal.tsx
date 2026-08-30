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

interface OptionItem {
  value: string;
  label: string;
  disabled?: boolean;
  disabledReason?: string;
}

function formatOptionLabel(value: string): string {
  return value
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
}

function collectAlreadyChosenValues(currentChoice: PendingChoiceResponse, character: Character): Set<string> {
  const already = new Set<string>();
  const feature = character.features?.find(f => f.id === currentChoice.characterFeatureId);
  if (!feature) return already;

  for (const answer of feature.choices) {
    if (answer.choiceKey === currentChoice.choiceKey) continue;
    if (Array.isArray(answer.selectedValues)) {
      for (const v of answer.selectedValues) {
        if (typeof v === 'string') already.add(v);
      }
    }
  }
  return already;
}

function resolveOptions(
    choice: PendingChoiceResponse,
    character: Character,
    filterObj: any,
    forcedFromList?: string[]
): OptionItem[] {
  switch (choice.optionsSource) {
    case 'INLINE': {
      const opts = (filterObj['options'] as string[] | undefined) ?? [];
      return opts.map(o => ({value: o, label: formatOptionLabel(o)}));
    }
    case 'SKILL_LIST': {
      const fromList = filterObj['fromList'] as string[] | undefined;
      const onlyProficient = filterObj['onlyProficient'] as boolean | undefined;
      const excludeChosen = filterObj['excludeChosen'] as boolean | undefined;

      const grantedProficiencies = new Set(character.skills.filter(s => s.proficient).map(s => s.skillType));
      const grantedExpertise = new Set(character.skills.filter(s => s.expertise).map(s => s.skillType));
      const alreadyChosenOnFeature = collectAlreadyChosenValues(choice, character);

      const filtered = SKILLS.filter(s => fromList ? fromList.includes(s.key) : true);

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

        return {value: s.key, label: s.name, disabled, disabledReason};
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

      const fromList = forcedFromList || (filterObj['fromList'] as string[] | undefined);
      return ABILITIES
          .filter(a => fromList ? fromList.includes(a.key) : true)
          .map(a => ({value: a.key, label: a.name}));
    }
    case 'LANGUAGE_LIST':
    case 'TOOL_LIST':
      return [];
    default:
      return [];
  }
}

// ── 2024 ASI Distribution Picker ──────────────────────────────

function AsiDistributionPicker({
                                 options, distributions, selected, onChange,
                               }: {
  options: OptionItem[];
  distributions: string[];
  selected: { ability: string; amount: number }[];
  onChange: (val: { ability: string; amount: number }[]) => void;
}) {
  const [distType, setDistType] = useState<'2_1' | '1_1_1'>(distributions.includes('2_1') ? '2_1' : '1_1_1');

  const handleDistChange = (newDist: '2_1' | '1_1_1') => {
    setDistType(newDist);
    onChange([]);
  };

  const slots = distType === '2_1' ? [2, 1] : [1, 1, 1];

  return (
      <div style={{display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem'}}>
        {distributions.length > 1 && (
            <div style={{display: 'flex', flexDirection: 'column', gap: '0.5rem'}}>
              <label style={{fontSize: 'var(--font-size-sm)', fontWeight: 500}}>Distribution
                Pattern:</label>
              <select
                  style={{
                    padding: '0.5rem',
                    borderRadius: '4px',
                    border: '1px solid var(--color-border)'
                  }}
                  value={distType}
                  onChange={e => handleDistChange(e.target.value as '2_1' | '1_1_1')}
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
                      style={{
                        flex: 1,
                        padding: '0.5rem',
                        borderRadius: '4px',
                        border: '1px solid var(--color-border)'
                      }}
                      value={currentVal}
                      onChange={e => {
                        const val = e.target.value;
                        const next = [...selected];
                        while (next.length < slots.length) {
                          next.push({ability: '', amount: slots[next.length]});
                        }
                        next[idx] = {ability: val, amount: amt};
                        onChange(next);
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

// ── Multi-select Picker ───────────────────────────────────────
function OptionPicker({options, selected, chooseCount, onToggle}: {
  options: OptionItem[];
  selected: string[];
  chooseCount: number;
  onToggle: (val: string) => void;
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
                    if (!isDisabled || isSelected) onToggle(opt.value);
                  }}
                  disabled={isDisabled && !isSelected}
                  title={opt.disabledReason}
              >
                <span
                    className={`${styles.optionCheck} ${isSelected ? styles.optionCheckSelected : ''}`}
                    aria-hidden="true">{isSelected ? '✓' : ' '}</span>
                <span className={styles.optionLabel}>{opt.label}</span>
                {opt.disabled && opt.disabledReason &&
                    <span className={styles.disabledReason}>{opt.disabledReason}</span>}
              </button>
          );
        })}
        {enabledCount === 0 && <p className={styles.noEligible}>No eligible options available.</p>}
      </div>
  );
}

// ── Freeform Picker ───────────────────────────────────────────
function FreeformPicker({chooseCount, selected, onChange, knownValues, label}: {
  chooseCount: number;
  selected: string[];
  onChange: (v: string[]) => void;
  knownValues?: Set<string>;
  label?: string;
}) {
  const slots = Array.from({length: chooseCount}, (_, i) => i);
  return (
      <div className={styles.freeformInputs}>
        {slots.map(i => {
          const val = selected[i] ?? '';
          const isDuplicate = val.trim() !== '' && selected.filter(v => v.trim() === val.trim()).length > 1;
          const isKnown = knownValues && val.trim() !== '' && knownValues.has(val.trim());
          return (
              <div key={i} className={styles.freeformRow}>
                <input
                    type="text"
                    className={`${styles.freeformInput} ${isDuplicate || isKnown ? styles.freeformInputError : ''}`}
                    placeholder={`Enter ${label || 'option'} ${i + 1}`}
                    value={val}
                    onChange={e => {
                      const next = [...selected];
                      next[i] = e.target.value;
                      onChange(next);
                    }}
                />
                {isDuplicate && <span className={styles.freeformError}>Duplicate</span>}
                {isKnown && !isDuplicate &&
                    <span className={styles.freeformWarning}>Already known</span>}
              </div>
          );
        })}
      </div>
  );
}

// ── Main Modal ────────────────────────────────────────────────

export function FeatureChoiceModal({
                                     isOpen,
                                     choice,
                                     character,
                                     onSubmit,
                                     onClear,
                                     onClose,
                                     saving = false
                                   }: FeatureChoiceModalProps) {
  console.log('PENDING CHOICE DATA:', choice);
  // 1. Attempt to parse the broken filter from the backend
  const filterObj = useMemo(() => {
    try {
      return typeof choice.optionsFilter === 'string' ? JSON.parse(choice.optionsFilter) : (choice.optionsFilter || {});
    } catch {
      return {};
    }
  }, [choice.optionsFilter]);

  // 2. BULLETPROOF FALLBACK: Read the description text if the backend destroyed the JSON
  const hasAsiText = choice.description?.includes('+2/+1') || choice.name?.includes('Ability Score Allocation');
  const isAsi2024 = choice.optionsSource === 'ABILITY_LIST' && (Array.isArray(filterObj['distributions']) || hasAsiText);

  const activeDistributions = (Array.isArray(filterObj['distributions']) && filterObj['distributions'].length > 0)
      ? filterObj['distributions']
      : ['2_1', '1_1_1'];

  // Infer fromList from text if missing
  let activeFromList = filterObj['fromList'] as string[] | undefined;
  if (isAsi2024 && (!activeFromList || !Array.isArray(activeFromList))) {
    const inferred: string[] = [];
    const desc = choice.description || '';
    if (desc.includes('Strength')) inferred.push('STR');
    if (desc.includes('Dexterity')) inferred.push('DEX');
    if (desc.includes('Constitution')) inferred.push('CON');
    if (desc.includes('Intelligence')) inferred.push('INT');
    if (desc.includes('Wisdom')) inferred.push('WIS');
    if (desc.includes('Charisma')) inferred.push('CHA');
    if (inferred.length > 0) activeFromList = inferred;
  }

  const options = useMemo(() => resolveOptions(choice, character, filterObj, activeFromList), [choice, character, filterObj, activeFromList]);

  const isFreeform = options.length === 0 && (choice.optionsSource === 'LANGUAGE_LIST' || choice.optionsSource === 'TOOL_LIST');
  const knownValues = useMemo(() => {
    if (choice.optionsSource === 'LANGUAGE_LIST') return new Set(character.proficiencies?.languages ?? []);
    if (choice.optionsSource === 'TOOL_LIST') return new Set(character.proficiencies?.tools ?? []);
    return undefined;
  }, [choice.optionsSource, character.proficiencies]);

  // States
  const [selected, setSelected] = useState<string[]>([]);
  const [asiSelected, setAsiSelected] = useState<{ ability: string; amount: number }[]>([]);

  // Validation
  const canSubmit = useMemo(() => {
    if (saving) return false;
    if (isAsi2024) {
      const is2_1 = asiSelected.some(s => s.amount === 2);
      const expectedCount = is2_1 ? 2 : 3;
      if (asiSelected.length !== expectedCount) return false;
      const uniqueAbilities = new Set(asiSelected.map(s => s.ability));
      return uniqueAbilities.size === expectedCount && !asiSelected.some(s => s.ability === '');
    }
    const nonBlank = selected.filter(v => v.trim() !== '');
    const hasDupes = new Set(nonBlank).size !== nonBlank.length;
    const hasKnown = isFreeform && knownValues ? nonBlank.some(v => knownValues.has(v.trim())) : false;
    return nonBlank.length === choice.chooseCount && !hasDupes && !hasKnown;
  }, [saving, isAsi2024, asiSelected, selected, choice.chooseCount, isFreeform, knownValues]);

  const handleSubmit = async () => {
    if (!canSubmit) return;
    if (isAsi2024) {
      await onSubmit(asiSelected);
    } else {
      await onSubmit(selected.filter(v => v.trim() !== '').map(v => v.trim()));
    }
  };

  const footer = (
      <div className={styles.footer}>
        <div className={styles.footerLeft}>
          {choice.currentSelection && (
              <Button variant="ghost" size="small" onClick={onClear} disabled={saving}>Clear
                Answer</Button>
          )}
        </div>
        <div className={styles.footerRight}>
          <Button variant="secondary" onClick={onClose} disabled={saving}>Cancel</Button>
          <Button onClick={handleSubmit} disabled={!canSubmit} loading={saving}>
            {saving ? 'Saving...' : 'Confirm'}
          </Button>
        </div>
      </div>
  );

  return (
      <Modal isOpen={isOpen} onClose={onClose} title={choice.name} size="medium" footer={footer}>
        <div className={styles.body}>
          {choice.description && <p className={styles.description}>{choice.description}</p>}

          {isAsi2024 ? (
              <AsiDistributionPicker
                  options={options}
                  distributions={activeDistributions}
                  selected={asiSelected}
                  onChange={setAsiSelected}
              />
          ) : isFreeform ? (
              <FreeformPicker
                  chooseCount={choice.chooseCount}
                  selected={selected}
                  onChange={setSelected}
                  knownValues={knownValues}
                  label={choice.optionsSource === 'LANGUAGE_LIST' ? 'language' : 'tool'}
              />
          ) : options.length > 0 ? (
              <>
                <p className={styles.instruction}>Choose <strong>{choice.chooseCount}</strong> option(s).
                </p>
                <OptionPicker
                    options={options}
                    selected={selected}
                    chooseCount={choice.chooseCount}
                    onToggle={val => setSelected(prev => prev.includes(val) ? prev.filter(v => v !== val) : (prev.length < choice.chooseCount ? [...prev, val] : prev))}
                />
              </>
          ) : (
              <div className={styles.noOptions}><p>No options available.</p></div>
          )}
        </div>
      </Modal>
  );
}
