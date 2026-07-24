import {type ChangeEvent, type FormEvent, useEffect, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import {Input} from '@/components/common/Input';
import {useCharacter} from '@/context/CharacterContext';
import type {EquipmentData, EquipmentResponse, EquipmentType} from '@/types';
import styles from './EquipmentFormModal.module.css';

const EQUIPMENT_TYPES: EquipmentType[] = ['WEAPON', 'ARMOR', 'GEAR', 'TOOL', 'CONSUMABLE', 'OTHER'];

const MAX_ACTIVE_WEAPONS = 3;
const MAX_ACTIVE_ARMOR = 1;

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
  equipped: false,
  damage: '',
  damageType: '',
  properties: '',
  armorClass: undefined,
  armorCategory: undefined
};

export function EquipmentFormModal({isOpen, onClose, itemToEdit}: EquipmentFormModalProps) {
  const {currentCharacter, addEquipment, updateEquipment, saving} = useCharacter();
  const [formData, setFormData] = useState<EquipmentData>(initialState);

  useEffect(() => {
    if (!isOpen) return;
    if (itemToEdit) {
      setFormData({
        name: itemToEdit.name,
        description: itemToEdit.description || '',
        quantity: itemToEdit.quantity,
        weight: itemToEdit.weight || 0,
        type: itemToEdit.type,
        equipped: itemToEdit.equipped,
        damage: itemToEdit.damage || '',
        damageType: itemToEdit.damageType || '',
        properties: itemToEdit.properties || '',
        armorClass: itemToEdit.armorClass,
      });
    } else {
      const equipment = currentCharacter?.equipment ?? [];
      const activeWeapons = equipment.filter(e => e.type === 'WEAPON' && e.equipped).length;
      const activeArmor = equipment.filter(e => e.type === 'ARMOR' && e.equipped).length;
      setFormData({
        ...initialState,
        equipped:
            (initialState.type === 'WEAPON' && activeWeapons < MAX_ACTIVE_WEAPONS) ||
            (initialState.type === 'ARMOR' && activeArmor < MAX_ACTIVE_ARMOR),
      });
    }
  }, [itemToEdit, isOpen, currentCharacter?.equipment]);

  const handleChange = (
      e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const {name, value, type} = e.target;
    if (type === 'number') {
      const parsed = value === '' ? undefined : parseFloat(value);
      setFormData(prev => ({...prev, [name]: parsed ?? 0}));
    } else {
      setFormData(prev => ({...prev, [name]: value}));
    }
  };

  const handleTypeChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const newType = e.target.value as EquipmentType;
    if (itemToEdit) {
      setFormData(prev => ({...prev, type: newType}));
      return;
    }
    const equipment = currentCharacter?.equipment ?? [];
    const activeWeapons = equipment.filter(e => e.type === 'WEAPON' && e.equipped).length;
    const activeArmor = equipment.filter(e => e.type === 'ARMOR' && e.equipped).length;
    setFormData(prev => ({
      ...prev,
      type: newType,
      equipped:
          (newType === 'WEAPON' && activeWeapons < MAX_ACTIVE_WEAPONS) ||
          (newType === 'ARMOR' && activeArmor < MAX_ACTIVE_ARMOR),
    }));
  };

  const handleToggleEquipped = () => {
    setFormData(prev => ({...prev, equipped: !prev.equipped}));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      const payload: EquipmentData = {...formData};
      if (payload.type !== 'ARMOR') {
        payload.armorClass = undefined;
        payload.armorCategory = undefined;
      }
      if (payload.type !== 'WEAPON') {
        payload.damage = '';
        payload.damageType = '';
      }
      if (itemToEdit) {
        await updateEquipment(itemToEdit.id, payload);
      } else {
        await addEquipment(payload);
      }
      onClose();
    } catch (error) {
      console.error('Failed to save equipment:', error);
    }
  };

  const isWeapon = formData.type === 'WEAPON';
  const isArmor = formData.type === 'ARMOR';
  const canBeActive = isWeapon || isArmor;

  const activeButtonLabel = (() => {
    if (isArmor) return formData.equipped ? 'Unequip Armor' : 'Equip Armor';
    return formData.equipped ? 'Set as Inactive' : 'Set as Active Weapon';
  })();

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title={itemToEdit ? 'Edit Item' : 'Add New Item'}
          size="medium"
          footer={
            <div className={styles.footer}>
              {canBeActive && (
                  <Button
                      variant={formData.equipped ? 'secondary' : 'primary'}
                      onClick={handleToggleEquipped}
                      className={styles.equipButton}
                      type="button"
                  >
                    {activeButtonLabel}
                  </Button>
              )}
              <div className={styles.footerActions}>
                <Button variant="secondary" onClick={onClose} type="button">Cancel</Button>
                <Button onClick={handleSubmit} loading={saving} type="button">
                  {itemToEdit ? 'Save Changes' : 'Add Item'}
                </Button>
              </div>
            </div>
          }
      >
        <form onSubmit={handleSubmit} className={styles.form}>
          <Input label="Item Name" name="name" value={formData.name} onChange={handleChange}
                 required/>
          <div className={styles.gridTwo}>
            <Input label="Quantity" name="quantity" type="number"
                   value={formData.quantity.toString()} onChange={handleChange} min={1} required/>
            <Input label="Weight (lbs)" name="weight" type="number"
                   value={(formData.weight ?? 0).toString()} onChange={handleChange} min={0}
                   step={0.1}/>
          </div>
          <label className={styles.label}>Item Type</label>
          <select name="type" value={formData.type} onChange={handleTypeChange}
                  className={styles.select}>
            {EQUIPMENT_TYPES.map(type => (<option key={type} value={type}>{type}</option>))}
          </select>
          {isWeapon && (
              <div className={styles.typeFields}>
                <div className={styles.gridTwo}>
                  <Input label="Damage (e.g., 1d8)" name="damage" value={formData.damage || ''}
                         onChange={handleChange}/>
                  <Input label="Damage Type" name="damageType" value={formData.damageType || ''}
                         onChange={handleChange}/>
                </div>
                <label className={styles.label}>Properties</label>
                <textarea name="properties" value={formData.properties || ''}
                          onChange={handleChange} className={styles.textarea} rows={2}
                          placeholder="e.g., Finesse, Versatile (1d10)"/>
              </div>
          )}
          {isArmor && (
              <div className={styles.typeFields}>
                <div className={styles.gridTwo}>
                  <Input label="Armor Class (AC)" name="armorClass" type="number"
                         value={(formData.armorClass ?? '').toString()}
                         onChange={handleChange} min={1}/>
                  <div>
                    <label className={styles.label}>Armor Category</label>
                    <select
                        name="armorCategory"
                        value={formData.armorCategory ?? ''}
                        onChange={handleChange}
                        className={styles.select}
                        required
                    >
                      <option value="" disabled>Select…</option>
                      <option value="LIGHT">Light</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HEAVY">Heavy</option>
                      <option value="SHIELD">Shield</option>
                    </select>
                  </div>
                </div>
                <label className={styles.label}>Properties</label>
                <textarea name="properties" value={formData.properties || ''}
                          onChange={handleChange} className={styles.textarea} rows={2}
                          placeholder="e.g., Stealth disadvantage, Heavy"/>
              </div>
          )}
          <label className={styles.label}>Description</label>
          <textarea name="description" value={formData.description || ''} onChange={handleChange}
                    className={styles.textarea} rows={3}/>
        </form>
      </Modal>
  );
}
