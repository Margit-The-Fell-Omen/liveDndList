import type {CharacterClass} from '@/types';
import {SelectionCard} from './SelectionCard';
import {stripMarkdown} from '@/utils/markdown';
import {ClassDetailPanel} from './ClassDetailPanel';
import styles from './StepGrid.module.css';

interface StepClassProps {
  classes: CharacterClass[];
  selectedKey: string;
  onSelect: (key: string) => void;
}

const humanize = (raw: string): string =>
    raw.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());

function documentLabel(doc?: CharacterClass['document']): string | undefined {
  if (!doc) return undefined;
  return doc.display_name || doc.name || doc.key;
}

function firstProficiencyDesc(cls: CharacterClass): string {
  const raw = cls.features?.find(f => {
    const r = f as unknown as Record<string, unknown>;
    const type = (r.featureType ?? r.feature_type) as string | undefined;
    return type === 'PROFICIENCIES';
  });
  const desc = (raw as unknown as Record<string, unknown> | undefined)?.desc;
  return typeof desc === 'string' ? desc : '';
}

export function StepClass({classes, selectedKey, onSelect}: StepClassProps) {
  const baseClasses = classes.filter(c => c.subclassOf === null);

  const buildBadges = (cls: CharacterClass): string[] => {
    const hitDie = cls.hitDice ?? cls.hit_dice;
    const saves = (cls.savingThrows ?? []).map(humanize).join(', ');
    return [
      hitDie ? `Hit Die: ${hitDie}` : undefined,
      saves ? `Saves: ${saves}` : undefined,
    ].filter(Boolean) as string[];
  };

  const handleClick = (key: string) => {
    onSelect(selectedKey === key ? '' : key);
  };

  return (
      <div className={styles.container}>
        <p className={styles.hint}>
          Choose your class. Click again to deselect. You will be level 1 — subclasses unlock at
          higher levels.
        </p>
        <div className={styles.grid}>
          {baseClasses.map(cls => {
            const previewSource = firstProficiencyDesc(cls) || cls.desc || '';
            const previewPlain = stripMarkdown(previewSource);
            const isSelected = selectedKey === cls.key;
            return (
                <>
                  <SelectionCard
                      key={cls.key}
                      title={cls.name}
                      topRight={documentLabel(cls.document)}
                      badges={buildBadges(cls)}
                      description={previewPlain}
                      isSelected={isSelected}
                      onClick={() => handleClick(cls.key)}
                  />
                  {isSelected && (
                      <ClassDetailPanel
                          key={`${cls.key}-detail`}
                          cls={cls}
                          allClasses={classes}
                      />
                  )}
                </>
            );
          })}
        </div>
      </div>
  );
}
