import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Form,
  Button,
  Card,
  Row,
  Col,
  Alert,
  Spinner,
} from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaSave, FaArrowLeft, FaTimes } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { materialsApi, getErrorMessage } from '../../api';
import type { MaterialCreateRequest } from '../../api';

interface MaterialFormData {
  code: string;
  name: string;
  description: string;
  isActive: boolean;
}

const MaterialFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEdit = !!id;

  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<MaterialFormData>({
    defaultValues: {
      code: '',
      name: '',
      description: '',
      isActive: true,
    },
  });

  // Fetch existing material for edit mode
  const { data: existingMaterial, isLoading: loadingMaterial } = useQuery({
    queryKey: ['material', id],
    queryFn: () => materialsApi.getById(Number(id)),
    enabled: isEdit,
  });

  // Populate form with existing data
  React.useEffect(() => {
    if (existingMaterial) {
      reset({
        code: existingMaterial.code,
        name: existingMaterial.name,
        description: existingMaterial.description || '',
        isActive: existingMaterial.status === 1,
      });
    }
  }, [existingMaterial, reset]);

  // Create/Update mutation
  const saveMutation = useMutation({
    mutationFn: (data: MaterialFormData) => {
      const payload: MaterialCreateRequest = {
        code: data.code.toUpperCase(),
        name: data.name,
        description: data.description || undefined,
        status: data.isActive ? 1 : 0,
      };
      if (isEdit) {
        return materialsApi.update(Number(id), payload);
      }
      return materialsApi.create(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['materials'] });
      navigate('/materials');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const onSubmit = async (data: MaterialFormData) => {
    setError(null);
    saveMutation.mutate(data);
  };

  if (isEdit && loadingMaterial) {
    return <LoadingSpinner fullPage text="Loading material..." />;
  }

  return (
    <div>
      <PageHeader
        title={isEdit ? 'Edit Material' : 'Create Material'}
        subtitle={isEdit ? `Editing ${existingMaterial?.code || existingMaterial?.name}` : 'Add a new material to the master data'}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Materials', path: '/materials' },
          { label: isEdit ? 'Edit' : 'Create' },
        ]}
        actions={
          <Button variant="outline-secondary" onClick={() => navigate('/materials')}>
            <FaArrowLeft className="me-2" /> Back
          </Button>
        }
      />

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Form onSubmit={handleSubmit(onSubmit)}>
        <Row>
          <Col lg={8}>
            {/* Basic Information */}
            <Card className="mb-4">
              <Card.Header>
                <h5 className="mb-0">Basic Information</h5>
              </Card.Header>
              <Card.Body>
                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Material Code <span className="text-danger">*</span></Form.Label>
                      <Form.Control
                        type="text"
                        {...register('code', { 
                          required: 'Material code is required',
                          pattern: {
                            value: /^[A-Z0-9_-]+$/i,
                            message: 'Code must be uppercase letters, numbers, underscores, and hyphens only'
                          }
                        })}
                        isInvalid={!!errors.code}
                        disabled={isEdit}
                        placeholder="e.g., MAT-001"
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.code?.message}
                      </Form.Control.Feedback>
                      <Form.Text className="text-muted">
                        Uppercase letters, numbers, underscores, and hyphens only
                      </Form.Text>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Name <span className="text-danger">*</span></Form.Label>
                      <Form.Control
                        type="text"
                        {...register('name', { required: 'Material name is required' })}
                        isInvalid={!!errors.name}
                        placeholder="e.g., Copper Wire 2.5mm"
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.name?.message}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col xs={12}>
                    <Form.Group className="mb-3">
                      <Form.Label>Description</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={3}
                        {...register('description')}
                        placeholder="Enter material description"
                      />
                    </Form.Group>
                  </Col>
                </Row>
              </Card.Body>
            </Card>
          </Col>

          <Col lg={4}>
            {/* Status Card */}
            <Card className="mb-4">
              <Card.Header>
                <h5 className="mb-0">Status</h5>
              </Card.Header>
              <Card.Body>
                <Form.Check
                  type="switch"
                  id="isActive"
                  label="Active"
                  {...register('isActive')}
                />
                <Form.Text className="text-muted">
                  Inactive materials won't appear in selection lists
                </Form.Text>
              </Card.Body>
            </Card>

            {/* Actions Card */}
            <Card>
              <Card.Body>
                <div className="d-grid gap-2">
                  <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    disabled={isSubmitting || saveMutation.isPending}
                  >
                    {(isSubmitting || saveMutation.isPending) ? (
                      <>
                        <Spinner as="span" animation="border" size="sm" className="me-2" />
                        Saving...
                      </>
                    ) : (
                      <>
                        <FaSave className="me-2" />
                        {isEdit ? 'Update Material' : 'Create Material'}
                      </>
                    )}
                  </Button>
                  <Button
                    type="button"
                    variant="outline-secondary"
                    onClick={() => navigate('/materials')}
                  >
                    <FaTimes className="me-2" /> Cancel
                  </Button>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Form>
    </div>
  );
};

export default MaterialFormPage;
