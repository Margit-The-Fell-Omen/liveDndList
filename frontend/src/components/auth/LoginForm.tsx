import {type FormEvent, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '@/context/AuthContext';
import {Input} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import styles from './AuthForm.module.css';

interface LoginFormProps {
  onSwitchToRegister: () => void;
}

export function LoginForm({onSwitchToRegister}: LoginFormProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(true);
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {login, error: authError, clearError} = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    clearError();

    if (!username || !password) {
      setFormError('Username and password are required.');
      return;
    }

    setFormError(null);
    setIsSubmitting(true);

    try {
      await login({username, password}, {remember});
      navigate('/', {replace: true});
    } catch (err) {
      console.error('Login failed:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
      <div className={styles.formWrapper}>
        <h2 className={styles.title}>Welcome Back</h2>
        <p className={styles.subtitle}>Log in to access your characters.</p>

        <form onSubmit={handleSubmit} noValidate className={styles.form}>
          {(formError || authError) && (
              <div className={styles.errorBanner}>{formError || authError}</div>
          )}

          <Input
              label="Username"
              name="username"
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
          />
          <Input
              label="Password"
              type="password"
              name="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
          />

          <div className={styles.rememberRow}>
            <label className={styles.rememberLabel}>
              <input
                  type="checkbox"
                  name="rememberDevice"
                  checked={remember}
                  onChange={(e) => setRemember(e.target.checked)}
                  className={styles.rememberCheckbox}
              />
              <span>Stay signed in on this device</span>
            </label>
          </div>

          <Button type="submit" fullWidth disabled={isSubmitting}>
            {isSubmitting ? 'Logging in...' : 'Log In'}
          </Button>
        </form>

        <p className={styles.switchText}>
          Don't have an account?{' '}
          <button type="button" onClick={onSwitchToRegister} className={styles.switchButton}>
            Sign Up
          </button>
        </p>
      </div>
  );
}
