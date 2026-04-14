// src/components/auth/LoginForm.tsx

import {type FormEvent, useState} from 'react';
import {useNavigate} from 'react-router-dom'; // <-- Import the hook
import {useAuth} from '@/context/AuthContext';
import {Input} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import {validate, validators} from '@/utils/validators';
import styles from './AuthForm.module.css';

interface LoginFormProps {
  onSwitchToRegister: () => void;
}

export function LoginForm({onSwitchToRegister}: LoginFormProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {login, error: authError, clearError} = useAuth();
  const navigate = useNavigate(); // <-- Get the navigate function

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    clearError(); // Clear any previous auth errors

    const usernameError = validate(username, validators.required);
    if (usernameError) {
      setFormError(usernameError);
      return;
    }
    setFormError(null);
    setIsSubmitting(true);

    try {
      // Call the login function from the context
      await login({username, password});

      // --- THE FIX ---
      // If the login call succeeds, navigate to the main page.
      navigate('/', {replace: true});

    } catch (err) {
      // If the login function throws an error (which it does on failure),
      // the error will be set in the AuthContext. We don't need to do anything here,
      // as the component will re-render and display the authError.
      console.error("Login failed:", err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
      <div className={styles.formWrapper}>
        <h2 className={styles.title}>Welcome Back</h2>
        <p className={styles.subtitle}>Log in to access your characters.</p>

        <form onSubmit={handleSubmit} noValidate>
          {/* Display auth error from the context if it exists */}
          {(formError || authError) && (
              <div className={styles.errorBanner}>{formError || authError}</div>
          )}

          <Input
              label="Username"
              name="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
          />
          <Input
              label="Password"
              type="password"
              name="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
          />
          <Button type="submit" fullWidth disabled={isSubmitting}>
            {isSubmitting ? 'Logging in...' : 'Log In'}
          </Button>
        </form>

        <p className={styles.switch}>
          Don't have an account?{' '}
          <button onClick={onSwitchToRegister} className={styles.switchButton}>
            Sign Up
          </button>
        </p>
      </div>
  );
}
