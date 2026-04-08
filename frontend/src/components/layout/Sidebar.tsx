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

// Add props for expanded state
interface SidebarProps {
  isExpanded: boolean;
  setIsExpanded: (isExpanded: boolean) => void;
}

export function Sidebar({isExpanded, setIsExpanded}: SidebarProps) {
  const {
    characters,
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

  // Use a variable to decide when to expand on hover (desktop only)
  const isDesktop = window.innerWidth > 1024;

  return (
      <>
        <aside
            className={`${styles.sidebar} ${isExpanded ? styles.expanded : ''}`}
            // Only expand on hover on desktop
            onMouseEnter={() => isDesktop && setIsExpanded(true)}
            onMouseLeave={() => isDesktop && setIsExpanded(false)}
        >
          <button
              className={styles.expandButton}
              // On any device, this button toggles the state
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
              {characters.length === 0 ? (
                  <p className={styles.emptyMessage}>No characters yet. Create your first hero!</p>
              ) : (
                  characters.map((character) => (
                      <div
                          key={character.id}
                          className={`
                    ${styles.characterItem}
                    ${currentCharacter?.id === character.id ? styles.active : ''}
                  `}
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
                            <span className={styles.characterMeta}>
                        {character.race?.name} {character.characterClass?.name} Lvl {character.level || 1}
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
            </nav>
          </div>
        </aside>

        {/* Show overlay only on mobile when expanded */}
        {!isDesktop && isExpanded &&
            <div className={styles.overlay} onClick={() => setIsExpanded(false)}/>}

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
