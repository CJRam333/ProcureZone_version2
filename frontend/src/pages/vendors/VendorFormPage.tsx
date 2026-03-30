import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Form,
  Button,
  Card,
  Row,
  Col,
  Spinner,
  Alert,
} from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaSave, FaArrowLeft } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { vendorsApi, masterDataApi, getErrorMessage } from '../../api';
import { VendorCreateRequest } from '../../api/vendors';

const VendorFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const [error, setError] = useState<string | null>(null);

  // Form setup
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<VendorCreateRequest>({
    defaultValues: {
      country: 'India',
    },
  });

  // Fetch existing vendor for edit mode
  const { data: existingVendor, isLoading: loadingVendor } = useQuery({
    queryKey: ['vendor', id],
    queryFn: () => vendorsApi.getById(Number(id)),
    enabled: isEditMode,
  });

  // Fetch categories
  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: () => masterDataApi.getCategories(),
  });

  // Load existing data in edit mode
  useEffect(() => {
    if (existingVendor) {
      reset({
        vendorCode: existingVendor.vendorCode,
        vendorName: existingVendor.vendorName,
        contactPerson: existingVendor.contactPerson,
        email: existingVendor.email,
        phone: existingVendor.phone,
        mobile: existingVendor.mobile,
        address: existingVendor.address,
        city: existingVendor.city,
        state: existingVendor.state,
        pincode: existingVendor.pincode,
        country: existingVendor.country || 'India',
        gstNumber: existingVendor.gstNumber,
        panNumber: existingVendor.panNumber,
        bankName: existingVendor.bankName,
        bankBranch: existingVendor.bankBranch,
        accountNumber: existingVendor.accountNumber,
        ifscCode: existingVendor.ifscCode,
        paymentTerms: existingVendor.paymentTerms,
        creditDays: existingVendor.creditDays,
        creditLimit: existingVendor.creditLimit,
        categoryIds: existingVendor.categories?.map(c => c.categoryId) || [],
      });
    }
  }, [existingVendor, reset]);

  // Create/Update mutations
  const createMutation = useMutation({
    mutationFn: (data: VendorCreateRequest) => vendorsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vendors'] });
      navigate('/vendors');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: VendorCreateRequest) => vendorsApi.update(Number(id), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vendors'] });
      queryClient.invalidateQueries({ queryKey: ['vendor', id] });
      navigate('/vendors');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // Handle form submission
  const onSubmit = async (data: VendorCreateRequest) => {
    setError(null);
    if (isEditMode) {
      await updateMutation.mutateAsync(data);
    } else {
      await createMutation.mutateAsync(data);
    }
  };

  if (isEditMode && loadingVendor) {
    return <LoadingSpinner fullPage text="Loading vendor..." />;
  }

  return (
    <div>
      <PageHeader
        title={isEditMode ? 'Edit Vendor' : 'Add New Vendor'}
        subtitle={isEditMode ? `Editing ${existingVendor?.vendorName}` : 'Register a new vendor/supplier'}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Vendors', path: '/vendors' },
          { label: isEditMode ? 'Edit' : 'New' },
        ]}
        actions={
          <Button variant="outline-secondary" onClick={() => navigate('/vendors')}>
            <FaArrowLeft className="me-2" /> Back to List
          </Button>
        }
      />

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Form onSubmit={handleSubmit(onSubmit)}>
        {/* Basic Information */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Basic Information</h5>
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Vendor Code <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    {...register('vendorCode', { required: 'Vendor code is required' })}
                    isInvalid={!!errors.vendorCode}
                    placeholder="e.g., VND001"
                    disabled={isEditMode}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.vendorCode?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={8}>
                <Form.Group>
                  <Form.Label>Vendor Name <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    {...register('vendorName', { required: 'Vendor name is required' })}
                    isInvalid={!!errors.vendorName}
                    placeholder="Enter vendor/company name"
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.vendorName?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Contact Person</Form.Label>
                  <Form.Control
                    {...register('contactPerson')}
                    placeholder="Primary contact name"
                  />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Categories */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Categories</h5>
          </Card.Header>
          <Card.Body>
            <Form.Group>
              <Form.Label>Vendor Categories</Form.Label>
              <div className="d-flex flex-wrap gap-3">
                {categories.map((category: any) => (
                  <Form.Check
                    key={category.id}
                    type="checkbox"
                    id={`category-${category.id}`}
                    label={category.categoryName}
                    value={category.id}
                    {...register('categoryIds')}
                  />
                ))}
                {categories.length === 0 && <span className="text-muted">No categories available</span>}
              </div>
              <Form.Text className="text-muted">
                Select one or more categories for this vendor
              </Form.Text>
            </Form.Group>
          </Card.Body>
        </Card>

        {/* Contact Information */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Contact Information</h5>
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Email</Form.Label>
                  <Form.Control
                    type="email"
                    {...register('email')}
                    placeholder="vendor@example.com"
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Phone</Form.Label>
                  <Form.Control
                    {...register('phone')}
                    placeholder="Landline number"
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Mobile</Form.Label>
                  <Form.Control
                    {...register('mobile')}
                    placeholder="Mobile number"
                  />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Address */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Address</h5>
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={12}>
                <Form.Group>
                  <Form.Label>Address</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    {...register('address')}
                    placeholder="Street address, Area, Landmark"
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>City</Form.Label>
                  <Form.Control {...register('city')} placeholder="City" />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>State</Form.Label>
                  <Form.Control {...register('state')} placeholder="State" />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Pincode</Form.Label>
                  <Form.Control {...register('pincode')} placeholder="PIN Code" />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Country</Form.Label>
                  <Form.Control {...register('country')} placeholder="Country" />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Tax & Statutory Information */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Tax & Statutory Information</h5>
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group>
                  <Form.Label>GST Number</Form.Label>
                  <Form.Control
                    {...register('gstNumber')}
                    placeholder="22AAAAA0000A1Z5"
                    style={{ textTransform: 'uppercase' }}
                  />
                  <Form.Text className="text-muted">
                    15-character GST Identification Number
                  </Form.Text>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>PAN Number</Form.Label>
                  <Form.Control
                    {...register('panNumber')}
                    placeholder="AAAAA0000A"
                    style={{ textTransform: 'uppercase' }}
                  />
                  <Form.Text className="text-muted">
                    10-character Permanent Account Number
                  </Form.Text>
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Bank Details */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Bank Details</h5>
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Bank Name</Form.Label>
                  <Form.Control {...register('bankName')} placeholder="Bank name" />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Branch</Form.Label>
                  <Form.Control {...register('bankBranch')} placeholder="Branch name" />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>IFSC Code</Form.Label>
                  <Form.Control
                    {...register('ifscCode')}
                    placeholder="SBIN0001234"
                    style={{ textTransform: 'uppercase' }}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Account Number</Form.Label>
                  <Form.Control
                    {...register('accountNumber')}
                    placeholder="Bank account number"
                  />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Payment Terms */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Payment Terms</h5>
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Payment Terms</Form.Label>
                  <Form.Select {...register('paymentTerms')}>
                    <option value="">Select Payment Terms</option>
                    <option value="Advance">100% Advance</option>
                    <option value="COD">Cash on Delivery</option>
                    <option value="Net15">Net 15 Days</option>
                    <option value="Net30">Net 30 Days</option>
                    <option value="Net45">Net 45 Days</option>
                    <option value="Net60">Net 60 Days</option>
                    <option value="Net90">Net 90 Days</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Credit Limit (₹)</Form.Label>
                  <Form.Control
                    type="number"
                    min="0"
                    step="1000"
                    {...register('creditLimit', { valueAsNumber: true })}
                    placeholder="0"
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Credit Days</Form.Label>
                  <Form.Control
                    type="number"
                    min="0"
                    max="365"
                    {...register('creditDays', { valueAsNumber: true })}
                    placeholder="0"
                  />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Actions */}
        <Card>
          <Card.Body className="d-flex flex-wrap gap-2 justify-content-between">
            <Button
              variant="outline-secondary"
              onClick={() => navigate('/vendors')}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}
            >
              {(isSubmitting || createMutation.isPending || updateMutation.isPending) && (
                <Spinner as="span" animation="border" size="sm" className="me-2" />
              )}
              <FaSave className="me-2" /> {isEditMode ? 'Update Vendor' : 'Create Vendor'}
            </Button>
          </Card.Body>
        </Card>
      </Form>
    </div>
  );
};

export default VendorFormPage;
