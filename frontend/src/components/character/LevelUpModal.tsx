import {useEffect, useMemo, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {CharacterClass, ClassFeature, DndClassLevel} from '@/types';
import {getClassByKey, getClassDisplayName, MAX_CLASS_LEVEL} from '@/utils/classes';
import styles from './LevelUpModal.module.css';

interface LevelUpModalProps {
  isOpen: boolean;
  onClose: () => void;
  classes: CharacterClass[];
  currentClasses: DndClassLevel[];
  pendingLevels: number;
  onExistingClassLevelUp: (classKey: string) => void | Promise<void>;
  onAddNewClass: () => void;
  saving?: boolean;
}

function findFeaturesAtLevel(
    classes: CharacterClass[],
    classKey: string,
    level: number
): ClassFeature[] {
  const results: ClassFeature[] = [];

  // Main class features
  const cls = getClassByKey(classes, classKey);
  if (cls?.features) {
    for (const f of cls.features) {
      if (!f.gainedAt || !Array.isArray(f.gainedAt)) continue;
      if (f.gainedAt.some(ga => ga.level === level)) {
        results.push(f);
      }
    }
  }

  for (const c of classes) {
    if (c.subclassOf?.key === classKey && c.features) {
      for (const f of c.features) {
        if (!f.gainedAt || !Array.isArray(f.gainedAt)) continue;
        if (f.gainedAt.some(ga => ga.level === level)) {
          results.push(f);
        }
      }
    }
  }

  return results;
}

function getLevelSummary(level: number): string[] {
  const notes: string[] = [];

  const profBefore = Math.ceil((level - 1) / 4) + 1;
  const profAfter = Math.ceil(level / 4) + 1;
  if (level > 1 && profAfter > profBefore) {
    notes.push(`Proficiency bonus increases to +${profAfter}`);
  }

  if ([4, 8, 12, 16, 19].includes(level)) {
    notes.push('Ability Score Improvement (or Feat)');
  }

  return notes;
}

export function LevelUpModal({
                               isOpen,
                               onClose,
                               classes,
                               currentClasses,
                               pendingLevels,
                               onExistingClassLevelUp,
                               onAddNewClass,
                               saving = false,
                             }: LevelUpModalProps) {
  const [selectedKey, setSelectedKey] = useState('');

  useEffect(() => {
    if (isOpen) setSelectedKey('');
  }, [isOpen]);

  const levelableClasses = currentClasses.filter(entry => entry.level < MAX_CLASS_LEVEL);
  const canConfirm = !!selectedKey && !saving;

  const selectedEntry = currentClasses.find(e => e.classKey === selectedKey);
  const nextLevel = selectedEntry ? selectedEntry.level + 1 : 0;

  const previewFeatures = useMemo(() => {
    if (!selectedKey || !nextLevel) return [];
    return findFeaturesAtLevel(classes, selectedKey, nextLevel);
  }, [selectedKey, nextLevel, classes]);

  const levelNotes = useMemo(() => {
    if (!nextLevel) return [];
    return getLevelSummary(nextLevel);
  }, [nextLevel]);

  const footer = (
      <div className={styles.footerActions}>
        <Button variant="secondary" onClick={onClose} disabled={saving}>
          Cancel
        </Button>
        <Button
            onClick={() => onExistingClassLevelUp(selectedKey)}
            disabled={!canConfirm}
            loading={saving}
        >
          {saving ? 'Saving...' : 'Apply Level'}
        </Button>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title="Apply Level"
          size="medium"
          footer={footer}
      >
        <div className={styles.body}>
          <p className={styles.hint}>
            You have <strong>{pendingLevels}</strong> unassigned
            level{pendingLevels === 1 ? '' : 's'}.
            Choose a class to gain a level in, or multiclass into a new one.
          </p>

          {levelableClasses.length === 0 && (
              <p className={styles.notice}>
                All your classes are at the maximum level ({MAX_CLASS_LEVEL}).
              </p>
          )}

          {levelableClasses.length > 0 && (
              <div className={styles.list}>
                {levelableClasses.map(entry => {
                  const isSelected = selectedKey === entry.classKey;
                  return (
                      <button
                          key={entry.classKey}
                          type="button"
                          className={styles.item}
                          data-selected={isSelected}
                          onClick={() => setSelectedKey(entry.classKey)}
                      >
                  <span className={styles.itemName}>
                    {getClassDisplayName(classes, entry.classKey)}
                  </span>
                        <span className={styles.itemLevel}>
                    Level {entry.level} → {entry.level + 1}
                  </span>
                      </button>
                  );
                })}
              </div>
          )}

          {selectedKey && (previewFeatures.length > 0 || levelNotes.length > 0) && (
              <div className={styles.featurePreview}>
                <h4 className={styles.featurePreviewTitle}>
                  At level {nextLevel} you gain:
                </h4>

                {levelNotes.length > 0 && (
                    <div className={styles.levelNotes}>
                      {levelNotes.map((note, i) => (
                          <div key={i} className={styles.levelNote}>
                            <span className={styles.levelNoteIcon}>↑</span>
                            <span>{note}</span>
                          </div>
                      ))}
                    </div>
                )}

                {previewFeatures.length > 0 && (
                    <div className={styles.featurePreviewList}>
                      {previewFeatures.map(f => (
                          <div key={f.key} className={styles.featurePreviewItem}>
                            <span className={styles.featurePreviewName}>{f.name}</span>
                            {f.desc && (
                                <p className={styles.featurePreviewDesc}>
                                  {f.desc.length > 200 ? `${f.desc.slice(0, 200)}…` : f.desc}
                                </p>
                            )}
                          </div>
                      ))}
                    </div>
                )}
              </div>
          )}

          {selectedKey && previewFeatures.length === 0 && levelNotes.length === 0 && (
              <div className={styles.featurePreview}>
                <p className={styles.noFeatures}>
                  No specific new features at this level. Check Features &amp; Traits after
                  leveling up for any pipeline-computed changes.
                </p>
              </div>
          )}

          <div className={styles.divider}/>

          <Button variant="secondary" fullWidth onClick={onAddNewClass} disabled={saving}>
            + Multiclass into a new class
          </Button>
        </div>
      </Modal>
  );
}
