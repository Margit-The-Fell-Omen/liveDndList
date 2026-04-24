import {useAuth} from '@/context/AuthContext';
import {ThemeToggle} from '@/components/common/ThemeToggle';
import {Button} from '@/components/common/Button';
import styles from './Header.module.css';

// NEW: Props for sidebar toggle functionality
interface HeaderProps {
  isSidebarExpanded: boolean;
  onToggleSidebar: () => void;
}

export function Header({isSidebarExpanded, onToggleSidebar}: HeaderProps) {
  const {user, logout} = useAuth();

  return (
      <header className={styles.header}>
        {/* NEW: Hamburger menu for mobile */}
        <button
            className={styles.hamburger}
            onClick={onToggleSidebar}
            aria-label={isSidebarExpanded ? 'Close menu' : 'Open menu'}
            aria-expanded={isSidebarExpanded}
        >
          <span className={styles.hamburgerLine}></span>
          <span className={styles.hamburgerLine}></span>
          <span className={styles.hamburgerLine}></span>
        </button>

        <div className={styles.left}>
          <h1 className={styles.logo}>
            <span className={styles.logoIcon}>🎲</span>
            <span
                className={styles.logoText}>D&D Sheet</span> {/* MODIFIED: Shorter text for mobile */}
          </h1>
        </div>

        <div className={styles.right}>
          <ThemeToggle/>
          {user && (
              <>
                <span className={styles.username}>{user.username}</span>
                <Button variant="ghost" size="small" onClick={logout}>
                  Logout
                </Button>
              </>
          )}
        </div>
      </header>
  );
}
