import React, {type ChangeEvent, useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Button} from '@/components/common/Button';
import {ConfirmModal} from '@/components/common/Modal';
import {EquipmentFormModal} from './EquipmentFormModal';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import type {DndCurrencyResponse, EquipmentResponse} from '@/types';
import styles from './Equipment.module.css';
import {Card} from '@/components/common/Card';

const CURRENCY_KEYS: (keyof DndCurrencyResponse)[] = ['copper', 'silver', 'electrum', 'gold', 'platinum'];

const CURRENCY_LABELS: Record<keyof DndCurrencyResponse, string> = {
  copper: 'CP',
  silver: 'SP',
  electrum: 'EP',
  gold: 'GP',
  platinum: 'PP',
};


export function Equipment({className}: { className?: string }) {
  const {currentCharacter, removeEquipment, updateCharacter} = useCharacter();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [itemToEdit, setItemToEdit] = useState<EquipmentResponse | null>(null);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [itemToDeleteId, setItemToDeleteId] = useState<number | null>(null);

  const [localCurrency, setLocalCurrency] = useState<DndCurrencyResponse>({
    copper: 0, silver: 0, electrum: 0, gold: 0, platinum: 0
  });

  useEffect(() => {
    if (currentCharacter?.currency) {
      setLocalCurrency(currentCharacter.currency);
    }
  }, [currentCharacter?.currency]);

  const debouncedUpdate = useDebouncedCallback(
      (payload: object) => {
        if (currentCharacter) updateCharacter(currentCharacter.id, payload);
      },
      500
  );

  if (!currentCharacter) return null;

  const equipment = currentCharacter.equipment || [];

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

  const handleCurrencyChange = (e: ChangeEvent<HTMLInputElement>, coin: keyof DndCurrencyResponse) => {
    const value = parseInt(e.target.value, 10) || 0;
    const updatedCurrency = {...localCurrency, [coin]: value};

    setLocalCurrency(updatedCurrency);
    debouncedUpdate({currency: updatedCurrency});
  };

  return (
      <>
        <Card title="Equipment" className={className}>
          <div className={styles.currencyRow}>
            {CURRENCY_KEYS.map(coin => (
                <div key={coin} className={styles.coinGroup}>
                  <label htmlFor={`coin-${coin}`} className={styles.coinLabel}>
                    {CURRENCY_LABELS[coin]}
                  </label>
                  <input
                      id={`coin-${coin}`}
                      type="number"
                      min="0"
                      className={styles.coinInput}
                      value={localCurrency[coin] || 0}
                      onChange={(e) => handleCurrencyChange(e, coin)}
                  />
                </div>
            ))}
          </div>

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
                        <div className={styles.itemMeta}>
                          <span className={styles.itemType}>{item.type}</span>
                          {item.equipped && <span className={styles.activeBadge}>Active</span>}
                          {item.type === 'ARMOR' && item.armorClass !== undefined && (
                              <span className={styles.itemStat}>AC {item.armorClass}</span>
                          )}
                          {item.type === 'WEAPON' && item.damage && (
                              <span
                                  className={styles.itemStat}>{item.damage}{item.damageType ? ` ${item.damageType}` : ''}</span>
                          )}
                        </div>
                      </div>
                      <button
                          type="button"
                          className={styles.deleteButton}
                          onClick={(e) => handleOpenDeleteConfirm(e, item.id)}
                          aria-label={`Delete ${item.name}`}
                          title="Delete"
                      >
                        ✕
                      </button>
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
