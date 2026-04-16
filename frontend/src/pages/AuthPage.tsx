// src/pages/AuthPage.tsx

import {useState} from 'react';
import {LoginForm} from '@/components/auth/LoginForm';
import {RegisterForm} from '@/components/auth/RegisterForm';
import styles from './AuthPage.module.css';

type AuthMode = 'login' | 'register';

export function AuthPage() {
  const [mode, setMode] = useState<AuthMode>('login');

  return (
      <div className={styles.page}>
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
