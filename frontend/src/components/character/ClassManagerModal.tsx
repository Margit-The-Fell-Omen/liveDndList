import {useState} from 'react';
import {ConfirmModal, Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {CharacterClass, DndClassLevel} from '@/types';
import {getClassDisplayName} from '@/utils/classes';
import {ClassPickerModal} from './ClassPickerModal';
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
            size="medium"
            footer={footer}
        >
          <div className={styles.body}>
            <p className={styles.hint}>
              Change or remove classes. Levels from a removed class must be transferred to another
              class.
            </p>

            <div className={styles.list}>
              {currentClasses.map(entry => {
                const canRemove = currentClasses.length > 1;
                return (
                    <div key={entry.classKey} className={styles.item}>
                      <div className={styles.itemInfo}>
                    <span className={styles.itemName}>
                      {getClassDisplayName(classes, entry.classKey)}
                    </span>
                        <span className={styles.itemLevel}>Level {entry.level}</span>
                      </div>
                      <div className={styles.itemActions}>
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
                      </div>
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