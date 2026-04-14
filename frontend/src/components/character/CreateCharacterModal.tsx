// src/components/character/CreateCharacterModal.tsx
import React from 'react';
import {Button} from '@/components/common/Button';

// Define the props for this component
interface CreateCharacterModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function CreateCharacterModal({isOpen, onClose}: CreateCharacterModalProps) {
  if (!isOpen) return null;

  return (
      <div className="modal-overlay">
        <div className="modal-content">
          <h2>Create New Character</h2>
          <p>Creation form will go here...</p>
          <div className="modal-actions">
            <Button onClick={onClose} variant="secondary">Close</Button>
          </div>
        </div>
      </div>
  );
}
