import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col, Table, Badge, InputGroup, Alert } from 'react-bootstrap';
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
  FaSearch,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { purchaseOrdersApi, vendorsApi, materialsApi, indentsApi, getErrorMessage } from '../../api';
import type { POCreateRequest, POItemCreateRequest, Vendor, Material } from '../../api';

interface FormData {
  vendorId: number | null;
  indentId?: number | null;
  deliveryDate: string;
  paymentTerms: string;
  deliveryTerms: string;
  remarks: string;
  items: POItemFormData[];
}

interface POItemFormData {
  materialId: number | null;
  indentItemId?: number | null;
  orderedQuantity: number;
  unitRate: number;
  taxRate: number;
  deliveryDate: string;
  remarks: string;
}

const POFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditMode = !!id;

  const [selectedVendor, setSelectedVendor] = useState<Vendor | null>(null);
  const [materialSearch, setMaterialSearch] = useState('');

  const { register, handleSubmit, control, watch, setValue, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    defaultValues: {
      vendorId: null,
      indentId: null,
      deliveryDate: format(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd'),
      paymentTerms: 'Net 30',
      deliveryTerms: 'Ex-Works',
      remarks: '',
      items: [{ materialId: null, indentItemId: null, orderedQuantity: 1, unitRate: 0, taxRate: 18, deliveryDate: '', remarks: '' }],
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
  const vendors = vendorsData?.content?.length
    ? vendorsData.content
    : [
        { id: 1, vendorName: 'Agro Seeds India Ltd', vendorCode: 'AGRO001', contactPerson: 'Ramesh Kumar', contactEmail: 'ramesh@agroseeds.com', contactPhone: '9876543210', gstNumber: '29AACCV1234F1Z5' },
        { id: 2, vendorName: 'Bharat Fertilizers Pvt Ltd', vendorCode: 'BFA002', contactPerson: 'Sunil Mehta', contactEmail: 'sunil@bharatfert.com', contactPhone: '9876501234', gstNumber: '27AABCV5678G1Z1' },
        { id: 3, vendorName: 'Green Crop Suppliers', vendorCode: 'GCS003', contactPerson: 'Anita Rao', contactEmail: 'anita@greencrop.in', contactPhone: '9845612300', gstNumber: '36AADCV9012H1Z3' },
        { id: 4, vendorName: 'National Agro Traders', vendorCode: 'NAT004', contactPerson: 'Vijay Sharma', contactEmail: 'vijay@natagro.com', contactPhone: '9123456780', gstNumber: '07AABCN3456I1Z7' },
        { id: 5, vendorName: 'Pioneer Seed Corporation', vendorCode: 'PSC005', contactPerson: 'Priya Nair', contactEmail: 'priya@pioneerseed.co.in', contactPhone: '9988776655', gstNumber: '32AABCP7890J1Z2' },
      ];

  // Fetch materials
  const { data: materialsData } = useQuery({
    queryKey: ['materials-active', materialSearch],
    queryFn: () => materialsApi.list({ search: materialSearch, isActive: true, size: 50 }),
  });
  const materials = materialsData?.content || [];

  // Fetch passed indent details if selected
  const selectedIndentId = watch('indentId');
  const { data: selectedIndent } = useQuery({
    queryKey: ['indent', selectedIndentId],
    queryFn: () => indentsApi.getById(selectedIndentId!),
    enabled: !!selectedIndentId && !isEditMode,
  });

  // Populate form from selected indent
  useEffect(() => {
    if (selectedIndent && !isEditMode) {
      // Check if we should populate (e.g. if items are empty or just have the default empty row)
      const currentItems = watch('items');
      const isDefaultState = currentItems.length === 1 && !currentItems[0].materialId;

      if (isDefaultState || currentItems.length === 0) {
        // Map indent items to PO items
        const poItems = selectedIndent.items.map(item => ({
          materialId: item.materialId,
          indentItemId: item.id, // Map indent item ID
          orderedQuantity: item.deptQuantity || item.rmQuantity || item.quantity,
          unitRate: item.pricing || 0,
          taxRate: 18, // Default tax rate
          deliveryDate: format(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd'),
          remarks: item.purpose || '',
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
        indentId: existingPO.indentId || null,
        deliveryDate: existingPO.deliveryDate?.split('T')[0] || '',
        paymentTerms: existingPO.paymentTerms || '',
        deliveryTerms: existingPO.deliveryTerms || '',
        remarks: existingPO.remarks || '',
        items: existingPO.items?.map(item => ({
          materialId: item.materialId,
          indentItemId: item.indentItemId || null,
          orderedQuantity: item.orderedQuantity,
          unitRate: item.unitRate,
          taxRate: item.taxRate,
          deliveryDate: item.deliveryDate?.split('T')[0] || '',
          remarks: item.remarks || '',
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

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: (data: Partial<POCreateRequest>) => purchaseOrdersApi.update(Number(id), data),
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

    const validItems: POItemCreateRequest[] = data.items
      .filter(item => item.materialId)
      .map(item => ({
        materialId: item.materialId!,
        indentItemId: item.indentItemId || undefined,
        orderedQuantity: item.orderedQuantity,
        unitRate: item.unitRate,
        taxRate: item.taxRate,
        deliveryDate: item.deliveryDate || undefined,
        remarks: item.remarks || undefined,
      }));

    const payload: POCreateRequest = {
      vendorId: data.vendorId,
      indentId: data.indentId || undefined,
      deliveryDate: data.deliveryDate,
      paymentTerms: data.paymentTerms,
      deliveryTerms: data.deliveryTerms,
      remarks: data.remarks || undefined,
      items: validItems,
    };

    if (isEditMode) {
      updateMutation.mutate(payload);
    } else {
      createMutation.mutate(payload);
    }
  };

  const handleVendorChange = (vendorId: number) => {
    setValue('vendorId', vendorId);
    const vendor = vendors.find(v => v.id === vendorId);
    setSelectedVendor(vendor || null);
  };

  const addItem = () => {
    append({ materialId: null, indentItemId: null, orderedQuantity: 1, unitRate: 0, taxRate: 18, deliveryDate: '', remarks: '' });
  };

  const calculateItemTotal = (item: POItemFormData) => {
    const subtotal = item.orderedQuantity * item.unitRate;
    const tax = subtotal * (item.taxRate / 100);
    return subtotal + tax;
  };

  const calculateTotals = () => {
    let subtotal = 0;
    let taxTotal = 0;

    watchItems.forEach(item => {
      if (item.materialId) {
        const itemSubtotal = item.orderedQuantity * item.unitRate;
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
                      >
                        <option value="">Select Vendor</option>
                        {vendors.map(v => (
                          <option key={v.id} value={v.id}>{v.vendorName} ({v.vendorCode})</option>
                        ))}
                      </Form.Select>
                      <Form.Control.Feedback type="invalid">{errors.vendorId?.message}</Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>From Indent (Optional)</Form.Label>
                      <Form.Select {...register('indentId', { valueAsNumber: true })}>
                        <option value="">Direct PO (No Indent)</option>
                        {approvedIndents.map((indent: any) => (
                          <option key={indent.id} value={indent.id}>
                            {indent.indentNo} - {indent.departmentName} ({format(new Date(indent.indentDate), 'dd/MM/yyyy')})
                          </option>
                        ))}
                      </Form.Select>
                    </Form.Group>
                  </Col>
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
                      <Form.Label>Delivery Terms</Form.Label>
                      <Form.Select {...register('deliveryTerms')}>
                        <option value="Ex-Works">Ex-Works</option>
                        <option value="FOR Destination">FOR Destination</option>
                        <option value="CIF">CIF</option>
                        <option value="FOB">FOB</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                </Row>
                <Form.Group className="mb-3">
                  <Form.Label>Remarks</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    placeholder="Additional notes or instructions..."
                    {...register('remarks')}
                  />
                </Form.Group>
              </Card.Body>
            </Card>

            {/* Line Items */}
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
                      {/* <th style={{ width: '120px' }}>Rate (₹)</th> */}
                      <th style={{ width: '80px' }}>Tax %</th>
                      <th style={{ width: '120px' }}>Total (₹)</th>
                      <th style={{ width: '50px' }}></th>
                    </tr>
                  </thead>
                  <tbody>
                    {fields.length === 0 ? (
                      <tr>
                        <td colSpan={8} className="text-center py-4 text-muted">
                          No items added. Click "Add Item" to start.
                        </td>
                      </tr>
                    ) : (
                      fields.map((field, index) => {
                        const material = getMaterialInfo(watchItems[index]?.materialId);
                        const itemTotal = calculateItemTotal(watchItems[index] || { orderedQuantity: 0, unitRate: 0, taxRate: 0 });

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
                              <Badge bg="secondary">-</Badge>
                            </td>
                            <td>
                              <Form.Control
                                type="number"
                                min="1"
                                step="1"
                                {...register(`items.${index}.orderedQuantity`, { valueAsNumber: true, min: 1 })}
                                size="sm"
                              />
                            </td>
                            {/* unitRate input hidden
                            <td>
                              <Form.Control
                                type="number"
                                min="0"
                                step="0.01"
                                {...register(`items.${index}.unitRate`, { valueAsNumber: true, min: 0 })}
                                size="sm"
                              />
                            </td>
                            */}
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
