// src/components/layout/Sidebar.tsx

import {useCharacter} from '@/context/CharacterContext';
import {Button} from '@/components/common/Button';
import {ConfirmModal} from '@/components/common/Modal';
import {CreateCharacterModal} from '@/components/character/CreateCharacterModal';
import {useState} from 'react';
import styles from './Sidebar.module.css';

interface DeleteModalState {
  open: boolean;
  id: number | null;
}

interface SidebarProps {
  isExpanded: boolean;
  setIsExpanded: (isExpanded: boolean) => void;
}

export function Sidebar({isExpanded, setIsExpanded}: SidebarProps) {
  const {
    characters, // This is of type CharacterSummary[]
    currentCharacter,
    loading,
    selectCharacter,
    deleteCharacter,
  } = useCharacter();

  const [deleteModal, setDeleteModal] = useState<DeleteModalState>({open: false, id: null});
  const [isCreateModalOpen, setIsCreateModalOpen] = useState<boolean>(false);

  const handleDeleteConfirm = async (): Promise<void> => {
    if (deleteModal.id !== null) {
      await deleteCharacter(deleteModal.id);
    }
    setDeleteModal({open: false, id: null});
  };

  const isDesktop = window.innerWidth > 1024;

  return (
      <>
        <aside
            className={`${styles.sidebar} ${isExpanded ? styles.expanded : ''}`}
            onMouseEnter={() => isDesktop && setIsExpanded(true)}
            onMouseLeave={() => isDesktop && setIsExpanded(false)}
        >
          <button
              className={styles.expandButton}
              onClick={() => setIsExpanded(!isExpanded)}
              aria-label={isExpanded ? 'Collapse sidebar' : 'Expand sidebar'}
          >
            <span className={styles.expandIcon}>⚔️</span>
          </button>

          <div className={styles.content}>
            <div className={styles.header}>
              <h2 className={styles.title}>Characters</h2>
            </div>

            <div className={styles.actions}>
              <Button
                  variant="primary"
                  size="small"
                  fullWidth
                  onClick={() => setIsCreateModalOpen(true)}
                  disabled={loading}
              >
                + New Character
              </Button>
            </div>

            <nav className={styles.characterList}>
              {characters.length === 0 && !loading ? (
                  <p className={styles.emptyMessage}>No characters yet.</p>
              ) : (
                  characters.map((character) => ( // `character` is a CharacterSummary
                      <div
                          key={character.id}
                          className={`${styles.characterItem} ${currentCharacter?.id === character.id ? styles.active : ''}`}
                      >
                        <button
                            className={styles.characterButton}
                            onClick={() => selectCharacter(character.id)}
                        >
                          <div className={styles.characterAvatar}>
                            {character.name.charAt(0).toUpperCase()}
                          </div>
                          <div className={styles.characterInfo}>
                            <span className={styles.characterName}>{character.name}</span>
                            {/* FIX: Use the properties from CharacterSummary */}
                            <span className={styles.characterMeta}>
                        {character.raceName} {character.classDisplay}
                      </span>
                          </div>
                        </button>

                        <button
                            className={styles.deleteButton}
                            onClick={() => setDeleteModal({open: true, id: character.id})}
                            aria-label={`Delete ${character.name}`}
                        >
                          🗑️
                        </button>
                      </div>
                  ))
              )}
              {loading && <p className={styles.emptyMessage}>Loading...</p>}
            </nav>
          </div>
        </aside>

        {!isDesktop && isExpanded && (
            <div className={styles.overlay} onClick={() => setIsExpanded(false)}/>
        )}

        <ConfirmModal
            isOpen={deleteModal.open}
            onClose={() => setDeleteModal({open: false, id: null})}
            onConfirm={handleDeleteConfirm}
            title="Delete Character"
            message="Are you sure you want to delete this character? This action cannot be undone."
            confirmText="Delete"
            variant="danger"
        />

        <CreateCharacterModal
            isOpen={isCreateModalOpen}
            onClose={() => setIsCreateModalOpen(false)}
        />
      </>
  );
}
