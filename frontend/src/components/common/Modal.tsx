// src/components/common/Modal.tsx

import React from 'react';
import {Button} from './Button';
import styles from './Modal.module.css';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  size?: 'small' | 'medium' | 'large';
  children: React.ReactNode;
  footer?: React.ReactNode;
}

export function Modal({
                        isOpen,
                        onClose,
                        title,
                        size = 'medium',
                        children,
                        footer
                      }: ModalProps) {
  if (!isOpen) return null;

  const handleOverlayClick = () => {
    onClose();
  };

  const handleModalClick = (e: React.MouseEvent) => {
    e.stopPropagation();
  };

  return (
      <div className={styles.overlay} onClick={handleOverlayClick}>
        <div className={`${styles.modal} ${styles[size]}`} onClick={handleModalClick}>
          <div className={styles.header}>
            <h2 className={styles.title}>{title}</h2>
            <button onClick={onClose} className={styles.closeButton} aria-label="Close modal">
              &times;
            </button>
          </div>
          <div className={styles.content}>
            {children}
          </div>
          {footer && (
              <div className={styles.footer}>
                {footer}
              </div>
          )}
        </div>
      </div>
  );
}


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
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title={title}
          size="small"
          footer={
            <>
              <Button onClick={onClose} variant="secondary">{cancelText}</Button>
              <Button onClick={onConfirm} variant={variant}>{confirmText}</Button>
            </>
          }
      >
        <p>{message}</p>
      </Modal>
  );
}
