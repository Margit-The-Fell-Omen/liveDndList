// src/components/character/Equipment.tsx

import {type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import type {DndCurrencyResponse} from '@/types';
import styles from './Equipment.module.css';

const CURRENCY_KEYS: (keyof DndCurrencyResponse)[] = ['copper', 'silver', 'electrum', 'gold', 'platinum'];

export function Equipment() {
  const {currentCharacter} = useCharacter();

  if (!currentCharacter) {
    return null;
  }

  // --- THE FIX ---
  // Use a fallback empty array for equipment and a default object for currency.
  const equipment = currentCharacter.equipment || [];
  const currency = currentCharacter.currency || {
    copper: 0,
    silver: 0,
    electrum: 0,
    gold: 0,
    platinum: 0
  };
  // --- END OF FIX ---

  const handleCurrencyChange = (key: keyof DndCurrencyResponse, value: string) => {
    console.warn("Currency updates require a 'currency' field in the backend's CharacterUpdateRequest DTO.");
  };

  const handleRemoveItem = (itemId: number) => {
    console.warn("Removing equipment requires a dedicated API endpoint.");
  };

  return (
      <div className={styles.equipment}>
        <h3 className={styles.title}>Equipment & Currency</h3>

        <div className={styles.currency}>
          {CURRENCY_KEYS.map((key) => (
              <div key={key} className={styles.coin}>
                <label className={styles.coinLabel}>{key.slice(0, 2).toUpperCase()}</label>
                <Input
                    type="number"
                    defaultValue={currency[key]} // This will now safely access the property
                    onChange={(e: ChangeEvent<HTMLInputElement>) =>
                        handleCurrencyChange(key, e.target.value)
                    }
                    min={0}
                    className={styles.coinInput}
                />
              </div>
          ))}
        </div>

        <div className={styles.itemList}>
          {/* This `equipment.length` check is now safe */}
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
                      <button
                          type="button"
                          className={styles.removeButton}
                          onClick={() => handleRemoveItem(item.id)}
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
