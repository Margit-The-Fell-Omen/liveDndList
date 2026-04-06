import { useState } from 'react';
import { useCharacter } from '../../context/CharacterContext';
import { TextArea } from '../common/Input';
import { Button } from '../common/Button';
import { Modal } from '../common/Modal';
import styles from './Features.module.css';

export function Features() {
  const { currentCharacter, updateCharacter } = useCharacter();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingFeature, setEditingFeature] = useState(null);
  const [formData, setFormData] = useState({ name: '', description: '' });

  if (!currentCharacter) return null;

  const { features } = currentCharacter;

  const openAddModal = () => {
    setEditingFeature(null);
    setFormData({ name: '', description: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (feature) => {
    setEditingFeature(feature);
    setFormData({ name: feature.name, description: feature.description });
    setIsModalOpen(true);
  };

  const handleSave = () => {
    if (!formData.name.trim()) return;

    if (editingFeature) {
      updateCharacter({
        features: features.map(f =>
          f.id === editingFeature.id
            ? { ...f, name: formData.name, description: formData.description }
            : f
        )
      });
    } else {
      updateCharacter({
        features: [...features, {
          id: Date.now(),
          name: formData.name,
          description: formData.description,
        }]
      });
    }

    setIsModalOpen(false);
  };

  const handleDelete = (id) => {
    updateCharacter({
      features: features.filter(f => f.id !== id)
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
          features.map(feature => (
            <div key={feature.id} className={styles.feature}>
              <div
                className={styles.featureHeader}
                onClick={() => openEditModal(feature)}
              >
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
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="Feature name"
            className={styles.nameInput}
            autoFocus
          />
          <TextArea
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Description (optional)"
            rows={4}
            fullWidth
          />
        </div>
      </Modal>
    </div>
  );
}
