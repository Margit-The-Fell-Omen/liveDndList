import {useState, useEffect, type FormEvent} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import {Input} from '@/components/common/Input';
import {useCharacter} from '@/context/CharacterContext';
import type {EquipmentData, EquipmentResponse, EquipmentType} from '@/types';
import styles from './EquipmentFormModal.module.css';

const EQUIPMENT_TYPES: EquipmentType[] = ['WEAPON', 'ARMOR', 'GEAR', 'TOOL', 'CONSUMABLE', 'OTHER'];

interface EquipmentFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  itemToEdit?: EquipmentResponse | null;
}

const initialState: EquipmentData = {
  name: '',
  description: '',
  quantity: 1,
  weight: 0,
  type: 'GEAR',
  damage: '',
  damageType: '',
  properties: '',
};

export function EquipmentFormModal({isOpen, onClose, itemToEdit}: EquipmentFormModalProps) {
  const {addEquipment, updateEquipment, saving} = useCharacter();
  const [formData, setFormData] = useState<EquipmentData>(initialState);

  useEffect(() => {
    if (itemToEdit) {
      // If editing, populate the form with the item's data
      setFormData({
        name: itemToEdit.name,
        description: itemToEdit.description || '',
        quantity: itemToEdit.quantity,
        weight: itemToEdit.weight || 0,
        type: itemToEdit.type,
        damage: itemToEdit.damage || '',
        damageType: itemToEdit.damageType || '',
        properties: itemToEdit.properties || '',
      });
    } else {
      // If adding, reset to the initial state
      setFormData(initialState);
    }
  }, [itemToEdit, isOpen]); // Reset form when modal opens/changes item

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const {name, value, type} = e.target;

    // Handle number inputs
    if (type === 'number') {
      setFormData(prev => ({...prev, [name]: parseFloat(value) || 0}));
    } else {
      setFormData(prev => ({...prev, [name]: value}));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      if (itemToEdit) {
        await updateEquipment(itemToEdit.id, formData);
      } else {
        await addEquipment(formData);
      }
      onClose(); // Close modal on success
    } catch (error) {
      console.error("Failed to save equipment:", error);
      // Optional: show a toast notification with the error
    }
  };

  const isWeapon = formData.type === 'WEAPON';

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title={itemToEdit ? 'Edit Item' : 'Add New Item'}
          size="medium"
          footer={
            <>
              <Button variant="secondary" onClick={onClose}>Cancel</Button>
              <Button onClick={handleSubmit} loading={saving}>
                {itemToEdit ? 'Save Changes' : 'Add Item'}
              </Button>
            </>
          }
      >
        <form onSubmit={handleSubmit} className={styles.form}>
          <Input
              label="Item Name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
          />

          <div className={styles.gridTwo}>
            <Input
                label="Quantity"
                name="quantity"
                type="number"
                value={formData.quantity.toString()}
                onChange={handleChange}
                min={1}
                required
            />
            <Input
                label="Weight (lbs)"
                name="weight"
                type="number"
                value={formData.weight.toString()}
                onChange={handleChange}
                min={0}
                step={0.1}
            />
          </div>

          <label className={styles.label}>Item Type</label>
          <select name="type" value={formData.type} onChange={handleChange}
                  className={styles.select}>
            {EQUIPMENT_TYPES.map(type => (
                <option key={type} value={type}>{type}</option>
            ))}
          </select>

          {isWeapon && (
              <div className={styles.weaponFields}>
                <div className={styles.gridTwo}>
                  <Input label="Damage (e.g., 1d8)" name="damage" value={formData.damage}
                         onChange={handleChange}/>
                  <Input label="Damage Type" name="damageType" value={formData.damageType}
                         onChange={handleChange}/>
                </div>
                <label className={styles.label}>Properties</label>
                <textarea
                    name="properties"
                    value={formData.properties}
                    onChange={handleChange}
                    className={styles.textarea}
                    rows={2}
                    placeholder="e.g., Finesse, Versatile (1d10)"
                />
              </div>
          )}

          <label className={styles.label}>Description</label>
          <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className={styles.textarea}
              rows={3}
          />
        </form>
      </Modal>
  );
}