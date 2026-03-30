import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaUser } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { employeesApi, departmentsApi, locationsApi, companiesApi, getErrorMessage } from '../../api';
import type { EmployeeCreateRequest, EmployeeUpdateRequest, Department, Location, Company } from '../../api';

interface FormData { empId: string; empName: string; empEmail: string; empDesignation: string; empCostCenter: string; departmentId: number | null; locationId: number | null; companyId: number | null; plantName: string; empStatus: number; empJoinDate: string; }

const EmployeeFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ defaultValues: { empId: '', empName: '', empEmail: '', empDesignation: '', empCostCenter: '', departmentId: null, locationId: null, companyId: null, plantName: '', empStatus: 1, empJoinDate: '' } });

  const { data: employee, isLoading: loadingEmployee } = useQuery({ queryKey: ['employee', id], queryFn: () => employeesApi.getById(Number(id)), enabled: isEditMode });
  const { data: departmentsData } = useQuery({ queryKey: ['departments-active'], queryFn: () => departmentsApi.getActive(0, 100) });
  const { data: locationsData } = useQuery({ queryKey: ['locations-active'], queryFn: () => locationsApi.getActive(0, 100) });
  const { data: companiesData } = useQuery({ queryKey: ['companies-active'], queryFn: () => companiesApi.getActive(0, 100) });
  
  const departments = departmentsData?.content ?? [];
  const locations = locationsData?.content ?? [];
  const companies = companiesData?.content ?? [];

  useEffect(() => { 
    if (employee) reset({ 
      empId: employee.empId || '', 
      empName: employee.empName || '', 
      empEmail: employee.empEmail || '', 
      empDesignation: employee.empDesignation || '', 
      empCostCenter: employee.empCostCenter || '', 
      departmentId: employee.departmentId || null, 
      locationId: employee.locationId || null, 
      companyId: employee.companyId || null, 
      plantName: employee.plantName || '', 
      empStatus: employee.empStatus ?? 1, 
      empJoinDate: employee.empJoinDate ? new Date(employee.empJoinDate).toISOString().split('T')[0] : '' 
    }); 
  }, [employee, reset]);

  const createMutation = useMutation({
    mutationFn: (data: EmployeeCreateRequest) => employeesApi.create(data),
    onSuccess: () => { toast.success('Employee created successfully'); queryClient.invalidateQueries({ queryKey: ['employees'] }); navigate('/masters/employees'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: EmployeeUpdateRequest) => employeesApi.update(Number(id), data),
    onSuccess: () => { toast.success('Employee updated successfully'); queryClient.invalidateQueries({ queryKey: ['employees'] }); navigate('/masters/employees'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => {
    const payload: EmployeeCreateRequest = { 
      empId: data.empId, 
      empName: data.empName, 
      empEmail: data.empEmail, 
      empDesignation: data.empDesignation || undefined, 
      empCostCenter: data.empCostCenter || undefined, 
      departmentId: data.departmentId || undefined, 
      locationId: data.locationId || undefined, 
      companyId: data.companyId || undefined,
      plantName: data.plantName || undefined,
      empStatus: data.empStatus,
      empJoinDate: data.empJoinDate || undefined
    };
    isEditMode ? updateMutation.mutate(payload) : createMutation.mutate(payload);
  };

  if (isEditMode && loadingEmployee) return <LoadingSpinner text="Loading employee..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit Employee' : 'Add Employee'} subtitle={isEditMode ? `Editing: ${employee?.empName}` : 'Create a new employee'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Employees', path: '/masters/employees' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaUser className="me-2 text-primary" /><span className="fw-bold">Employee Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Row>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Employee ID <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="e.g., EMP001" {...register('empId', { required: 'Employee ID is required' })} isInvalid={!!errors.empId} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.empId?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Full Name <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter full name" {...register('empName', { required: 'Name is required' })} isInvalid={!!errors.empName} /><Form.Control.Feedback type="invalid">{errors.empName?.message}</Form.Control.Feedback></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Email <span className="text-danger">*</span></Form.Label><Form.Control type="email" placeholder="email@example.com" {...register('empEmail', { required: 'Email is required', pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Invalid email format' } })} isInvalid={!!errors.empEmail} /><Form.Control.Feedback type="invalid">{errors.empEmail?.message}</Form.Control.Feedback></Form.Group></Col>
            </Row>
            <Row>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Designation</Form.Label><Form.Control type="text" placeholder="e.g., Manager" {...register('empDesignation')} /></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Cost Center</Form.Label><Form.Control type="text" placeholder="e.g., CC001" {...register('empCostCenter')} /></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Join Date</Form.Label><Form.Control type="date" {...register('empJoinDate')} /></Form.Group></Col>
            </Row>
            <Row>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Company</Form.Label><Form.Select {...register('companyId', { setValueAs: (v) => v ? Number(v) : null })}><option value="">Select Company</option>{companies.map((c: Company) => <option key={c.id} value={c.id}>{c.name}</option>)}</Form.Select></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Department</Form.Label><Form.Select {...register('departmentId', { setValueAs: (v) => v ? Number(v) : null })}><option value="">Select Department</option>{departments.map((d: Department) => <option key={d.id} value={d.id}>{d.name}</option>)}</Form.Select></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Location</Form.Label><Form.Select {...register('locationId', { setValueAs: (v) => v ? Number(v) : null })}><option value="">Select Location</option>{locations.map((l: Location) => <option key={l.id} value={l.id}>{l.name}</option>)}</Form.Select></Form.Group></Col>
            </Row>
            <Row>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Plant</Form.Label><Form.Control type="text" placeholder="Plant code" {...register('plantName')} /></Form.Group></Col>
              <Col md={4}><Form.Group className="mb-3"><Form.Label>Status</Form.Label><Form.Select {...register('empStatus', { valueAsNumber: true })}><option value={1}>Active</option><option value={0}>Inactive</option></Form.Select></Form.Group></Col>
            </Row>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/employees')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} Employee</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default EmployeeFormPage;
