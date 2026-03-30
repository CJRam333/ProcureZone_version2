import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaSitemap } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { departmentsApi, getErrorMessage } from '../../api';
import type { DepartmentCreateRequest, DepartmentUpdateRequest } from '../../api';

interface FormData { code: string; name: string; status: number; }

const DepartmentFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ defaultValues: { code: '', name: '', status: 1 } });

  const { data: department, isLoading } = useQuery({ queryKey: ['department', id], queryFn: () => departmentsApi.getById(Number(id)), enabled: isEditMode });

  useEffect(() => { if (department) reset({ code: department.code, name: department.name, status: department.status }); }, [department, reset]);

  const createMutation = useMutation({
    mutationFn: (data: DepartmentCreateRequest) => departmentsApi.create(data),
    onSuccess: () => { toast.success('Department created successfully'); queryClient.invalidateQueries({ queryKey: ['departments'] }); navigate('/masters/departments'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: DepartmentUpdateRequest) => departmentsApi.update(Number(id), data),
    onSuccess: () => { toast.success('Department updated successfully'); queryClient.invalidateQueries({ queryKey: ['departments'] }); navigate('/masters/departments'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => isEditMode ? updateMutation.mutate(data) : createMutation.mutate(data);

  if (isEditMode && isLoading) return <LoadingSpinner text="Loading department..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit Department' : 'Add Department'} subtitle={isEditMode ? `Editing: ${department?.name}` : 'Create a new department'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Departments', path: '/masters/departments' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaSitemap className="me-2 text-primary" /><span className="fw-bold">Department Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Department Code <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter department code" {...register('code', { required: 'Department code is required' })} isInvalid={!!errors.code} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.code?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Department Name <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter department name" {...register('name', { required: 'Department name is required' })} isInvalid={!!errors.name} /><Form.Control.Feedback type="invalid">{errors.name?.message}</Form.Control.Feedback></Form.Group></Col>
            </Row>
            <Row><Col md={6}><Form.Group className="mb-3"><Form.Label>Status</Form.Label><Form.Select {...register('status', { valueAsNumber: true })}><option value={1}>Active</option><option value={0}>Inactive</option></Form.Select></Form.Group></Col></Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/departments')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} Department</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default DepartmentFormPage;
