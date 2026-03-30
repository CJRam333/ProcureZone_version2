import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col, Tab, Tabs } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaTruck } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { vendorsApi, getErrorMessage } from '../../api';
import type { VendorCreateRequest } from '../../api';

interface FormData { vendorCode: string; vendorName: string; contactPerson: string; email: string; phone: string; mobile: string; address: string; city: string; state: string; pincode: string; country: string; gstNumber: string; panNumber: string; bankName: string; bankBranch: string; accountNumber: string; ifscCode: string; paymentTerms: string; creditDays: number | null; creditLimit: number | null; }

const VendorFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({ defaultValues: { vendorCode: '', vendorName: '', contactPerson: '', email: '', phone: '', mobile: '', address: '', city: '', state: '', pincode: '', country: 'India', gstNumber: '', panNumber: '', bankName: '', bankBranch: '', accountNumber: '', ifscCode: '', paymentTerms: '', creditDays: null, creditLimit: null } });

  const { data: vendor, isLoading } = useQuery({ queryKey: ['vendor', id], queryFn: () => vendorsApi.getById(Number(id)), enabled: isEditMode });

  useEffect(() => { 
    if (vendor) reset({ 
      vendorCode: vendor.vendorCode || '', vendorName: vendor.vendorName || '', 
      contactPerson: vendor.contactPerson || '', email: vendor.email || '', phone: vendor.phone || '', mobile: vendor.mobile || '', 
      address: vendor.address || '', city: vendor.city || '', state: vendor.state || '', pincode: vendor.pincode || '', country: vendor.country || 'India',
      gstNumber: vendor.gstNumber || '', panNumber: vendor.panNumber || '', 
      bankName: vendor.bankName || '', bankBranch: vendor.bankBranch || '', accountNumber: vendor.accountNumber || '', ifscCode: vendor.ifscCode || '',
      paymentTerms: vendor.paymentTerms || '', creditDays: vendor.creditDays, creditLimit: vendor.creditLimit
    }); 
  }, [vendor, reset]);

  const createMutation = useMutation({
    mutationFn: (data: VendorCreateRequest) => vendorsApi.create(data),
    onSuccess: () => { toast.success('Vendor created successfully'); queryClient.invalidateQueries({ queryKey: ['vendors'] }); navigate('/masters/vendors'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: Partial<VendorCreateRequest>) => vendorsApi.update(Number(id), data),
    onSuccess: () => { toast.success('Vendor updated successfully'); queryClient.invalidateQueries({ queryKey: ['vendors'] }); navigate('/masters/vendors'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => {
    const payload = { ...data, creditDays: data.creditDays || undefined, creditLimit: data.creditLimit || undefined };
    isEditMode ? updateMutation.mutate(payload) : createMutation.mutate(payload);
  };

  if (isEditMode && isLoading) return <LoadingSpinner text="Loading vendor..." />;

  return (
    <div>
      <PageHeader title={isEditMode ? 'Edit Vendor' : 'Add Vendor'} subtitle={isEditMode ? `Editing: ${vendor?.vendorName}` : 'Create a new vendor'} breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Vendors', path: '/masters/vendors' }, { label: isEditMode ? 'Edit' : 'Add' }]} />
      <Card>
        <Card.Header><FaTruck className="me-2 text-primary" /><span className="fw-bold">Vendor Details</span></Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Tabs defaultActiveKey="basic" className="mb-3">
              <Tab eventKey="basic" title="Basic Info">
                <Row>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Vendor Code <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="e.g., VND001" {...register('vendorCode', { required: 'Vendor code is required' })} isInvalid={!!errors.vendorCode} disabled={isEditMode} /><Form.Control.Feedback type="invalid">{errors.vendorCode?.message}</Form.Control.Feedback></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Vendor Name <span className="text-danger">*</span></Form.Label><Form.Control type="text" placeholder="Enter vendor name" {...register('vendorName', { required: 'Vendor name is required' })} isInvalid={!!errors.vendorName} /><Form.Control.Feedback type="invalid">{errors.vendorName?.message}</Form.Control.Feedback></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Contact Person</Form.Label><Form.Control type="text" placeholder="Contact person name" {...register('contactPerson')} /></Form.Group></Col>
                </Row>
                <Row>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Email</Form.Label><Form.Control type="email" placeholder="Email address" {...register('email')} /></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Phone</Form.Label><Form.Control type="text" placeholder="Phone number" {...register('phone')} /></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Mobile</Form.Label><Form.Control type="text" placeholder="Mobile number" {...register('mobile')} /></Form.Group></Col>
                </Row>
              </Tab>
              <Tab eventKey="address" title="Address">
                <Row>
                  <Col md={12}><Form.Group className="mb-3"><Form.Label>Address</Form.Label><Form.Control as="textarea" rows={2} placeholder="Full address" {...register('address')} /></Form.Group></Col>
                </Row>
                <Row>
                  <Col md={3}><Form.Group className="mb-3"><Form.Label>City</Form.Label><Form.Control type="text" placeholder="City" {...register('city')} /></Form.Group></Col>
                  <Col md={3}><Form.Group className="mb-3"><Form.Label>State</Form.Label><Form.Control type="text" placeholder="State" {...register('state')} /></Form.Group></Col>
                  <Col md={3}><Form.Group className="mb-3"><Form.Label>Country</Form.Label><Form.Control type="text" placeholder="Country" {...register('country')} /></Form.Group></Col>
                  <Col md={3}><Form.Group className="mb-3"><Form.Label>PIN Code</Form.Label><Form.Control type="text" placeholder="PIN Code" {...register('pincode')} /></Form.Group></Col>
                </Row>
              </Tab>
              <Tab eventKey="tax" title="Tax & Payment">
                <Row>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>GST Number</Form.Label><Form.Control type="text" placeholder="e.g., 27AABCU9603R1ZM" {...register('gstNumber')} /></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>PAN Number</Form.Label><Form.Control type="text" placeholder="e.g., AABCU9603R" {...register('panNumber')} /></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Payment Terms</Form.Label><Form.Control type="text" placeholder="e.g., Net 30" {...register('paymentTerms')} /></Form.Group></Col>
                </Row>
                <Row>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Credit Days</Form.Label><Form.Control type="number" placeholder="0" {...register('creditDays', { valueAsNumber: true })} /></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Credit Limit</Form.Label><Form.Control type="number" placeholder="0" {...register('creditLimit', { valueAsNumber: true })} /></Form.Group></Col>
                </Row>
              </Tab>
              <Tab eventKey="bank" title="Bank Details">
                <Row>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Bank Name</Form.Label><Form.Control type="text" placeholder="Bank name" {...register('bankName')} /></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Branch</Form.Label><Form.Control type="text" placeholder="Branch name" {...register('bankBranch')} /></Form.Group></Col>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>Account Number</Form.Label><Form.Control type="text" placeholder="Account number" {...register('accountNumber')} /></Form.Group></Col>
                </Row>
                <Row>
                  <Col md={4}><Form.Group className="mb-3"><Form.Label>IFSC Code</Form.Label><Form.Control type="text" placeholder="IFSC code" {...register('ifscCode')} /></Form.Group></Col>
                </Row>
              </Tab>
            </Tabs>
            <hr />
            <div className="d-flex justify-content-end gap-2">
              <Button variant="outline-secondary" onClick={() => navigate('/masters/vendors')}><FaTimes className="me-2" />Cancel</Button>
              <Button type="submit" variant="primary" disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}><FaSave className="me-2" />{isEditMode ? 'Update' : 'Create'} Vendor</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default VendorFormPage;
