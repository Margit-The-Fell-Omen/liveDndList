import { useTheme } from '@/context/ThemeContext';
import styles from './ThemeToggle.module.css';

export function ThemeToggle() {
  const { theme, setTheme } = useTheme();

  return (
    <div className={styles.wrapper}>
      <button
        className={`${styles.option} ${theme === 'light' ? styles.active : ''}`}
        onClick={() => setTheme('light')}
        aria-label="Light theme"
        title="Light"
      >
        ☀️
      </button>
      <button
        className={`${styles.option} ${theme === 'dark' ? styles.active : ''}`}
        onClick={() => setTheme('dark')}
        aria-label="Dark theme"
        title="Dark"
      >
        🌙
      </button>
      <button
        className={`${styles.option} ${theme === 'system' ? styles.active : ''}`}
        onClick={() => setTheme('system')}
        aria-label="System theme"
        title="System"
      >
        💻
      </button>
    </div>
  );
}
