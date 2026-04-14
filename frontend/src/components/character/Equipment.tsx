// src/components/character/Equipment.tsx

import {type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import type {DndCurrencyResponse} from '@/types';
import styles from './Equipment.module.css';

// Define the keys for our currency object for easy mapping
const CURRENCY_KEYS: (keyof DndCurrencyResponse)[] = ['copper', 'silver', 'electrum', 'gold', 'platinum'];

export function Equipment() {
  const {currentCharacter, updateCharacter} = useCharacter();

  if (!currentCharacter) {
    return null;
  }

  // Destructure the correct properties from the character object
  const {equipment, currency} = currentCharacter;

  // --- Event Handlers ---

  const handleCurrencyChange = (key: keyof DndCurrencyResponse, value: string) => {
    const newCurrency = {
      ...currency,
      [key]: parseInt(value, 10) || 0,
    };
    // updateCharacter(currentCharacter.id, { currency: newCurrency });
    console.log(`Updating currency: ${key} to ${value}`);
    console.warn("Currency updates require a 'currency' field in the backend's CharacterUpdateRequest DTO.");
  };

  const handleRemoveItem = (itemId: number) => {
    // This would eventually call an API endpoint like:
    // charactersApi.removeEquipment(currentCharacter.id, itemId);
    // For now, we'll log it.
    console.log(`Request to remove equipment item with ID: ${itemId}`);
    console.warn("Removing equipment requires a dedicated API endpoint.");
  };

  // Note: Adding and updating items is now more complex. It would involve a modal
  // to fill out the full EquipmentRequest DTO and call a dedicated API endpoint.
  // The simple "add by name" input is removed for now to reflect the new data model.

  return (
      <div className={styles.equipment}>
        <h3 className={styles.title}>Equipment & Currency</h3>

        {/* Currency */}
        <div className={styles.currency}>
          {CURRENCY_KEYS.map((key) => (
              <div key={key} className={styles.coin}>
                {/* FIX: Use `key.slice(0, 2).toUpperCase()` for 'CP', 'SP', etc. */}
                <label className={styles.coinLabel}>{key.slice(0, 2).toUpperCase()}</label>
                <Input
                    type="number"
                    value={currency[key]}
                    onChange={(e: ChangeEvent<HTMLInputElement>) =>
                        handleCurrencyChange(key, e.target.value)
                    }
                    min={0}
                    className={styles.coinInput}
                />
              </div>
          ))}
        </div>

        {/* Item List */}
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
                      {/* For now, we just display the quantity. Editing would require a more complex handler. */}
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

        {/* The simple text input for adding items is removed.
          This should be replaced by a more robust "Add Equipment" button
          that opens a modal to search/create a full equipment item. */}
      </div>
  );
}
