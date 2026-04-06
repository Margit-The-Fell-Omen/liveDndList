import { RegisterForm } from '../components/auth/RegisterForm';
import { useNavigate } from 'react-router-dom';
import styles from './AuthPage.module.css';
import heroImage from '../assets/hero.png';

export function RegisterPage() {
  const navigate = useNavigate();

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <img src={heroImage} alt="D&D Hero" className={styles.heroImage} />
        <div className={styles.overlay} />
      </div>

      <div className={styles.formContainer}>
        <div className={styles.formWrapper}>
          <RegisterForm onSwitchToLogin={() => navigate('/auth')} />
        </div>
      </div>
    </div>
  );
}
