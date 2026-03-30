import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Form, Button, Alert, Spinner } from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '../../contexts/AuthContext';
import { getErrorMessage } from '../../api';
import { FaBox, FaUser, FaLock } from 'react-icons/fa';

const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormData = z.infer<typeof loginSchema>;

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setError(null);
    setIsLoading(true);

    try {
      await login(data);
      navigate(from, { replace: true });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-header text-center mb-4">
          <img
            src="/logo-full.png"
            alt="ProcureZone Logo"
            className="img-fluid"
            style={{ maxHeight: '70px' }}
          />
        </div>

        {error && (
          <Alert variant="danger" dismissible onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <Form onSubmit={handleSubmit(onSubmit)}>
          <Form.Group className="mb-3">
            <Form.Label>
              <FaUser className="me-2" style={{ opacity: 0.6 }} />
              Username
            </Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter your username"
              autoComplete="username"
              {...register('username')}
              isInvalid={!!errors.username}
              disabled={isLoading}
              autoFocus
            />
            <Form.Control.Feedback type="invalid">
              {errors.username?.message}
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-4">
            <Form.Label>
              <FaLock className="me-2" style={{ opacity: 0.6 }} />
              Password
            </Form.Label>
            <Form.Control
              type="password"
              placeholder="Enter your password"
              autoComplete="current-password"
              {...register('password')}
              isInvalid={!!errors.password}
              disabled={isLoading}
            />
            <Form.Control.Feedback type="invalid">
              {errors.password?.message}
            </Form.Control.Feedback>
          </Form.Group>

          <Button
            type="submit"
            variant="primary"
            className="w-100"
            size="lg"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <Spinner
                  as="span"
                  animation="border"
                  size="sm"
                  role="status"
                  className="me-2"
                />
                Signing in...
              </>
            ) : (
              'Sign In'
            )}
          </Button>
        </Form>
      </div>
    </div>
  );
};

export default LoginPage;
