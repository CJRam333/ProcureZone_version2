import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaRuler } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { uomApi, getErrorMessage } from '../../api';
import type { UOMCreateRequest, UOMUpdateRequest } from '../../api';

interface FormData { code: string; name: string; status: number; }

const UOMFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ defaultValues: { code: '', name: '', status: 1 } });

  const { data: uom, isLoading } = useQuery({ queryKey: ['uom', id], queryFn: () => uomApi.getById(Number(id)), enabled: isEditMode });

  useEffect(() => { if (uom) reset({ code: uom.code, name: uom.name, status: uom.status }); }, [uom, reset]);

  const createMutation = useMutation({
    mutationFn: (data: UOMCreateRequest) => uomApi.create(data),
    onSuccess: () => { toast.success('UOM created successfully'); queryClient.invalidateQueries({ queryKey: ['uom'] }); navigate('/masters/uom'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: UOMUpdateRequest) => uomApi.update(Number(id), data),
    onSuccess: () => { toast.success('UOM updated successfully'); queryClient.invalidateQueries({ queryKey: ['uom'] }); navigate('/masters/uom'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => isEditMode ? updateMutation.mutate(data) : createMutation.mutate(data);

  if (isEditMode && isLoading) return <LoadingSpinner text="Loading UOM..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit UOM' : 'Add UOM'} subtitle={isEditMode ? `Editing: ${uom?.name}` : 'Create a new unit of measure'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'UOM', path: '/masters/uom' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaRuler className="me-2 text-primary" /><span className="fw-bold">UOM Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>UOM Code <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter UOM code (e.g., KG, LTR)" {...register('code', { required: 'UOM code is required' })} isInvalid={!!errors.code} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.code?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>UOM Name <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter UOM name (e.g., Kilogram, Liter)" {...register('name', { required: 'UOM name is required' })} isInvalid={!!errors.name} /><Form.Control.Feedback type="invalid">{errors.name?.message}</Form.Control.Feedback></Form.Group></Col>
            </Row>
            <Row><Col md={6}><Form.Group className="mb-3"><Form.Label>Status</Form.Label><Form.Select {...register('status', { valueAsNumber: true })}><option value={1}>Active</option><option value={0}>Inactive</option></Form.Select></Form.Group></Col></Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/uom')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} UOM</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default UOMFormPage;
