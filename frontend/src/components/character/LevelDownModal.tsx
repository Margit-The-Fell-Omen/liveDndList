import {useEffect, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {CharacterClass, DndClassLevel} from '@/types';
import {getClassDisplayName} from '@/utils/classes';
import styles from './LevelUpModal.module.css';

interface LevelDownModalProps {
  isOpen: boolean;
  onClose: () => void;
  classes: CharacterClass[];
  currentClasses: DndClassLevel[];
  excessLevels: number;
  onLevelDown: (classKey: string) => void | Promise<void>;
  saving?: boolean;
}

export function LevelDownModal({
                                 isOpen,
                                 onClose,
                                 classes,
                                 currentClasses,
                                 excessLevels,
                                 onLevelDown,
                                 saving = false,
                               }: LevelDownModalProps) {
  const [selectedKey, setSelectedKey] = useState('');

  useEffect(() => {
    if (isOpen) setSelectedKey('');
  }, [isOpen]);

  const canRemoveClass = currentClasses.length > 1;

  const downgradeable = currentClasses.filter(entry => {
    if (entry.level > 1) return true;
    return canRemoveClass;
  });

  const canConfirm = !!selectedKey && !saving;

  const selectedEntry = currentClasses.find(e => e.classKey === selectedKey);
  const willRemoveClass = selectedEntry?.level === 1;

  const footer = (
      <div className={styles.footerActions}>
        <Button variant="secondary" onClick={onClose}>Cancel</Button>
        <Button
            variant="danger"
            onClick={() => onLevelDown(selectedKey)}
            disabled={!canConfirm}
        >
          {saving ? 'Saving...' : willRemoveClass ? 'Remove Class' : 'Remove Level'}
        </Button>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title="Remove Level"
          size="medium"
          footer={footer}
      >
        <div className={styles.body}>
          <p className={styles.hint}>
            You have <strong>{excessLevels}</strong> excess
            {' '}level{excessLevels === 1 ? '' : 's'} to remove.
            Choose a class to remove a level from.
          </p>

          {downgradeable.length === 0 && (
              <p className={styles.notice}>
                No classes can be downgraded. A character must have at least one class.
              </p>
          )}

          {downgradeable.length > 0 && (
              <div className={styles.list}>
                {downgradeable.map(entry => {
                  const isSelected = selectedKey === entry.classKey;
                  const isLastLevel = entry.level === 1;
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
                          {isLastLevel
                              ? `Level 1 → Remove class`
                              : `Level ${entry.level} → ${entry.level - 1}`
                          }
                        </span>
                      </button>
                  );
                })}
              </div>
          )}
        </div>
      </Modal>
  );
}