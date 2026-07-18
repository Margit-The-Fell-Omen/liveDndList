import {useState} from 'react';
import {ConfirmModal, Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {CharacterClass, DndClassLevel} from '@/types';
import {getClassByKey, getClassDisplayName} from '@/utils/classes';
import {ClassPickerModal} from './ClassPickerModal';
import {ClassDetailPanel} from './wizard/ClassDetailPanel';
import styles from './ClassManagerModal.module.css';

interface ClassManagerModalProps {
  isOpen: boolean;
  onClose: () => void;
  classes: CharacterClass[];
  currentClasses: DndClassLevel[];
  onSave: (nextClasses: DndClassLevel[]) => void | Promise<void>;
  saving?: boolean;
}

type PendingAction =
    | { type: 'change'; classKey: string }
    | { type: 'transferFor'; classKey: string; level: number }
    | null;

export function ClassManagerModal({
                                    isOpen,
                                    onClose,
                                    classes,
                                    currentClasses,
                                    onSave,
                                    saving = false,
                                  }: ClassManagerModalProps) {
  const [pending, setPending] = useState<PendingAction>(null);
  const [confirmRemoval, setConfirmRemoval] = useState<{ classKey: string } | null>(null);
  const [expandedKey, setExpandedKey] = useState<string | null>(null);

  const takenKeys = currentClasses.map(entry => entry.classKey);

  const handleChangeClass = async (newClassKey: string) => {
    if (!pending || pending.type !== 'change') return;
    const oldClassKey = pending.classKey;

    if (newClassKey === oldClassKey) {
      setPending(null);
      return;
    }

    const next = currentClasses.map(entry =>
        entry.classKey === oldClassKey
            ? {classKey: newClassKey, level: entry.level}
            : entry
    );

    await onSave(next);
    setPending(null);
    setExpandedKey(null);
  };

  const handleTransferLevels = async (destinationKey: string) => {
    if (!pending || pending.type !== 'transferFor') return;
    const {classKey: removedKey, level: removedLevels} = pending;

    const next = currentClasses
        .filter(entry => entry.classKey !== removedKey)
        .map(entry =>
            entry.classKey === destinationKey
                ? {...entry, level: entry.level + removedLevels}
                : entry
        );

    await onSave(next);
    setPending(null);
    setExpandedKey(null);
  };

  const beginRemove = (classKey: string) => {
    if (currentClasses.length <= 1) return;
    setConfirmRemoval({classKey});
  };

  const confirmRemove = () => {
    if (!confirmRemoval) return;
    const target = currentClasses.find(entry => entry.classKey === confirmRemoval.classKey);
    setConfirmRemoval(null);
    if (!target) return;

    if (target.level === 0) {
      const next = currentClasses.filter(entry => entry.classKey !== target.classKey);
      onSave(next);
      return;
    }

    setPending({type: 'transferFor', classKey: target.classKey, level: target.level});
  };

  const toggleExpanded = (classKey: string) => {
    setExpandedKey(prev => (prev === classKey ? null : classKey));
  };

  const footer = (
      <div className={styles.footerActions}>
        <Button variant="secondary" onClick={onClose}>Close</Button>
      </div>
  );

  const transferCandidates =
      pending?.type === 'transferFor'
          ? currentClasses.filter(entry => entry.classKey !== pending.classKey)
          : [];

  return (
      <>
        <Modal
            isOpen={isOpen && !pending}
            onClose={onClose}
            title="Manage Classes"
            size="large"
            footer={footer}
        >
          <div className={styles.body}>
            <p className={styles.hint}>
              Click a class to view its details. Use the actions on the right to change or remove a
              class.
              Levels from a removed class must be transferred to another class.
            </p>

            <div className={styles.list}>
              {currentClasses.map(entry => {
                const canRemove = currentClasses.length > 1;
                const isExpanded = expandedKey === entry.classKey;
                const clsRef = getClassByKey(classes, entry.classKey);

                return (
                    <div key={entry.classKey} className={styles.itemGroup}>
                      <button
                          type="button"
                          className={styles.item}
                          data-expanded={isExpanded}
                          onClick={() => toggleExpanded(entry.classKey)}
                          aria-expanded={isExpanded}
                      >
                        <div className={styles.itemInfo}>
                      <span className={styles.itemName}>
                        {getClassDisplayName(classes, entry.classKey)}
                      </span>
                          <span className={styles.itemLevel}>Level {entry.level}</span>
                        </div>
                        <div
                            className={styles.itemActions}
                            onClick={e => e.stopPropagation()}
                        >
                          <Button
                              variant="ghost"
                              size="small"
                              onClick={() => setPending({type: 'change', classKey: entry.classKey})}
                              disabled={saving}
                          >
                            Change
                          </Button>
                          <Button
                              variant="danger"
                              size="small"
                              onClick={() => beginRemove(entry.classKey)}
                              disabled={saving || !canRemove}
                              title={canRemove ? 'Remove class' : 'A character must have at least one class'}
                          >
                            Remove
                          </Button>
                          <span
                              className={styles.chevron}
                              data-expanded={isExpanded}
                              aria-hidden="true"
                          >
                        ›
                      </span>
                        </div>
                      </button>

                      {isExpanded && clsRef && (
                          <div className={styles.detailWrapper}>
                            <ClassDetailPanel
                                cls={clsRef}
                                allClasses={classes}
                                currentLevel={entry.level}
                            />
                          </div>
                      )}
                    </div>
                );
              })}
            </div>
          </div>
        </Modal>

        {pending?.type === 'change' && (
            <ClassPickerModal
                isOpen
                onClose={() => setPending(null)}
                classes={classes}
                title="Change Class"
                disabledKeys={takenKeys}
                initialClassKey={pending.classKey}
                onConfirm={handleChangeClass}
                saving={saving}
            />
        )}

        {pending?.type === 'transferFor' && (
            <Modal
                isOpen
                onClose={() => setPending(null)}
                title="Transfer Levels"
                size="small"
                footer={
                  <div className={styles.footerActions}>
                    <Button variant="secondary" onClick={() => setPending(null)}>Cancel</Button>
                  </div>
                }
            >
              <div className={styles.body}>
                <p className={styles.hint}>
                  Removing <strong>{getClassDisplayName(classes, pending.classKey)}</strong>.
                  Where should its {pending.level} level{pending.level === 1 ? '' : 's'} go?
                </p>
                <div className={styles.list}>
                  {transferCandidates.map(entry => (
                      <button
                          key={entry.classKey}
                          type="button"
                          className={styles.transferOption}
                          onClick={() => handleTransferLevels(entry.classKey)}
                          disabled={saving}
                      >
                  <span className={styles.itemName}>
                    {getClassDisplayName(classes, entry.classKey)}
                  </span>
                        <span className={styles.itemLevel}>
                    Level {entry.level} → {entry.level + pending.level}
                  </span>
                      </button>
                  ))}
                </div>
              </div>
            </Modal>
        )}

        <ConfirmModal
            isOpen={!!confirmRemoval}
            onClose={() => setConfirmRemoval(null)}
            onConfirm={confirmRemove}
            title="Remove Class"
            message={
              confirmRemoval
                  ? `Remove ${getClassDisplayName(classes, confirmRemoval.classKey)} from this character?`
                  : ''
            }
            confirmText="Remove"
            variant="danger"
        />
      </>
  );
}
