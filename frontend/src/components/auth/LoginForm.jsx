import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { validators, validate } from '../../utils/validators';
import styles from './AuthForm.module.css';

export function LoginForm({ onSwitchToRegister }) {
  const { login, error: authError, clearError } = useAuth();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    
    // Clear error for this field when user types
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
    if (authError) {
      clearError();
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    newErrors.username = validate(formData.username, validators.required);
    newErrors.password = validate(formData.password, validators.required);

    setErrors(newErrors);
    return !Object.values(newErrors).some(error => error !== null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    try {
      await login({
        username: formData.username,
        password: formData.password,
      });
      // Redirect handled by App.jsx
    } catch (error) {
      // Error displayed via authError
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.form}>
      <h2 className={styles.title}>Welcome Back</h2>
      <p className={styles.subtitle}>Log in to continue your adventure</p>

      {authError && (
        <div className={styles.errorBanner}>
          {authError}
        </div>
      )}

      <Input
        label="Username"
        name="username"
        value={formData.username}
        onChange={handleChange}
        error={errors.username}
        placeholder="Enter your username"
        autoComplete="username"
        fullWidth
        required
      />

      <Input
        label="Password"
        name="password"
        type="password"
        value={formData.password}
        onChange={handleChange}
        error={errors.password}
        placeholder="Enter your password"
        autoComplete="current-password"
        fullWidth
        required
      />

      <Button
        type="submit"
        variant="primary"
        size="large"
        fullWidth
        loading={loading}
      >
        Log In
      </Button>

      <p className={styles.switchText}>
        Don't have an account?{' '}
        <button
          type="button"
          className={styles.switchButton}
          onClick={onSwitchToRegister}
        >
          Register here
        </button>
      </p>
    </form>
  );
}
