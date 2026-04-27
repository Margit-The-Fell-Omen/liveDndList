import {useCharacter} from '@/context/CharacterContext';
import {Button} from '@/components/common/Button';
import {ConfirmModal} from '@/components/common/Modal'; // Assuming ConfirmModal is in Modal
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
  isDesktop: boolean;
}

export function Sidebar({isExpanded, setIsExpanded, isDesktop}: SidebarProps) {
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

  const handleMouseEnter = () => {
    if (isDesktop) {
      setIsExpanded(true);
    }
  };

  const handleMouseLeave = () => {
    if (isDesktop) {
      setIsExpanded(false);
    }
  };

  return (
      <>
        <aside
            className={`${styles.sidebar} ${isExpanded ? styles.expanded : ''}`}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
        >

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
                  characters.map((character) => (
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
