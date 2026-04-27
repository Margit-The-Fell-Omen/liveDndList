// src/components/character/Equipment.tsx

import React, {useState} from 'react'; // Add React import
import {useCharacter} from '@/context/CharacterContext';
import {Button} from '@/components/common/Button';
import {ConfirmModal} from '@/components/common/Modal';
import {EquipmentFormModal} from './EquipmentFormModal';
import type {DndCurrencyResponse, EquipmentResponse} from '@/types';
import styles from './Equipment.module.css';
import {Card} from '@/components/common/Card';

const CURRENCY_KEYS: (keyof DndCurrencyResponse)[] = ['copper', 'silver', 'electrum', 'gold', 'platinum'];

export function Equipment({className}: { className?: string }) {
  const {currentCharacter, removeEquipment} = useCharacter();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [itemToEdit, setItemToEdit] = useState<EquipmentResponse | null>(null);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [itemToDeleteId, setItemToDeleteId] = useState<number | null>(null);

  if (!currentCharacter) return null;

  const equipment = currentCharacter.equipment || [];
  const currency = currentCharacter.currency || {
    copper: 0,
    silver: 0,
    electrum: 0,
    gold: 0,
    platinum: 0
  };

  const handleOpenAddModal = () => {
    setItemToEdit(null);
    setIsFormOpen(true);
  };

  const handleOpenEditModal = (item: EquipmentResponse) => {
    setItemToEdit(item);
    setIsFormOpen(true);
  };

  const handleOpenDeleteConfirm = (e: React.MouseEvent, itemId: number) => {
    e.stopPropagation();
    setItemToDeleteId(itemId);
    setIsConfirmOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (itemToDeleteId) {
      await removeEquipment(itemToDeleteId);
    }
    setIsConfirmOpen(false);
    setItemToDeleteId(null);
  };

  const handleCurrencyChange = () => { /* ... */
  };

  return (
      <>
        <Card title="Equipment" className={className}>
          <div className={styles.currency}>{/* ... */}</div>

          <div className={styles.itemListHeader}>
            <h4 className={styles.itemsTitle}>Items</h4>
            <Button size="small" onClick={handleOpenAddModal}>+ Add Item</Button>
          </div>

          <div className={styles.itemList}>
            {equipment.length === 0 ? (
                <p className={styles.emptyMessage}>The backpack is empty.</p>
            ) : (
                equipment.map((item) => (
                    <div
                        key={item.id}
                        className={styles.item}
                        onClick={() => handleOpenEditModal(item)}
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' || e.key === ' ') handleOpenEditModal(item);
                        }}
                    >
                      <div className={styles.itemNameContainer}>
                <span className={styles.itemName}>
                  {item.name}
                  {item.quantity > 1 && ` (x${item.quantity})`}
                </span>
                        <span className={styles.itemType}>{item.type}</span>
                      </div>
                      <div className={styles.itemControls}>
                        <button
                            type="button"
                            className={styles.removeButton}
                            onClick={(e) => handleOpenDeleteConfirm(e, item.id)}
                            aria-label={`Remove ${item.name}`}
                        >
                          &times;
                        </button>
                      </div>
                    </div>
                ))
            )}
          </div>
        </Card>

        <EquipmentFormModal isOpen={isFormOpen} onClose={() => setIsFormOpen(false)}
                            itemToEdit={itemToEdit}/>
        <ConfirmModal isOpen={isConfirmOpen} onClose={() => setIsConfirmOpen(false)}
                      onConfirm={handleConfirmDelete} title="Delete Item"
                      message="Are you sure you want to permanently delete this item?"
                      variant="danger" confirmText="Delete"/>
      </>
  );
}
