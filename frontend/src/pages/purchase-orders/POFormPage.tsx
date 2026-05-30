import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col, Table, Badge } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, useFieldArray } from 'react-hook-form';
import { toast } from 'react-toastify';
import { format } from 'date-fns';
import {
  FaSave,
  FaTimes,
  FaPlus,
  FaTrash,
  FaShoppingCart,
  FaFileInvoice,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { purchaseOrdersApi, vendorsApi, materialsApi, indentsApi, getErrorMessage } from '../../api';
import type { POCreateRequest, POLineItemRequest, UpdatePORequest } from '../../api/purchaseOrders';
import type { Vendor } from '../../api/vendors';

// ── Form-only data shape (internal, not sent to backend directly) ──
interface FormData {
  vendorId: number | null;
  indentId?: number | null;
  deliveryDate: string;           // maps to expectedDeliveryDate (create) / deliveryDate (update)
  paymentTerms: string;
  termsConditions: string;        // was: deliveryTerms
  notes: string;                  // was: remarks
  items: POItemFormData[];        // renamed to lineItems only in payload
}

interface POItemFormData {
  materialId: number | null;
  indentDetailId?: number | null; // was: indentItemId
  quantity: number;               // was: orderedQuantity
  unitPrice: number;              // was: unitRate
  taxRate: number;
  deliveryDate: string;
  notes: string;                  // was: remarks
}

const POFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const [selectedVendor, setSelectedVendor] = useState<Vendor | null>(null);
  const [materialSearch] = useState('');

  const { register, handleSubmit, control, watch, setValue, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    defaultValues: {
      vendorId: null,
      indentId: null,
      deliveryDate: format(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd'),
      paymentTerms: 'Net 30',
      termsConditions: 'Ex-Works',
      notes: '',
      items: [{ materialId: null, indentDetailId: null, quantity: 1, unitPrice: 0, taxRate: 18, deliveryDate: '', notes: '' }],
    },
  });

  const { fields, append, remove } = useFieldArray({ control, name: 'items' });
  const watchItems = watch('items');

  // Fetch existing PO for edit mode
  const { data: existingPO, isLoading: loadingPO } = useQuery({
    queryKey: ['po', id],
    queryFn: () => purchaseOrdersApi.getById(Number(id)),
    enabled: isEditMode,
  });

  // Fetch approved indents for PO creation
  const { data: approvedIndents = [] } = useQuery({
    queryKey: ['approved-indents'],
    queryFn: () => purchaseOrdersApi.getApprovedIndents(),
    enabled: !isEditMode,
  });

  // Fetch vendors
  const { data: vendorsData } = useQuery({
    queryKey: ['vendors-active'],
    queryFn: () => vendorsApi.list({ page: 0, size: 100, isActive: true }),
  });
  const vendors: Vendor[] = vendorsData?.content ?? [];

  // Fetch materials
  const { data: materialsData } = useQuery({
    queryKey: ['materials-active', materialSearch],
    queryFn: () => materialsApi.list({ search: materialSearch, isActive: true, size: 50 }),
  });
  const materials = materialsData?.content || [];

  // Fetch indent details when an indent is selected
  const selectedIndentId = watch('indentId');
  const { data: selectedIndent } = useQuery({
    queryKey: ['indent', selectedIndentId],
    queryFn: () => indentsApi.getById(selectedIndentId!),
    enabled: !!selectedIndentId && !isEditMode,
  });

  // Populate form from selected indent
  useEffect(() => {
    if (selectedIndent && !isEditMode) {
      const currentItems = watch('items');
      const isDefaultState = currentItems.length === 1 && !currentItems[0].materialId;

      if (isDefaultState || currentItems.length === 0) {
        // Fallback: indent API may use `items` or `details` naming
        const indentLines = selectedIndent.details ?? [];
        const poItems: POItemFormData[] = indentLines.map((item: any) => ({
          materialId: item.materialId,
          indentDetailId: item.id,                              // indent detail ID
          quantity: item.deptQuantity || item.rmQuantity || item.quantity,
          unitPrice: item.pricing || 0,
          taxRate: 18,
          deliveryDate: format(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd'),
          notes: item.purpose || '',
        }));

        setValue('items', poItems);
        toast.info(`Loaded ${poItems.length} items from Indent ${selectedIndent.indentNumber}`);
      }
    }
  }, [selectedIndent, isEditMode, setValue]);

  // Populate form when editing existing PO
  useEffect(() => {
    if (existingPO) {
      reset({
        vendorId: existingPO.vendorId,
        indentId: existingPO.indentId ?? null,
        deliveryDate: existingPO.expectedDeliveryDate?.split('T')[0] || existingPO.deliveryDate?.split('T')[0] || '',
        paymentTerms: existingPO.paymentTerms || '',
        termsConditions: existingPO.termsConditions || '',
        notes: existingPO.notes || '',
        items: existingPO.details?.map(item => ({
          materialId: item.materialId,
          indentDetailId: item.indentDetailId ?? null,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          taxRate: item.taxRate ?? 0,
          deliveryDate: item.expectedDeliveryDate?.split('T')[0] || '',
          notes: item.notes || '',
        })) || [],
      });
      const vendor = vendors.find(v => v.id === existingPO.vendorId);
      if (vendor) setSelectedVendor(vendor);
    }
  }, [existingPO, vendors, reset]);

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: POCreateRequest) => purchaseOrdersApi.create(data),
    onSuccess: (po) => {
      toast.success(`Purchase Order ${po.poNumber} created successfully`);
      queryClient.invalidateQueries({ queryKey: ['pos'] });
      navigate('/purchase-orders');
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  // Update mutation — backend accepts header fields only (no line items)
  const updateMutation = useMutation({
    mutationFn: (data: UpdatePORequest) => purchaseOrdersApi.update(Number(id), data),
    onSuccess: () => {
      toast.success('Purchase Order updated successfully');
      queryClient.invalidateQueries({ queryKey: ['pos'] });
      navigate('/purchase-orders');
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const onSubmit = (data: FormData) => {
    if (!data.vendorId) {
      toast.error('Please select a vendor');
      return;
    }

    if (data.items.length === 0 || !data.items.some(item => item.materialId)) {
      toast.error('Please add at least one item');
      return;
    }

    if (isEditMode) {
      // Edit mode: only header fields can be updated
      const updatePayload: UpdatePORequest = {
        deliveryDate: data.deliveryDate || undefined,
        paymentTerms: data.paymentTerms || undefined,
        termsConditions: data.termsConditions || undefined,
        notes: data.notes || undefined,
      };
      updateMutation.mutate(updatePayload);
    } else {
      // Create mode: full payload with line items
      const lineItems: POLineItemRequest[] = data.items
        .filter(item => item.materialId)
        .map(item => ({
          indentDetailId: item.indentDetailId!,   // required by backend
          materialId: item.materialId!,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          taxRate: item.taxRate,
          expectedDeliveryDate: item.deliveryDate || undefined,
          notes: item.notes || undefined,
        }));

      const payload: POCreateRequest = {
        indentId: data.indentId!,                 // required by backend
        vendorId: data.vendorId,
        expectedDeliveryDate: data.deliveryDate,
        paymentTerms: data.paymentTerms,
        termsConditions: data.termsConditions,
        notes: data.notes || undefined,
        lineItems,
      };
      createMutation.mutate(payload);
    }
  };

  const handleVendorChange = (vendorId: number) => {
    setValue('vendorId', vendorId);
    const vendor = vendors.find(v => v.id === vendorId);
    setSelectedVendor(vendor || null);
  };

  const addItem = () => {
    append({ materialId: null, indentDetailId: null, quantity: 1, unitPrice: 0, taxRate: 18, deliveryDate: '', notes: '' });
  };

  const calculateItemTotal = (item: POItemFormData) => {
    const subtotal = item.quantity * item.unitPrice;
    const tax = subtotal * (item.taxRate / 100);
    return subtotal + tax;
  };

  const calculateTotals = () => {
    let subtotal = 0;
    let taxTotal = 0;

    watchItems.forEach(item => {
      if (item.materialId) {
        const itemSubtotal = item.quantity * item.unitPrice;
        subtotal += itemSubtotal;
        taxTotal += itemSubtotal * (item.taxRate / 100);
      }
    });

    return { subtotal, taxTotal, grandTotal: subtotal + taxTotal };
  };

  const totals = calculateTotals();

  const getMaterialInfo = (materialId: number | null) => {
    if (!materialId) return null;
    return materials.find(m => m.id === materialId);
  };

  if (isEditMode && loadingPO) {
    return <LoadingSpinner text="Loading Purchase Order..." />;
  }

  return (
    <div>
      <PageHeader
        title={isEditMode ? 'Edit Purchase Order' : 'Create Purchase Order'}
        subtitle={isEditMode ? `Editing: ${existingPO?.poNumber}` : 'Create a new purchase order'}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Purchase Orders', path: '/purchase-orders' },
          { label: isEditMode ? 'Edit' : 'New' },
        ]}
      />

      <Form onSubmit={handleSubmit(onSubmit)}>
        <Row>
          <Col lg={8}>
            {/* Vendor & Basic Info */}
            <Card className="mb-3">
              <Card.Header>
                <FaShoppingCart className="me-2 text-primary" />
                <span className="fw-bold">Order Details</span>
              </Card.Header>
              <Card.Body>
                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Vendor <span className="text-danger">*</span></Form.Label>
                      <Form.Select
                        {...register('vendorId', { required: 'Vendor is required', valueAsNumber: true })}
                        onChange={(e) => handleVendorChange(Number(e.target.value))}
                        isInvalid={!!errors.vendorId}
                        disabled={isEditMode}
                      >
                        <option value="">Select Vendor</option>
                        {vendors.map(v => (
                          <option key={v.id} value={v.id}>{v.vendorName} ({v.vendorCode})</option>
                        ))}
                      </Form.Select>
                      <Form.Control.Feedback type="invalid">{errors.vendorId?.message}</Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  {!isEditMode && (
                    <Col md={6}>
                      <Form.Group className="mb-3">
                        <Form.Label>From Approved Indent <span className="text-danger">*</span></Form.Label>
                        <Form.Select {...register('indentId', { required: !isEditMode ? 'Indent is required' : false, valueAsNumber: true })}>
                          <option value="">Select Approved Indent</option>
                          {approvedIndents.map((indent) => (
                            <option key={indent.indentId} value={indent.indentId}>
                              {indent.indentNumber} — {indent.departmentName} ({format(new Date(indent.indentDate), 'dd/MM/yyyy')})
                            </option>
                          ))}
                        </Form.Select>
                        {errors.indentId && <div className="text-danger small mt-1">{errors.indentId.message}</div>}
                      </Form.Group>
                    </Col>
                  )}
                </Row>
                <Row>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>Delivery Date <span className="text-danger">*</span></Form.Label>
                      <Form.Control
                        type="date"
                        {...register('deliveryDate', { required: 'Delivery date is required' })}
                        isInvalid={!!errors.deliveryDate}
                      />
                      <Form.Control.Feedback type="invalid">{errors.deliveryDate?.message}</Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>Payment Terms</Form.Label>
                      <Form.Select {...register('paymentTerms')}>
                        <option value="Net 30">Net 30</option>
                        <option value="Net 45">Net 45</option>
                        <option value="Net 60">Net 60</option>
                        <option value="Advance">Advance Payment</option>
                        <option value="COD">Cash on Delivery</option>
                        <option value="LC">Letter of Credit</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>Terms & Conditions</Form.Label>
                      <Form.Select {...register('termsConditions')}>
                        <option value="Ex-Works">Ex-Works</option>
                        <option value="FOR Destination">FOR Destination</option>
                        <option value="CIF">CIF</option>
                        <option value="FOB">FOB</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                </Row>
                <Form.Group className="mb-3">
                  <Form.Label>Notes</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    placeholder="Additional notes or instructions..."
                    {...register('notes')}
                  />
                </Form.Group>
              </Card.Body>
            </Card>

            {/* Line Items — shown only for create mode (backend doesn't allow line item changes on update) */}
            {!isEditMode && (
              <Card className="mb-3">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <span>
                    <FaFileInvoice className="me-2 text-success" />
                    <span className="fw-bold">Order Items</span>
                  </span>
                  <Button variant="outline-primary" size="sm" onClick={addItem}>
                    <FaPlus className="me-1" /> Add Item
                  </Button>
                </Card.Header>
                <Card.Body className="p-0">
                  <Table responsive className="mb-0">
                    <thead className="table-light">
                      <tr>
                        <th style={{ width: '40px' }}>#</th>
                        <th style={{ minWidth: '250px' }}>Material</th>
                        <th style={{ width: '80px' }}>UOM</th>
                        <th style={{ width: '100px' }}>Qty</th>
                        <th style={{ width: '80px' }}>Tax %</th>
                        <th style={{ width: '120px' }}>Total (₹)</th>
                        <th style={{ width: '50px' }}></th>
                      </tr>
                    </thead>
                    <tbody>
                      {fields.length === 0 ? (
                        <tr>
                          <td colSpan={7} className="text-center py-4 text-muted">
                            Select an approved indent above to load items automatically, or click "Add Item".
                          </td>
                        </tr>
                      ) : (
                        fields.map((field, index) => {
                          const material = getMaterialInfo(watchItems[index]?.materialId);
                          const itemTotal = calculateItemTotal(watchItems[index] || { quantity: 0, unitPrice: 0, taxRate: 0 } as POItemFormData);

                          return (
                            <tr key={field.id}>
                              <td className="align-middle text-center">{index + 1}</td>
                              <td>
                                <Form.Select
                                  {...register(`items.${index}.materialId`, { valueAsNumber: true })}
                                  size="sm"
                                >
                                  <option value="">Select Material</option>
                                  {materials.map(m => (
                                    <option key={m.id} value={m.id}>
                                      {m.code} - {m.name}
                                    </option>
                                  ))}
                                </Form.Select>
                              </td>
                              <td className="align-middle text-center">
                                <Badge bg="secondary">{material?.uomCode || '-'}</Badge>
                              </td>
                              <td>
                                <Form.Control
                                  type="number"
                                  min="1"
                                  step="1"
                                  {...register(`items.${index}.quantity`, { valueAsNumber: true, min: 1 })}
                                  size="sm"
                                />
                              </td>
                              <td>
                                <Form.Select
                                  {...register(`items.${index}.taxRate`, { valueAsNumber: true })}
                                  size="sm"
                                >
                                  <option value={0}>0%</option>
                                  <option value={5}>5%</option>
                                  <option value={12}>12%</option>
                                  <option value={18}>18%</option>
                                  <option value={28}>28%</option>
                                </Form.Select>
                              </td>
                              <td className="align-middle text-end fw-bold">
                                ₹{itemTotal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                              </td>
                              <td className="align-middle text-center">
                                {fields.length > 1 && (
                                  <Button
                                    variant="outline-danger"
                                    size="sm"
                                    onClick={() => remove(index)}
                                    title="Remove"
                                  >
                                    <FaTrash />
                                  </Button>
                                )}
                              </td>
                            </tr>
                          );
                        })
                      )}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            )}

            {/* Edit mode: line items are read-only */}
            {isEditMode && existingPO && existingPO.details.length > 0 && (
              <Card className="mb-3">
                <Card.Header>
                  <FaFileInvoice className="me-2 text-success" />
                  <span className="fw-bold">Order Items (read-only)</span>
                </Card.Header>
                <Card.Body className="p-0">
                  <Table responsive className="mb-0">
                    <thead className="table-light">
                      <tr>
                        <th>#</th>
                        <th>Material</th>
                        <th className="text-end">Qty</th>
                        <th className="text-end">Unit Price</th>
                        <th className="text-end">Line Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {existingPO.details.map((item, index) => (
                        <tr key={item.id}>
                          <td>{index + 1}</td>
                          <td>
                            <div className="fw-medium">{item.materialCode}</div>
                            <small className="text-muted">{item.materialDescription}</small>
                          </td>
                          <td className="text-end">{item.quantity} {item.unitOfMeasure}</td>
                          <td className="text-end">₹{item.unitPrice.toLocaleString('en-IN')}</td>
                          <td className="text-end fw-bold">₹{item.lineTotal.toLocaleString('en-IN')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            )}
          </Col>

          <Col lg={4}>
            {/* Vendor Info Card */}
            {selectedVendor && (
              <Card className="mb-3">
                <Card.Header>
                  <span className="fw-bold">Vendor Information</span>
                </Card.Header>
                <Card.Body>
                  <p className="mb-1"><strong>{selectedVendor.vendorName}</strong></p>
                  <p className="mb-1 text-muted small">{selectedVendor.vendorCode}</p>
                  {selectedVendor.contactPerson && (
                    <p className="mb-1 small">Contact: {selectedVendor.contactPerson}</p>
                  )}
                  {selectedVendor.contactEmail && (
                    <p className="mb-1 small">Email: {selectedVendor.contactEmail}</p>
                  )}
                  {selectedVendor.contactPhone && (
                    <p className="mb-1 small">Phone: {selectedVendor.contactPhone}</p>
                  )}
                  {selectedVendor.gstNumber && (
                    <p className="mb-0 small">GST: {selectedVendor.gstNumber}</p>
                  )}
                </Card.Body>
              </Card>
            )}

            {/* Order Summary */}
            {!isEditMode && (
              <Card className="mb-3">
                <Card.Header>
                  <span className="fw-bold">Order Summary</span>
                </Card.Header>
                <Card.Body>
                  <div className="d-flex justify-content-between mb-2">
                    <span>Subtotal:</span>
                    <span>₹{totals.subtotal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span>Tax:</span>
                    <span>₹{totals.taxTotal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                  </div>
                  <hr />
                  <div className="d-flex justify-content-between fw-bold fs-5">
                    <span>Grand Total:</span>
                    <span className="text-primary">₹{totals.grandTotal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                  </div>
                </Card.Body>
              </Card>
            )}

            {/* Action Buttons */}
            <Card>
              <Card.Body>
                <div className="d-grid gap-2">
                  <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}
                  >
                    <FaSave className="me-2" />
                    {isEditMode ? 'Update Order' : 'Create Order'}
                  </Button>
                  <Button
                    variant="outline-secondary"
                    onClick={() => navigate('/purchase-orders')}
                  >
                    <FaTimes className="me-2" />
                    Cancel
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

export default POFormPage;
