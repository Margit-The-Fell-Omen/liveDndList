import {useEffect, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {CharacterClass, DndClassLevel} from '@/types';
import {getClassDisplayName, MAX_CLASS_LEVEL} from '@/utils/classes';
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

  const footer = (
      <div className={styles.footerActions}>
        <Button variant="secondary" onClick={onClose}>Cancel</Button>
        <Button
            onClick={() => onExistingClassLevelUp(selectedKey)}
            disabled={!canConfirm}
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
                You can still multiclass into a new class below.
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

          <div className={styles.divider}/>

          <Button variant="secondary" fullWidth onClick={onAddNewClass} disabled={saving}>
            + Multiclass into a new class
          </Button>
        </div>
      </Modal>
  );
}
