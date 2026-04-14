// src/components/common/Modal.tsx
import React from 'react';
import {Button} from './Button';

// Define the props for our ConfirmModal component
interface ConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: 'primary' | 'danger';
}

export function ConfirmModal({
                               isOpen,
                               onClose,
                               onConfirm,
                               title,
                               message,
                               confirmText = 'Confirm',
                               cancelText = 'Cancel',
                               variant = 'primary',
                             }: ConfirmModalProps) {
  if (!isOpen) return null;

  return (
      <div className="modal-overlay">
        <div className="modal-content">
          <h2>{title}</h2>
          <p>{message}</p>
          <div className="modal-actions">
            <Button onClick={onClose} variant="secondary">{cancelText}</Button>
            <Button onClick={onConfirm} variant={variant}>{confirmText}</Button>
          </div>
        </div>
      </div>
  );
}
