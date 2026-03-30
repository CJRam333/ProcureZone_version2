import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaBox } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { materialsApi, getErrorMessage } from '../../api';
import type { MaterialCreateRequest, MaterialUpdateRequest } from '../../api';

// Form data matches backend CreateMaterialRequest
interface FormData { 
  code: string; 
  name: string; 
  description: string; 
  isActive: boolean; 
}

const MaterialFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ 
    defaultValues: { 
      code: '', 
      name: '', 
      description: '', 
      isActive: true 
    } 
  });

  const { data: material, isLoading } = useQuery({ queryKey: ['material', id], queryFn: () => materialsApi.getById(Number(id)), enabled: isEditMode });

  useEffect(() => { 
    if (material) reset({ 
      code: material.code || '', 
      name: material.name || '', 
      description: material.description || '', 
      isActive: material.status === 1 
    }); 
  }, [material, reset]);

  const createMutation = useMutation({
    mutationFn: (data: MaterialCreateRequest) => materialsApi.create(data),
    onSuccess: () => { toast.success('Material created successfully'); queryClient.invalidateQueries({ queryKey: ['materials'] }); navigate('/masters/materials'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: MaterialUpdateRequest) => materialsApi.update(Number(id), data),
    onSuccess: () => { toast.success('Material updated successfully'); queryClient.invalidateQueries({ queryKey: ['materials'] }); navigate('/masters/materials'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => {
    const payload: MaterialCreateRequest = {
      code: data.code.toUpperCase(),
      name: data.name,
      description: data.description || undefined,
      status: data.isActive ? 1 : 0,
    };
    if (isEditMode) {
      updateMutation.mutate(payload);
    } else {
      createMutation.mutate(payload);
    }
  };

  if (isEditMode && isLoading) return <LoadingSpinner text="Loading material..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit Material' : 'Add Material'} subtitle={isEditMode ? `Editing: ${material?.name}` : 'Create a new material'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Materials', path: '/masters/materials' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaBox className="me-2 text-primary" /><span className="fw-bold">Material Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Material Code <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="e.g., MAT-001" {...register('code', { required: 'Material code is required', pattern: { value: /^[A-Z0-9_-]+$/i, message: 'Code must be uppercase letters, numbers, underscores, and hyphens only' } })} isInvalid={!!errors.code} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.code?.message}</Form.Control.Feedback><Form.Text className="text-muted">Use uppercase letters, numbers, underscores, hyphens</Form.Text></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Name <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter material name" {...register('name', { required: 'Material name is required' })} isInvalid={!!errors.name} /><Form.Control.Feedback type="invalid">{errors.name?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Status</Form.Label><Form.Check type="switch" id="isActive" label="Active" {...register('isActive')} /></Form.Group></Col>
            </Row>
            <Row>
              <Col md={12}><Form.Group className="mb-3"><Form.Label>Description</Form.Label><Form.Control as="textarea" rows={3} placeholder="Enter material description" {...register('description')} /></Form.Group></Col>
            </Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/materials')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} Material</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default MaterialFormPage;
