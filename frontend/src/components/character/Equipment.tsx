// src/components/character/Equipment.tsx

import {useState, type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import {ConfirmModal, Modal} from '@/components/common/Modal';
import {EquipmentFormModal} from './EquipmentFormModal'; // NEW
import type {DndCurrencyResponse, EquipmentResponse} from '@/types';
import styles from './Equipment.module.css';
import {Card} from '@/components/common/Card';

const CURRENCY_KEYS: (keyof DndCurrencyResponse)[] = ['copper', 'silver', 'electrum', 'gold', 'platinum'];

export function Equipment({className}: { className?: string }) {
  const {currentCharacter, removeEquipment} = useCharacter();

  // State for modals
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [itemToEdit, setItemToEdit] = useState<EquipmentResponse | null>(null);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [itemToDeleteId, setItemToDeleteId] = useState<number | null>(null);

  if (!currentCharacter) {
    return null;
  }

  const equipment = currentCharacter.equipment || [];
  const currency = currentCharacter.currency || {
    copper: 0,
    silver: 0,
    electrum: 0,
    gold: 0,
    platinum: 0
  };

  // Handlers for opening modals
  const handleOpenAddModal = () => {
    setItemToEdit(null);
    setIsFormOpen(true);
  };

  const handleOpenEditModal = (item: EquipmentResponse) => {
    setItemToEdit(item);
    setIsFormOpen(true);
  };

  // Handlers for deletion
  const handleOpenDeleteConfirm = (itemId: number) => {
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

  const handleCurrencyChange = (key: keyof DndCurrencyResponse, value: string) => {
    console.warn("Currency updates require a 'currency' field in the backend's CharacterUpdateRequest DTO.");
  };

  return (
      <>
        <Card title="Equipment" className={className}>
          <div className={styles.currency}>
            {CURRENCY_KEYS.map((key) => (
                <div key={key} className={styles.coin}>
                  <label className={styles.coinLabel}>{key.slice(0, 2).toUpperCase()}</label>
                  <Input
                      type="number"
                      defaultValue={currency[key]}
                      onChange={(e: ChangeEvent<HTMLInputElement>) => handleCurrencyChange(key, e.target.value)}
                      min={0}
                      className={styles.coinInput}
                  />
                </div>
            ))}
          </div>

          <div className={styles.itemListHeader}>
            <h4 className={styles.itemsTitle}>Items</h4>
            <Button size="small" onClick={handleOpenAddModal}>
              + Add Item
            </Button>
          </div>

          <div className={styles.itemList}>
            {equipment.length === 0 ? (
                <p className={styles.emptyMessage}>The backpack is empty.</p>
            ) : (
                equipment.map((item) => (
                    <div key={item.id} className={styles.item}>
                <span className={styles.itemName}>
                  {item.name}
                  {item.quantity > 1 && ` (x${item.quantity})`}
                </span>
                      <div className={styles.itemControls}>
                        <Button variant="ghost" size="small"
                                onClick={() => handleOpenEditModal(item)}>Edit</Button>
                        <button
                            type="button"
                            className={styles.removeButton}
                            onClick={() => handleOpenDeleteConfirm(item.id)}
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

        {/* Modals rendered here */}
        <EquipmentFormModal
            isOpen={isFormOpen}
            onClose={() => setIsFormOpen(false)}
            itemToEdit={itemToEdit}
        />

        <ConfirmModal
            isOpen={isConfirmOpen}
            onClose={() => setIsConfirmOpen(false)}
            onConfirm={handleConfirmDelete}
            title="Delete Item"
            message="Are you sure you want to permanently delete this item from your inventory?"
            variant="danger"
            confirmText="Delete"
        />
      </>
  );
}
