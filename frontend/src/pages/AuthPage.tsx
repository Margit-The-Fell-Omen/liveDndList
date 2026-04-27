// src/pages/AuthPage.tsx

import {useState, useMemo} from 'react';
import {LoginForm} from '@/components/auth/LoginForm';
import {RegisterForm} from '@/components/auth/RegisterForm';
import styles from './AuthPage.module.css';

const wallpaperModules = import.meta.glob<{
  default: string
}>('../assets/wallpapers/*', {eager: true, as: 'url'});
const wallpapers = Object.values(wallpaperModules);

type AuthMode = 'login' | 'register';

export function AuthPage() {
  const [mode, setMode] = useState<AuthMode>('login');

  const wallpaperUrl = useMemo(() => {
    if (wallpapers.length === 0) return '';
    const randomIndex = Math.floor(Math.random() * wallpapers.length);
    return wallpapers[randomIndex];
  }, []);

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
