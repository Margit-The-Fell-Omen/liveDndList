import { useState, type ChangeEvent, type KeyboardEvent } from 'react';
import { useCharacter } from '@/context/CharacterContext';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import type { EquipmentItem, Currency } from '@/types';
import styles from './Equipment.module.css';

type CurrencyKey = keyof Currency;

export function Equipment() {
  const { currentCharacter, updateCharacter, updateNestedCharacter } = useCharacter();
  const [newItem, setNewItem] = useState<string>('');

  if (!currentCharacter) return null;

  const { equipment, currency } = currentCharacter;

  const addItem = (): void => {
    if (!newItem.trim()) return;

    const newEquipment: EquipmentItem = {
      id: Date.now(),
      name: newItem.trim(),
      quantity: 1,
    };

    updateCharacter({
      equipment: [...equipment, newEquipment],
    });
    setNewItem('');
  };

  const removeItem = (id: number): void => {
    updateCharacter({
      equipment: equipment.filter((item) => item.id !== id),
    });
  };

  const updateItemQuantity = (id: number, quantity: number): void => {
    updateCharacter({
      equipment: equipment.map((item) =>
        item.id === id ? { ...item, quantity: Math.max(0, quantity) } : item
      ),
    });
  };

  const handleKeyPress = (e: KeyboardEvent<HTMLInputElement>): void => {
    if (e.key === 'Enter') {
      e.preventDefault();
      addItem();
    }
  };

  const handleCurrencyChange = (key: CurrencyKey, value: string): void => {
    updateNestedCharacter(`currency.${key}`, parseInt(value, 10) || 0);
  };

  return (
    <div className={styles.equipment}>
      <h3 className={styles.title}>Equipment</h3>

      {/* Currency */}
      <div className={styles.currency}>
        {(['copper', 'silver', 'electrum', 'gold', 'platinum'] as CurrencyKey[]).map((key) => (
          <div key={key} className={styles.coin}>
            <label className={styles.coinLabel}>{key.charAt(0).toUpperCase()}P</label>
            <Input
              type="number"
              value={currency[key]}
              onChange={(e: ChangeEvent<HTMLInputElement>) =>
                handleCurrencyChange(key, e.target.value)
              }
              min={0}
            />
          </div>
        ))}
      </div>

      {/* Add Item */}
      <div className={styles.addItem}>
        <Input
          value={newItem}
          onChange={(e: ChangeEvent<HTMLInputElement>) => setNewItem(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Add new item..."
          fullWidth
        />
        <Button onClick={addItem} disabled={!newItem.trim()}>
          Add
        </Button>
      </div>

      {/* Item List */}
      <div className={styles.itemList}>
        {equipment.length === 0 ? (
          <p className={styles.emptyMessage}>No equipment yet</p>
        ) : (
          equipment.map((item) => (
            <div key={item.id} className={styles.item}>
              <span className={styles.itemName}>{item.name}</span>
              <div className={styles.itemControls}>
                <Input
                  type="number"
                  value={item.quantity}
                  onChange={(e: ChangeEvent<HTMLInputElement>) =>
                    updateItemQuantity(item.id, parseInt(e.target.value, 10) || 0)
                  }
                  min={0}
                  className={styles.quantityInput}
                />
                <button
                  type="button"
                  className={styles.removeButton}
                  onClick={() => removeItem(item.id)}
                  aria-label={`Remove ${item.name}`}
                >
                  ✕
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
