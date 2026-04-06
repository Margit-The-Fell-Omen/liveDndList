import { useAuth } from '../../context/AuthContext';
import { ThemeToggle } from '../common/ThemeToggle';
import { Button } from '../common/Button';
import styles from './Header.module.css';

export function Header() {
  const { user, logout } = useAuth();

  return (
    <header className={styles.header}>
      <div className={styles.left}>
        <h1 className={styles.logo}>
          <span className={styles.logoIcon}>🎲</span>
          D&D Character Sheet
        </h1>
      </div>

      <div className={styles.right}>
        <ThemeToggle />
        
        {user && (
          <>
            <span className={styles.username}>
              {user.username}
            </span>
            <Button variant="ghost" size="small" onClick={logout}>
              Logout
            </Button>
          </>
        )}
      </div>
    </header>
  );
}
