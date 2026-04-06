import { useState } from 'react';
import { useCharacter } from '../../context/CharacterContext';
import { Input, TextArea } from '../common/Input';
import { Button } from '../common/Button';
import styles from './Equipment.module.css';

export function Equipment() {
  const { currentCharacter, updateCharacter, updateNestedCharacter } = useCharacter();
  const [newItem, setNewItem] = useState('');

  if (!currentCharacter) return null;

  const { equipment, currency } = currentCharacter;

  const addItem = () => {
    if (!newItem.trim()) return;
    
    updateCharacter({
      equipment: [...equipment, { id: Date.now(), name: newItem.trim(), quantity: 1 }]
    });
    setNewItem('');
  };

  const removeItem = (id) => {
    updateCharacter({
      equipment: equipment.filter(item => item.id !== id)
    });
  };

  const updateItemQuantity = (id, quantity) => {
    updateCharacter({
      equipment: equipment.map(item =>
        item.id === id ? { ...item, quantity: Math.max(0, quantity) } : item
      )
    });
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      addItem();
    }
  };

  return (
    <div className={styles.equipment}>
      <h3 className={styles.title}>Equipment</h3>

      {/* Currency */}
      <div className={styles.currency}>
        <div className={styles.coin}>
          <label className={styles.coinLabel}>CP</label>
          <Input
            type="number"
            value={currency.copper}
            onChange={(e) => updateNestedCharacter(
              'currency.copper',
              parseInt(e.target.value, 10) || 0
            )}
            min="0"
          />
        </div>
        <div className={styles.coin}>
          <label className={styles.coinLabel}>SP</label>
          <Input
            type="number"
            value={currency.silver}
            onChange={(e) => updateNestedCharacter(
              'currency.silver',
              parseInt(e.target.value, 10) || 0
            )}
            min="0"
          />
        </div>
        <div className={styles.coin}>
          <label className={styles.coinLabel}>EP</label>
          <Input
            type="number"
            value={currency.electrum}
            onChange={(e) => updateNestedCharacter(
              'currency.electrum',
              parseInt(e.target.value, 10) || 0
            )}
            min="0"
          />
        </div>
        <div className={styles.coin}>
          <label className={styles.coinLabel}>GP</label>
          <Input
            type="number"
            value={currency.gold}
            onChange={(e) => updateNestedCharacter(
              'currency.gold',
              parseInt(e.target.value, 10) || 0
            )}
            min="0"
          />
        </div>
        <div className={styles.coin}>
          <label className={styles.coinLabel}>PP</label>
          <Input
            type="number"
            value={currency.platinum}
            onChange={(e) => updateNestedCharacter(
              'currency.platinum',
              parseInt(e.target.value, 10) || 0
            )}
            min="0"
          />
        </div>
      </div>

      {/* Add Item */}
      <div className={styles.addItem}>
        <Input
          value={newItem}
          onChange={(e) => setNewItem(e.target.value)}
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
          equipment.map(item => (
            <div key={item.id} className={styles.item}>
              <span className={styles.itemName}>{item.name}</span>
              <div className={styles.itemControls}>
                <Input
                  type="number"
                  value={item.quantity}
                  onChange={(e) => updateItemQuantity(
                    item.id,
                    parseInt(e.target.value, 10) || 0
                  )}
                  min="0"
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
