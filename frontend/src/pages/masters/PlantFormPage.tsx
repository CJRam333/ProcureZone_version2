import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaIndustry } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { plantsApi, getErrorMessage } from '../../api';
import type { PlantCreateRequest, PlantUpdateRequest } from '../../api';

interface FormData {
  code: string;
  name: string;
  status: number;
}

const PlantFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    defaultValues: { code: '', name: '', status: 1 },
  });

  const { data: plant, isLoading } = useQuery({
    queryKey: ['plant', id],
    queryFn: () => plantsApi.getById(Number(id)),
    enabled: isEditMode,
  });

  useEffect(() => {
    if (plant) reset({ code: plant.code, name: plant.name, status: plant.status });
  }, [plant, reset]);

  const createMutation = useMutation({
    mutationFn: (data: PlantCreateRequest) => plantsApi.create(data),
    onSuccess: () => { toast.success('Plant created successfully'); queryClient.invalidateQueries({ queryKey: ['plants'] }); navigate('/masters/plants'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: PlantUpdateRequest) => plantsApi.update(Number(id), data),
    onSuccess: () => { toast.success('Plant updated successfully'); queryClient.invalidateQueries({ queryKey: ['plants'] }); navigate('/masters/plants'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => isEditMode ? updateMutation.mutate(data) : createMutation.mutate(data);

  if (isEditMode && isLoading) return <LoadingSpinner text="Loading plant..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit Plant' : 'Add Plant'} subtitle={isEditMode ? `Editing: ${plant?.name}` : 'Create a new plant'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Plants', path: '/masters/plants' }, { label: isEditMode ? 'Edit' : 'Add' }]} />

      <Card>
        <Card.Header><FaIndustry className="me-2 text-primary" /><span className="fw-bold">Plant Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Plant Code <span className="text-danger">*</span></Form.Label>
                  <Form.Control type="text" placeholder="Enter plant code" {...register('code', { required: 'Plant code is required', maxLength: { value: 100, message: 'Max 100 characters' } })} isInvalid={!!errors.code} disabled={isEditMode} />
                  <Form.Control.Feedback type="invalid">{errors.code?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Plant Name <span className="text-danger">*</span></Form.Label>
                  <Form.Control type="text" placeholder="Enter plant name" {...register('name', { required: 'Plant name is required', maxLength: { value: 100, message: 'Max 100 characters' } })} isInvalid={!!errors.name} />
                  <Form.Control.Feedback type="invalid">{errors.name?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Status <span className="text-danger">*</span></Form.Label>
                  <Form.Select {...register('status', { valueAsNumber: true })}><option value={1}>Active</option><option value={0}>Inactive</option></Form.Select>
                </Form.Group>
              </Col>
            </Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/plants')} disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update Plant' : 'Create Plant'}</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default PlantFormPage;
