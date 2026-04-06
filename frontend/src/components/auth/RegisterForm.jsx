import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { validators, validate } from '../../utils/validators';
import styles from './AuthForm.module.css';

export function RegisterForm({ onSwitchToLogin }) {
  const { register, error: authError, clearError } = useAuth();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
    if (authError) {
      clearError();
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    newErrors.username = validate(
      formData.username, 
      validators.required, 
      validators.username
    );
    
    newErrors.email = validate(
      formData.email, 
      validators.required, 
      validators.email
    );
    
    newErrors.password = validate(
      formData.password, 
      validators.required, 
      validators.password
    );
    
    newErrors.confirmPassword = validate(
      formData.confirmPassword,
      validators.required,
      validators.match('password', () => formData.password)
    );

    setErrors(newErrors);
    return !Object.values(newErrors).some(error => error !== null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    try {
      await register({
        username: formData.username,
        email: formData.email,
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
      <h2 className={styles.title}>Create Account</h2>
      <p className={styles.subtitle}>Join the adventure</p>

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
        placeholder="Choose a username"
        autoComplete="username"
        fullWidth
        required
      />

      <Input
        label="Email"
        name="email"
        type="email"
        value={formData.email}
        onChange={handleChange}
        error={errors.email}
        placeholder="your.email@example.com"
        autoComplete="email"
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
        placeholder="Create a password"
        autoComplete="new-password"
        hint="At least 8 characters with uppercase, lowercase, and number"
        fullWidth
        required
      />

      <Input
        label="Confirm Password"
        name="confirmPassword"
        type="password"
        value={formData.confirmPassword}
        onChange={handleChange}
        error={errors.confirmPassword}
        placeholder="Confirm your password"
        autoComplete="new-password"
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
        Register
      </Button>

      <p className={styles.switchText}>
        Already have an account?{' '}
        <button
          type="button"
          className={styles.switchButton}
          onClick={onSwitchToLogin}
        >
          Log in here
        </button>
      </p>
    </form>
  );
}
