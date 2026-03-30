import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaLayerGroup } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { sectionsApi, getErrorMessage } from '../../api';
import type { SectionCreateRequest, SectionUpdateRequest } from '../../api';

interface FormData { code: string; name: string; status: number; }

const SectionFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ defaultValues: { code: '', name: '', status: 1 } });

  const { data: section, isLoading } = useQuery({ queryKey: ['section', id], queryFn: () => sectionsApi.getById(Number(id)), enabled: isEditMode });

  useEffect(() => { if (section) reset({ code: section.code, name: section.name, status: section.status }); }, [section, reset]);

  const createMutation = useMutation({
    mutationFn: (data: SectionCreateRequest) => sectionsApi.create(data),
    onSuccess: () => { toast.success('Section created successfully'); queryClient.invalidateQueries({ queryKey: ['sections'] }); navigate('/masters/sections'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: SectionUpdateRequest) => sectionsApi.update(Number(id), data),
    onSuccess: () => { toast.success('Section updated successfully'); queryClient.invalidateQueries({ queryKey: ['sections'] }); navigate('/masters/sections'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => isEditMode ? updateMutation.mutate(data) : createMutation.mutate(data);

  if (isEditMode && isLoading) return <LoadingSpinner text="Loading section..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit Section' : 'Add Section'} subtitle={isEditMode ? `Editing: ${section?.name}` : 'Create a new section'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Sections', path: '/masters/sections' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaLayerGroup className="me-2 text-primary" /><span className="fw-bold">Section Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Section Code <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter section code" {...register('code', { required: 'Section code is required' })} isInvalid={!!errors.code} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.code?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Section Name <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter section name" {...register('name', { required: 'Section name is required' })} isInvalid={!!errors.name} /><Form.Control.Feedback type="invalid">{errors.name?.message}</Form.Control.Feedback></Form.Group></Col>
            </Row>
            <Row><Col md={6}><Form.Group className="mb-3"><Form.Label>Status</Form.Label><Form.Select {...register('status', { valueAsNumber: true })}><option value={1}>Active</option><option value={0}>Inactive</option></Form.Select></Form.Group></Col></Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/sections')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} Section</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default SectionFormPage;
