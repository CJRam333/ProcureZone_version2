import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Form,
  Button,
  Card,
  Row,
  Col,
  Table,
  Alert,
  Spinner,
  Badge,
} from 'react-bootstrap';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import { FaSave, FaArrowLeft, FaTimes, FaPlus } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { grnApi, purchaseOrdersApi, getErrorMessage } from '../../api';
import { POStatus } from '../../api/purchaseOrders';

// Validation schema
const grnItemSchema = z.object({
  poDetailId: z.number().min(1),
  materialId: z.number().min(1),
  materialCode: z.string(),
  materialDescription: z.string(),
  orderedQuantity: z.number(),
  previouslyReceived: z.number(),
  pendingQuantity: z.number(),
  receivedQuantity: z.number().min(0, 'Quantity must be 0 or more'),
  acceptedQuantity: z.number().min(0).optional(),
  rejectedQuantity: z.number().min(0).optional(),
  remarks: z.string().optional(),
});

const grnFormSchema = z.object({
  poId: z.number().min(1, 'Purchase Order is required'),
  challanNumber: z.string().min(1, 'Challan number is required'),
  challanDate: z.string().min(1, 'Challan date is required'),
  vehicleNumber: z.string().optional(),
  transporterName: z.string().optional(),
  receivedDate: z.string().min(1, 'Received date is required'),
  remarks: z.string().optional(),
  items: z.array(grnItemSchema).min(1, 'At least one item is required'),
});

type GRNFormData = z.infer<typeof grnFormSchema>;

const GRNFormPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const queryClient = useQueryClient();
  const poIdParam = searchParams.get('poId');

  const [error, setError] = useState<string | null>(null);
  const [selectedPoId, setSelectedPoId] = useState<number | null>(poIdParam ? Number(poIdParam) : null);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
    setValue,
    watch,
  } = useForm<GRNFormData>({
    resolver: zodResolver(grnFormSchema),
    defaultValues: {
      receivedDate: format(new Date(), 'yyyy-MM-dd'),
      items: [],
    },
  });

  const { fields, replace } = useFieldArray({
    control,
    name: 'items',
  });

  // Fetch POs for selection (only confirmed POs with pending deliveries)
  const { data: posResponse } = useQuery({
    queryKey: ['pos-for-grn'],
    queryFn: () => purchaseOrdersApi.list({ status: POStatus.SENT_TO_VENDOR, size: 100 }),
  });

  // Fetch selected PO details
  const { data: selectedPo, isLoading: loadingPo } = useQuery({
    queryKey: ['po', selectedPoId],
    queryFn: () => purchaseOrdersApi.getById(selectedPoId!),
    enabled: !!selectedPoId,
  });

  // When PO is selected, populate items
  React.useEffect(() => {
    if (selectedPo) {
      setValue('poId', selectedPo.id);
      const grnItems = selectedPo.details
        ?.filter((item) => item.pendingQuantity > 0)
        .map((item) => ({
          poDetailId: item.id,
          materialId: item.materialId,
          materialCode: item.materialCode,
          materialDescription: item.materialDescription ?? '',
          orderedQuantity: item.quantity,
          previouslyReceived: item.receivedQuantity || 0,
          pendingQuantity: item.pendingQuantity,
          receivedQuantity: 0,
          acceptedQuantity: 0,
          rejectedQuantity: 0,
          remarks: '',
        })) || [];
      replace(grnItems);
    }
  }, [selectedPo, setValue, replace]);

  // Create GRN mutation
  const createMutation = useMutation({
    mutationFn: async (data: GRNFormData) => {
      // The backend expects one GRN per Item (Indent Detail Link)
      // So we must loop through the items and create individual GRN requests
      const validItems = data.items.filter(item => item.receivedQuantity > 0);

      const promises = validItems.map(item => {
        const originalPoItem = selectedPo?.details.find((i) => i.id === item.poDetailId);

        if (!selectedPo?.indentId || !originalPoItem?.indentDetailId) {
          throw new Error(`Missing Indent Information for item ${item.materialCode}. Cannot create GRN.`);
        }

        const grnRequest = {
          indentId: selectedPo.indentId,
          indentDetailsId: originalPoItem.indentDetailId,
          receivedQuantity: item.receivedQuantity,
          rate: originalPoItem.unitPrice,
          vendorName: selectedPo.vendorName,
          openingQuantity: 0,
          comments: `${data.remarks || ''}\nChallan: ${data.challanNumber} (${data.challanDate})\nVehicle: ${data.vehicleNumber || 'N/A'}\nTransporter: ${data.transporterName || 'N/A'}\nItem Remarks: ${item.remarks || ''}`.trim(),
        };

        return grnApi.create(grnRequest);
      });

      return Promise.all(promises);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['grns'] });
      queryClient.invalidateQueries({ queryKey: ['pos'] });
      navigate('/grn');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const onSubmit = async (data: GRNFormData) => {
    setError(null);
    const hasReceivedItems = data.items.some(item => item.receivedQuantity > 0);
    if (!hasReceivedItems) {
      setError('At least one item must have received quantity > 0');
      return;
    }
    createMutation.mutate(data);
  };

  const pos = posResponse?.content || [];

  return (
    <div>
      <PageHeader
        title="Create Goods Receipt Note"
        subtitle="Record materials received from vendor"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'GRN', path: '/grn' },
          { label: 'Create' },
        ]}
        actions={
          <Button variant="outline-secondary" onClick={() => navigate('/grn')}>
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
            {/* PO Selection */}
            <Card className="mb-4">
              <Card.Header>
                <h5 className="mb-0">Purchase Order</h5>
              </Card.Header>
              <Card.Body>
                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Select Purchase Order <span className="text-danger">*</span></Form.Label>
                      <Form.Select
                        value={selectedPoId || ''}
                        onChange={(e) => setSelectedPoId(Number(e.target.value) || null)}
                        isInvalid={!!errors.poId}
                      >
                        <option value="">Select PO...</option>
                        {pos.map((po: any) => (
                          <option key={po.id} value={po.id}>
                            {po.poNumber} - {po.vendorName}
                          </option>
                        ))}
                      </Form.Select>
                      <Form.Control.Feedback type="invalid">
                        {errors.poId?.message}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                </Row>

                {loadingPo && <LoadingSpinner text="Loading PO details..." />}

                {selectedPo && (
                  <div className="bg-light rounded p-3 mt-3">
                    <Row>
                      <Col sm={4}>
                        <small className="text-muted">PO Number</small>
                        <div className="fw-medium">{selectedPo.poNumber}</div>
                      </Col>
                      <Col sm={4}>
                        <small className="text-muted">Vendor</small>
                        <div className="fw-medium">{selectedPo.vendorName}</div>
                      </Col>
                      <Col sm={4}>
                        <small className="text-muted">PO Date</small>
                        <div className="fw-medium">
                          {format(new Date(selectedPo.poDate), 'PPP')}
                        </div>
                      </Col>
                    </Row>
                  </div>
                )}
              </Card.Body>
            </Card>

            {/* Delivery Details */}
            <Card className="mb-4">
              <Card.Header>
                <h5 className="mb-0">Delivery Details</h5>
              </Card.Header>
              <Card.Body>
                <Row>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>Challan Number <span className="text-danger">*</span></Form.Label>
                      <Form.Control
                        type="text"
                        {...register('challanNumber')}
                        isInvalid={!!errors.challanNumber}
                        placeholder="Enter challan number"
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.challanNumber?.message}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>Challan Date <span className="text-danger">*</span></Form.Label>
                      <Form.Control
                        type="date"
                        {...register('challanDate')}
                        isInvalid={!!errors.challanDate}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.challanDate?.message}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>Received Date <span className="text-danger">*</span></Form.Label>
                      <Form.Control
                        type="date"
                        {...register('receivedDate')}
                        isInvalid={!!errors.receivedDate}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.receivedDate?.message}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Vehicle Number</Form.Label>
                      <Form.Control
                        type="text"
                        {...register('vehicleNumber')}
                        placeholder="e.g., MH12AB1234"
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Transporter Name</Form.Label>
                      <Form.Control
                        type="text"
                        {...register('transporterName')}
                        placeholder="Enter transporter name"
                      />
                    </Form.Group>
                  </Col>
                  <Col xs={12}>
                    <Form.Group className="mb-3">
                      <Form.Label>Remarks</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={2}
                        {...register('remarks')}
                        placeholder="Any additional notes..."
                      />
                    </Form.Group>
                  </Col>
                </Row>
              </Card.Body>
            </Card>

            {/* Items */}
            {fields.length > 0 && (
              <Card className="mb-4">
                <Card.Header>
                  <h5 className="mb-0">Items to Receive</h5>
                </Card.Header>
                <Card.Body className="p-0">
                  <div className="table-responsive">
                    <Table className="mb-0">
                      <thead className="table-light">
                        <tr>
                          <th>Material</th>
                          <th className="text-center">Ordered</th>
                          <th className="text-center">Received</th>
                          <th className="text-center">Pending</th>
                          <th className="text-center">Receiving Now</th>
                          <th>Remarks</th>
                        </tr>
                      </thead>
                      <tbody>
                        {fields.map((field, index) => (
                          <tr key={field.id}>
                            <td>
                              <div className="fw-medium">{field.materialCode}</div>
                              <small className="text-muted">{field.materialDescription}</small>
                            </td>
                            <td className="text-center">{field.orderedQuantity}</td>
                            <td className="text-center">{field.previouslyReceived}</td>
                            <td className="text-center">
                              <Badge bg={field.pendingQuantity > 0 ? 'warning' : 'success'}>
                                {field.pendingQuantity}
                              </Badge>
                            </td>
                            <td>
                              <Form.Control
                                type="number"
                                min="0"
                                max={field.pendingQuantity}
                                {...register(`items.${index}.receivedQuantity`, { valueAsNumber: true })}
                                isInvalid={!!errors.items?.[index]?.receivedQuantity}
                                className="text-center"
                              />
                            </td>
                            <td>
                              <Form.Control
                                type="text"
                                {...register(`items.${index}.remarks`)}
                                placeholder="Remarks"
                                size="sm"
                              />
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                  </div>
                </Card.Body>
              </Card>
            )}
          </Col>

          <Col lg={4}>
            {/* Summary Card */}
            {selectedPo && (
              <Card className="mb-4">
                <Card.Header>
                  <h5 className="mb-0">Summary</h5>
                </Card.Header>
                <Card.Body>
                  <div className="mb-3">
                    <small className="text-muted d-block">Total Items</small>
                    <strong>{fields.length}</strong>
                  </div>
                  <div className="mb-3">
                    <small className="text-muted d-block">Items Being Received</small>
                    <strong>
                      {watch('items')?.filter(item => item.receivedQuantity > 0).length || 0}
                    </strong>
                  </div>
                  <div>
                    <small className="text-muted d-block">Total Quantity Receiving</small>
                    <strong>
                      {watch('items')?.reduce((sum, item) => sum + (item.receivedQuantity || 0), 0) || 0}
                    </strong>
                  </div>
                </Card.Body>
              </Card>
            )}

            {/* Actions Card */}
            <Card>
              <Card.Body>
                <div className="d-grid gap-2">
                  <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    disabled={isSubmitting || createMutation.isPending || !selectedPoId || fields.length === 0}
                  >
                    {(isSubmitting || createMutation.isPending) ? (
                      <>
                        <Spinner as="span" animation="border" size="sm" className="me-2" />
                        Creating GRN...
                      </>
                    ) : (
                      <>
                        <FaSave className="me-2" />
                        Create GRN
                      </>
                    )}
                  </Button>
                  <Button
                    type="button"
                    variant="outline-secondary"
                    onClick={() => navigate('/grn')}
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

export default GRNFormPage;
