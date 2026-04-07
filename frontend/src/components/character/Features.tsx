import { useState, type ChangeEvent } from 'react';
import { useCharacter } from '@/context/CharacterContext';
import { TextArea } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { Modal } from '@/components/common/Modal';
import type { Feature } from '@/types';
import styles from './Features.module.css';

interface FormData {
  name: string;
  description: string;
}

export function Features() {
  const { currentCharacter, updateCharacter } = useCharacter();
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingFeature, setEditingFeature] = useState<Feature | null>(null);
  const [formData, setFormData] = useState<FormData>({ name: '', description: '' });

  if (!currentCharacter) return null;

  const { features } = currentCharacter;

  const openAddModal = (): void => {
    setEditingFeature(null);
    setFormData({ name: '', description: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (feature: Feature): void => {
    setEditingFeature(feature);
    setFormData({ name: feature.name, description: feature.description });
    setIsModalOpen(true);
  };

  const handleSave = (): void => {
    if (!formData.name.trim()) return;

    if (editingFeature) {
      updateCharacter({
        features: features.map((f) =>
          f.id === editingFeature.id
            ? { ...f, name: formData.name, description: formData.description }
            : f
        ),
      });
    } else {
      const newFeature: Feature = {
        id: Date.now(),
        name: formData.name,
        description: formData.description,
      };
      updateCharacter({
        features: [...features, newFeature],
      });
    }

    setIsModalOpen(false);
  };

  const handleDelete = (id: number): void => {
    updateCharacter({
      features: features.filter((f) => f.id !== id),
    });
  };

  return (
    <div className={styles.features}>
      <div className={styles.header}>
        <h3 className={styles.title}>Features & Traits</h3>
        <Button variant="secondary" size="small" onClick={openAddModal}>
          + Add
        </Button>
      </div>

      <div className={styles.list}>
        {features.length === 0 ? (
          <p className={styles.emptyMessage}>
            No features or traits yet. Add your class features, racial traits, and feats here.
          </p>
        ) : (
          features.map((feature) => (
            <div key={feature.id} className={styles.feature}>
              <div className={styles.featureHeader} onClick={() => openEditModal(feature)}>
                <h4 className={styles.featureName}>{feature.name}</h4>
                <button
                  type="button"
                  className={styles.deleteButton}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDelete(feature.id);
                  }}
                  aria-label={`Delete ${feature.name}`}
                >
                  ✕
                </button>
              </div>
              {feature.description && (
                <p className={styles.featureDescription}>{feature.description}</p>
              )}
            </div>
          ))
        )}
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingFeature ? 'Edit Feature' : 'Add Feature'}
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSave} disabled={!formData.name.trim()}>
              Save
            </Button>
          </>
        }
      >
        <div className={styles.form}>
          <input
            type="text"
            value={formData.name}
            onChange={(e: ChangeEvent<HTMLInputElement>) =>
              setFormData({ ...formData, name: e.target.value })
            }
            placeholder="Feature name"
            className={styles.nameInput}
            autoFocus
          />
          <TextArea
            value={formData.description}
            onChange={(e: ChangeEvent<HTMLTextAreaElement>) =>
              setFormData({ ...formData, description: e.target.value })
            }
            placeholder="Description (optional)"
            rows={4}
            fullWidth
          />
        </div>
      </Modal>
    </div>
  );
}
