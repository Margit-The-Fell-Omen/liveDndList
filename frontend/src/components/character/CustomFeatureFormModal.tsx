import {useEffect, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {CustomFeatureResponse} from '@/types';
import styles from './CustomFeatureFormModal.module.css';

interface CustomFeatureFormModalProps {
  isOpen: boolean;
  initial: CustomFeatureResponse | null;
  onSave: (name: string, description?: string) => Promise<void>;
  onClose: () => void;
  saving?: boolean;
}

export function CustomFeatureFormModal({
                                         isOpen,
                                         initial,
                                         onSave,
                                         onClose,
                                         saving = false,
                                       }: CustomFeatureFormModalProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  useEffect(() => {
    if (isOpen) {
      setName(initial?.name ?? '');
      setDescription(initial?.description ?? '');
    }
  }, [isOpen, initial]);

  const canSave = name.trim().length >= 2 && !saving;

  const handleSave = async () => {
    if (!canSave) return;
    await onSave(name.trim(), description.trim() || undefined);
  };

  const footer = (
      <div className={styles.footer}>
        <Button variant="secondary" onClick={onClose} disabled={saving}>
          Cancel
        </Button>
        <Button onClick={handleSave} disabled={!canSave} loading={saving}>
          {saving ? 'Saving...' : initial ? 'Update' : 'Add Feature'}
        </Button>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title={initial ? 'Edit Custom Feature' : 'Add Custom Feature'}
          size="small"
          footer={footer}
      >
        <div className={styles.body}>
          <div className={styles.field}>
            <label className={styles.label} htmlFor="custom-feature-name">
              Name <span className={styles.required}>*</span>
            </label>
            <input
                id="custom-feature-name"
                type="text"
                className={styles.input}
                value={name}
                onChange={e => setName(e.target.value)}
                placeholder="e.g. Homebrew Ability"
                maxLength={100}
                autoFocus
            />
          </div>

          <div className={styles.field}>
            <label className={styles.label} htmlFor="custom-feature-desc">
              Description
            </label>
            <textarea
                id="custom-feature-desc"
                className={styles.textarea}
                value={description}
                onChange={e => setDescription(e.target.value)}
                placeholder="Describe what this feature does..."
                rows={5}
            />
          </div>
        </div>
      </Modal>
  );
}