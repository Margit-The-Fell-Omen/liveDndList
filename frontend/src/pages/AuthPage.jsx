import { useState } from 'react';
import { LoginForm } from '../components/auth/LoginForm';
import { RegisterForm } from '../components/auth/RegisterForm';
import styles from './AuthPage.module.css';
import heroImage from '../assets/hero.png';

export function AuthPage() {
  const [mode, setMode] = useState('login');

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <img src={heroImage} alt="D&D Hero" className={styles.heroImage} />
        <div className={styles.overlay} />
      </div>

      <div className={styles.formContainer}>
        <div className={styles.formWrapper}>
          {mode === 'login' ? (
            <LoginForm onSwitchToRegister={() => setMode('register')} />
          ) : (
            <RegisterForm onSwitchToLogin={() => setMode('login')} />
          )}
        </div>
      </div>
    </div>
  );
}
