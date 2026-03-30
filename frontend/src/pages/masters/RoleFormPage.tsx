import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaUserShield } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { rolesApi, getErrorMessage } from '../../api';
import type { RoleCreateRequest, RoleUpdateRequest } from '../../api';

interface FormData { code: string; name: string; canView: boolean; canAdd: boolean; canEdit: boolean; canDelete: boolean; status: number; }

const RoleFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ defaultValues: { code: '', name: '', canView: true, canAdd: false, canEdit: false, canDelete: false, status: 1 } });

  const { data: role, isLoading } = useQuery({ queryKey: ['role', id], queryFn: () => rolesApi.getById(Number(id)), enabled: isEditMode });

  useEffect(() => { if (role) reset({ code: role.code || '', name: role.name || '', canView: role.canView || false, canAdd: role.canAdd || false, canEdit: role.canEdit || false, canDelete: role.canDelete || false, status: role.status ?? 1 }); }, [role, reset]);

  const createMutation = useMutation({
    mutationFn: (data: RoleCreateRequest) => rolesApi.create(data),
    onSuccess: () => { toast.success('Role created successfully'); queryClient.invalidateQueries({ queryKey: ['roles'] }); navigate('/masters/roles'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: RoleUpdateRequest) => rolesApi.update(Number(id), data),
    onSuccess: () => { toast.success('Role updated successfully'); queryClient.invalidateQueries({ queryKey: ['roles'] }); navigate('/masters/roles'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => isEditMode ? updateMutation.mutate(data) : createMutation.mutate(data);

  if (isEditMode && isLoading) return <LoadingSpinner text="Loading role..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit Role' : 'Add Role'} subtitle={isEditMode ? `Editing: ${role?.name}` : 'Create a new role'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Roles', path: '/masters/roles' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaUserShield className="me-2 text-primary" /><span className="fw-bold">Role Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Role Code <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="e.g., ADMIN, MANAGER" {...register('code', { required: 'Role code is required' })} isInvalid={!!errors.code} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.code?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Role Name <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="e.g., Administrator, Manager" {...register('name', { required: 'Role name is required' })} isInvalid={!!errors.name} /><Form.Control.Feedback type="invalid">{errors.name?.message}</Form.Control.Feedback></Form.Group></Col>
            </Row>
            <Row><Col md={6}><Form.Group className="mb-3"><Form.Label>Status</Form.Label><Form.Select {...register('status', { valueAsNumber: true })}><option value={1}>Active</option><option value={0}>Inactive</option></Form.Select></Form.Group></Col></Row>
            <hr />
            <h6 className="mb-3">Permissions</h6>
            <Row>
              <Col md={3}><Form.Check type="checkbox" id="canView" label="Can View" {...register('canView')} className="mb-3" /></Col>
              <Col md={3}><Form.Check type="checkbox" id="canAdd" label="Can Add" {...register('canAdd')} className="mb-3" /></Col>
              <Col md={3}><Form.Check type="checkbox" id="canEdit" label="Can Edit" {...register('canEdit')} className="mb-3" /></Col>
              <Col md={3}><Form.Check type="checkbox" id="canDelete" label="Can Delete" {...register('canDelete')} className="mb-3" /></Col>
            </Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/roles')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} Role</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default RoleFormPage;
