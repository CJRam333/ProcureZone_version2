import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaBuilding } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { companiesApi, getErrorMessage } from '../../api';
import type { CompanyCreateRequest, CompanyUpdateRequest } from '../../api';

interface FormData {
  code: string;
  name: string;
  status: number;
}

const CompanyFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    defaultValues: {
      code: '',
      name: '',
      status: 1,
    },
  });

  // Fetch company for edit mode
  const { data: company, isLoading } = useQuery({
    queryKey: ['company', id],
    queryFn: () => companiesApi.getById(Number(id)),
    enabled: isEditMode,
  });

  // Load company data into form
  useEffect(() => {
    if (company) {
      reset({
        code: company.code,
        name: company.name,
        status: company.status,
      });
    }
  }, [company, reset]);

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: CompanyCreateRequest) => companiesApi.create(data),
    onSuccess: () => {
      toast.success('Company created successfully');
      queryClient.invalidateQueries({ queryKey: ['companies'] });
      navigate('/masters/companies');
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: (data: CompanyUpdateRequest) => companiesApi.update(Number(id), data),
    onSuccess: () => {
      toast.success('Company updated successfully');
      queryClient.invalidateQueries({ queryKey: ['companies'] });
      queryClient.invalidateQueries({ queryKey: ['company', id] });
      navigate('/masters/companies');
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });

  const onSubmit = (data: FormData) => {
    if (isEditMode) {
      updateMutation.mutate(data);
    } else {
      createMutation.mutate(data);
    }
  };

  if (isEditMode && isLoading) {
    return <LoadingSpinner text="Loading company..." />;
  }

  return (
    <div>
      <PageHeader
        title={isEditMode ? 'Edit Company' : 'Add Company'}
        subtitle={isEditMode ? `Editing: ${company?.name}` : 'Create a new company'}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Masters', path: '/masters' },
          { label: 'Companies', path: '/masters/companies' },
          { label: isEditMode ? 'Edit' : 'Add' },
        ]}
      />

      <Card>
        <Card.Header>
          <div className="d-flex align-items-center">
            <FaBuilding className="me-2 text-primary" />
            <span className="fw-bold">Company Details</span>
          </div>
        </Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Company Code <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="Enter company code"
                    {...register('code', {
                      required: 'Company code is required',
                      maxLength: { value: 100, message: 'Max 100 characters' },
                    })}
                    isInvalid={!!errors.code}
                    disabled={isEditMode}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.code?.message}
                  </Form.Control.Feedback>
                  <Form.Text className="text-muted">
                    Unique identifier for the company (cannot be changed after creation)
                  </Form.Text>
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Company Name <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="Enter company name"
                    {...register('name', {
                      required: 'Company name is required',
                      maxLength: { value: 100, message: 'Max 100 characters' },
                    })}
                    isInvalid={!!errors.name}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.name?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Status <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    {...register('status', { valueAsNumber: true })}
                    isInvalid={!!errors.status}
                  >
                    <option value={1}>Active</option>
                    <option value={0}>Inactive</option>
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">
                    {errors.status?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>

            <hr />

            <div className="d-flex justify-content-end gap-2">
              <Button
                variant="outline-secondary"
                onClick={() => navigate('/masters/companies')}
                disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}
              >
                <FaTimes className="me-2" />
                Cancel
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}
              >
                <FaSave className="me-2" />
                {isEditMode ? 'Update Company' : 'Create Company'}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default CompanyFormPage;
