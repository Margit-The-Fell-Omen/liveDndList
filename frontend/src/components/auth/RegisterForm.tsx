import {type ChangeEvent, type FormEvent, useState} from 'react';
import {useAuth} from '@/context/AuthContext';
import {Input} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import {validate, validators} from '@/utils/validators';
import styles from './AuthForm.module.css';

interface RegisterFormProps {
  onSwitchToLogin: () => void;
}

interface FormData {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

interface FormErrors {
  username: string | null;
  email: string | null;
  password: string | null;
  confirmPassword: string | null;
}

export function RegisterForm({onSwitchToLogin}: RegisterFormProps) {
  const {register, error: authError, clearError} = useAuth();
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<FormData>({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState<FormErrors>({
    username: null,
    email: null,
    password: null,
    confirmPassword: null,
  });

  const handleChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const {name, value} = e.target;
    setFormData((prev) => ({...prev, [name]: value}));

    if (errors[name as keyof FormErrors]) {
      setErrors((prev) => ({...prev, [name]: null}));
    }
    if (authError) {
      clearError();
    }
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {
      username: validate(formData.username, validators.required, validators.username),
      email: validate(formData.email, validators.required, validators.email),
      password: validate(formData.password, validators.required, validators.password),
      confirmPassword: validate(
          formData.confirmPassword,
          validators.required,
          validators.match('password', () => formData.password)
      ),
    };

    setErrors(newErrors);
    return !Object.values(newErrors).some((error) => error !== null);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();

    if (!validateForm()) return;

    setLoading(true);
    try {
      await register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
      });
      // Redirect handled by App.tsx
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

        {authError && <div className={styles.errorBanner}>{authError}</div>}

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

        <Button type="submit" variant="primary" size="large" fullWidth>
          Register
        </Button>

        <p className={styles.switchText}>
          Already have an account?{' '}
          <button type="button" className={styles.switchButton} onClick={onSwitchToLogin}>
            Log in here
          </button>
        </p>
      </form>
  );
}
