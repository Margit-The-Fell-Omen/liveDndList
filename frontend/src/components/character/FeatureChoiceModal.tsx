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
        {key: 'STRENGTH', name: 'Strength'},
        {key: 'DEXTERITY', name: 'Dexterity'},
        {key: 'CONSTITUTION', name: 'Constitution'},
        {key: 'INTELLIGENCE', name: 'Intelligence'},
        {key: 'WISDOM', name: 'Wisdom'},
        {key: 'CHARISMA', name: 'Charisma'},
      ];

      const fromList = extractValidOptions(choice, ABILITIES);

      const filtered = ABILITIES.filter(a => {
        if (fromList && !fromList.includes(a.key)) return false;
        return true;
      });

      return filtered.map(a => ({value: a.key, label: a.name}));
    }

    case 'LANGUAGE_LIST': {
      return []; // freeform mode
    }

    case 'TOOL_LIST':
      return []; // freeform mode

    case 'FEAT_LIST':
      return []; // TODO: populate from context.feats

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

// ── Freeform text input (for LANGUAGE_LIST, TOOL_LIST) ────────

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

  // Determine known values for freeform validation
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

  const [selected, setSelected] = useState<string[]>(() => {
    if (Array.isArray(choice.currentSelection)) {
      return choice.currentSelection as string[];
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

  const canSubmit =
      !saving &&
      nonBlankSelected.length === choice.chooseCount &&
      !hasDuplicates &&
      !hasAlreadyKnown;

  const handleSubmit = async () => {
    if (!canSubmit) return;
    await onSubmit(nonBlankSelected.map(v => v.trim()));
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

          <p className={styles.instruction}>
            Choose <strong>{choice.chooseCount}</strong> option
            {choice.chooseCount > 1 ? 's' : ''}.{' '}
            <span className={styles.selectionCount}>
            ({nonBlankSelected.length}/{choice.chooseCount} selected)
          </span>
          </p>

          {isFreeform ? (
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
