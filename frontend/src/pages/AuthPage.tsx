// src/pages/AuthPage.tsx

import {useState, useMemo} from 'react';
import {LoginForm} from '@/components/auth/LoginForm';
import {RegisterForm} from '@/components/auth/RegisterForm';
import styles from './AuthPage.module.css';

// 1. Use Vite's glob import to get all images from the wallpapers folder.
// The `{ eager: true, as: 'url' }` options make Vite return the final public URLs directly.
const wallpaperModules = import.meta.glob<{
  default: string
}>('../assets/wallpapers/*', {eager: true, as: 'url'});
const wallpapers = Object.values(wallpaperModules);

type AuthMode = 'login' | 'register';

export function AuthPage() {
  const [mode, setMode] = useState<AuthMode>('login');

  // 2. Pick a single random wallpaper URL and memoize it so it doesn't change on re-renders.
  const wallpaperUrl = useMemo(() => {
    if (wallpapers.length === 0) return '';
    const randomIndex = Math.floor(Math.random() * wallpapers.length);
    return wallpapers[randomIndex];
  }, []); // Empty dependency array ensures this runs only once.

  // 3. Apply the chosen wallpaper URL as an inline style.
  const pageStyle = {
    backgroundImage: `url(${wallpaperUrl})`,
  };

  return (
      <div className={styles.page} style={pageStyle}>
        <div className={styles.formContainer}>
          {mode === 'login' ? (
              <LoginForm onSwitchToRegister={() => setMode('register')}/>
          ) : (
              <RegisterForm onSwitchToLogin={() => setMode('login')}/>
          )}
        </div>
      </div>
  );
}
