import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaMapMarkerAlt } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { locationsApi, getErrorMessage } from '../../api';
import type { LocationCreateRequest, LocationUpdateRequest } from '../../api';

interface FormData { code: string; name: string; status: number; }

const LocationFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ defaultValues: { code: '', name: '', status: 1 } });

  const { data: location, isLoading } = useQuery({ queryKey: ['location', id], queryFn: () => locationsApi.getById(Number(id)), enabled: isEditMode });

  useEffect(() => { if (location) reset({ code: location.code, name: location.name, status: location.status }); }, [location, reset]);

  const createMutation = useMutation({
    mutationFn: (data: LocationCreateRequest) => locationsApi.create(data),
    onSuccess: () => { toast.success('Location created successfully'); queryClient.invalidateQueries({ queryKey: ['locations'] }); navigate('/masters/locations'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: LocationUpdateRequest) => locationsApi.update(Number(id), data),
    onSuccess: () => { toast.success('Location updated successfully'); queryClient.invalidateQueries({ queryKey: ['locations'] }); navigate('/masters/locations'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => isEditMode ? updateMutation.mutate(data) : createMutation.mutate(data);

  if (isEditMode && isLoading) return <LoadingSpinner text="Loading location..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit Location' : 'Add Location'} subtitle={isEditMode ? `Editing: ${location?.name}` : 'Create a new location'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Locations', path: '/masters/locations' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaMapMarkerAlt className="me-2 text-primary" /><span className="fw-bold">Location Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Location Code <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter location code" {...register('code', { required: 'Location code is required' })} isInvalid={!!errors.code} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.code?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3"><Form.Label>Location Name <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter location name" {...register('name', { required: 'Location name is required' })} isInvalid={!!errors.name} /><Form.Control.Feedback type="invalid">{errors.name?.message}</Form.Control.Feedback></Form.Group></Col>
            </Row>
            <Row><Col md={6}><Form.Group className="mb-3"><Form.Label>Status</Form.Label><Form.Select {...register('status', { valueAsNumber: true })}><option value={1}>Active</option><option value={0}>Inactive</option></Form.Select></Form.Group></Col></Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/locations')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} Location</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default LocationFormPage;
