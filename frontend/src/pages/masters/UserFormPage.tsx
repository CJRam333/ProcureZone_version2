import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaUserCog } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { usersApi, employeesApi, getErrorMessage } from '../../api';
import type { UserCreateRequest, UserUpdateRequest, Employee } from '../../api';

interface FormData { username: string; password: string; confirmPassword: string; employeeId: number | null; status: number; }

const UserFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;
  const [watchPassword, setWatchPassword] = useState('');

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ defaultValues: { username: '', password: '', confirmPassword: '', employeeId: null, status: 1 } });

  const { data: user, isLoading: loadingUser } = useQuery({ queryKey: ['user', id], queryFn: () => usersApi.getById(Number(id)), enabled: isEditMode });
  const { data: employeesData } = useQuery({ queryKey: ['employees-available', user?.employeeId], queryFn: async () => employeesApi.getAvailableForUser(user?.employeeId, 0, 100) });
  const employees: Employee[] = employeesData?.content || [];

  useEffect(() => { if (user) reset({ username: user.username || '', password: '', confirmPassword: '', employeeId: user.employeeId || null, status: user.status ?? 1 }); }, [user, reset]);

  const createMutation = useMutation({
    mutationFn: (data: UserCreateRequest) => usersApi.create(data),
    onSuccess: () => { toast.success('User created successfully'); queryClient.invalidateQueries({ queryKey: ['users'] }); navigate('/masters/users'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: UserUpdateRequest) => usersApi.update(Number(id), data),
    onSuccess: () => { toast.success('User updated successfully'); queryClient.invalidateQueries({ queryKey: ['users'] }); navigate('/masters/users'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => {
    if (isEditMode) {
      const updatePayload: UserUpdateRequest = { username: data.username, status: data.status };
      if (data.password) updatePayload.password = data.password;
      updateMutation.mutate(updatePayload);
    } else {
      const createPayload: UserCreateRequest = { username: data.username, password: data.password, employeeId: data.employeeId!, status: data.status };
      createMutation.mutate(createPayload);
    }
  };

  if (isEditMode && loadingUser) return <LoadingSpinner text="Loading user..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit User' : 'Add User'} subtitle={isEditMode ? `Editing: ${user?.username}` : 'Create a new user account'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Users', path: '/masters/users' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaUserCog className="me-2 text-primary" /><span className="fw-bold">User Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)} autoComplete="off">
            <Row>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Username <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter username" autoComplete="off" {...register('username', { required: 'Username is required', minLength: { value: 3, message: 'Username must be at least 3 characters' } })} isInvalid={!!errors.username} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.username?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Employee {!isEditMode && <span className="text-danger">*</span>}</Form.Label><Form.Select {...register('employeeId', { required: !isEditMode ? 'Employee is required' : false, setValueAs: (v) => v ? Number(v) : null })} disabled={isEditMode}><option value="">Select Employee</option>{employees.map((e: Employee) => <option key={e.id} value={e.id}>{e.empName} ({e.empId})</option>)}</Form.Select>{errors.employeeId && <div className="invalid-feedback d-block">{errors.employeeId.message}</div>}</Form.Group></Col>
            </Row>
            <Row>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Password {!isEditMode && <span className="text-danger">*</span>}</Form.Label><Form.Control type="password" placeholder={isEditMode ? 'Leave blank to keep current' : 'Enter password'} autoComplete="new-password" {...register('password', { required: isEditMode ? false : 'Password is required', minLength: { value: 6, message: 'Password must be at least 6 characters' }, onChange: (e) => setWatchPassword(e.target.value) })} isInvalid={!!errors.password} /><Form.Control.Feedback type="invalid">{errors.password?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Confirm Password {!isEditMode && <span className="text-danger">*</span>}</Form.Label><Form.Control type="password" placeholder={isEditMode ? 'Leave blank to keep current' : 'Confirm password'} autoComplete="new-password" {...register('confirmPassword', { validate: (value) => !watchPassword || value === watchPassword || 'Passwords do not match' })} isInvalid={!!errors.confirmPassword} /><Form.Control.Feedback type="invalid">{errors.confirmPassword?.message}</Form.Control.Feedback></Form.Group></Col>
            </Row>
            <Row><Col md={6}><Form.Group className="mb-3"><Form.Label>Status</Form.Label><Form.Select {...register('status', { valueAsNumber: true })}><option value={1}>Active</option><option value={0}>Inactive</option></Form.Select></Form.Group></Col></Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/users')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} User</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default UserFormPage;
