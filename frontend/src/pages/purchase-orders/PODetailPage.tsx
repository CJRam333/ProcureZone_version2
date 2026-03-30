import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Card,
  Row,
  Col,
  Table,
  Button,
  Badge,
  Modal,
  Form,
  Alert,
  Tab,
  Tabs,
  Spinner,
  ProgressBar,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
  FaArrowLeft,
  FaEdit,
  FaCheck,
  FaTimes,
  FaPrint,
  FaHistory,
  FaFileAlt,
  FaTruck,
  FaBoxes,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { purchaseOrdersApi, getErrorMessage } from '../../api';
import { POStatus } from '../../api/purchaseOrders';
import { useAuth } from '../../contexts/AuthContext';

const PODetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { hasAnyRole } = useAuth();

  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [error, setError] = useState<string | null>(null);

  // Fetch PO details
  const { data: po, isLoading, error: fetchError } = useQuery({
    queryKey: ['purchase-order', id],
    queryFn: () => purchaseOrdersApi.getById(Number(id)),
    enabled: !!id,
  });

  // Mutations
  const confirmMutation = useMutation({
    mutationFn: () => purchaseOrdersApi.approve(Number(id)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['purchase-order', id] });
      setShowConfirmModal(false);
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const cancelMutation = useMutation({
    mutationFn: () => purchaseOrdersApi.cancel(Number(id), cancelReason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['purchase-order', id] });
      setShowCancelModal(false);
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // Permissions based on status (using backend POStatus: 1=Draft, 2=Submitted, 3=Approved, etc.)
  const canEdit = po?.status === POStatus.DRAFT && hasAnyRole(['SUPERADMIN', 'ADMIN', 'PROCUREMENT']);
  const canConfirm = po?.status === POStatus.SUBMITTED && hasAnyRole(['SUPERADMIN', 'ADMIN', 'DEPTHEAD']);
  const canCancel = po && po.status !== POStatus.CANCELLED && po.status !== POStatus.CLOSED && po.status !== POStatus.FULLY_RECEIVED && hasAnyRole(['SUPERADMIN', 'ADMIN']);
  const canCreateGRN = po && [POStatus.SENT_TO_VENDOR, POStatus.PARTIALLY_RECEIVED].includes(po.status) &&
    hasAnyRole(['SUPERADMIN', 'ADMIN', 'STOREKEEPER']);
  const canAmend = po && [POStatus.APPROVED, POStatus.SENT_TO_VENDOR, POStatus.PARTIALLY_RECEIVED].includes(po.status) &&
    hasAnyRole(['SUPERADMIN', 'ADMIN', 'PROCUREMENT']);

  // Status display mapping
  const getStatusDisplay = (status: POStatus): { text: string; variant: string } => {
    const statusMap: Record<POStatus, { text: string; variant: string }> = {
      [POStatus.DRAFT]: { text: 'Draft', variant: 'secondary' },
      [POStatus.SUBMITTED]: { text: 'Pending Approval', variant: 'warning' },
      [POStatus.APPROVED]: { text: 'Approved', variant: 'primary' },
      [POStatus.SENT_TO_VENDOR]: { text: 'Sent to Vendor', variant: 'info' },
      [POStatus.PARTIALLY_RECEIVED]: { text: 'Partially Received', variant: 'info' },
      [POStatus.FULLY_RECEIVED]: { text: 'Fully Received', variant: 'success' },
      [POStatus.CANCELLED]: { text: 'Cancelled', variant: 'danger' },
      [POStatus.CLOSED]: { text: 'Closed', variant: 'success' },
    };
    return statusMap[status] || { text: 'Unknown', variant: 'secondary' };
  };

  if (isLoading) {
    return <LoadingSpinner fullPage text="Loading purchase order..." />;
  }

  if (fetchError || !po) {
    return (
      <div className="text-center py-5">
        <Alert variant="danger">
          {fetchError ? getErrorMessage(fetchError) : 'Purchase order not found'}
        </Alert>
        <Button variant="primary" onClick={() => navigate('/purchase-orders')}>
          Back to Purchase Orders
        </Button>
      </div>
    );
  }

  const statusInfo = getStatusDisplay(po.status);

  // Calculate totals from items
  const subtotal = po.items.reduce(
    (sum, item) => sum + (item.orderedQuantity * item.unitRate),
    0
  );

  // Calculate delivery progress
  const totalOrdered = po.items.reduce((sum, item) => sum + item.orderedQuantity, 0);
  const totalReceived = po.items.reduce((sum, item) => sum + (item.receivedQuantity || 0), 0);
  const deliveryProgress = totalOrdered > 0 ? (totalReceived / totalOrdered) * 100 : 0;

  return (
    <div>
      <PageHeader
        title={`Purchase Order ${po.poNumber}`}
        subtitle={`Date: ${format(new Date(po.poDate), 'PPP')}`}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Purchase Orders', path: '/purchase-orders' },
          { label: po.poNumber },
        ]}
        actions={
          <div className="d-flex flex-wrap gap-2">
            <Button variant="outline-secondary" onClick={() => navigate('/purchase-orders')}>
              <FaArrowLeft className="me-2" /> Back
            </Button>
            <Button variant="outline-secondary" onClick={() => window.print()}>
              <FaPrint className="me-2" /> Print
            </Button>
            {canEdit && (
              <Button variant="primary" onClick={() => navigate(`/purchase-orders/${id}/edit`)}>
                <FaEdit className="me-2" /> Edit
              </Button>
            )}
          </div>
        }
      />

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Status and Actions Bar */}
      <Card className="mb-4">
        <Card.Body className="d-flex flex-wrap justify-content-between align-items-center gap-3">
          <div className="d-flex flex-wrap gap-4 align-items-center">
            <div>
              <small className="text-muted d-block">Status</small>
              <Badge bg={statusInfo.variant} className="fs-6">{statusInfo.text}</Badge>
            </div>
            <div className="vr d-none d-sm-block" />
            <div>
              <small className="text-muted d-block">Total Amount</small>
              <strong className="text-primary fs-5">
                ₹{new Intl.NumberFormat('en-IN').format(po.grandTotal)}
              </strong>
            </div>
            <div className="vr d-none d-sm-block" />
            <div style={{ minWidth: '150px' }}>
              <small className="text-muted d-block mb-1">Delivery Progress</small>
              <ProgressBar
                now={deliveryProgress}
                variant={deliveryProgress >= 100 ? 'success' : 'primary'}
                label={`${Math.round(deliveryProgress)}%`}
              />
            </div>
          </div>

          <div className="d-flex flex-wrap gap-2">
            {canConfirm && (
              <Button variant="success" onClick={() => setShowConfirmModal(true)}>
                <FaCheck className="me-2" /> Confirm PO
              </Button>
            )}
            {canAmend && (
              <Button variant="warning" onClick={() => navigate(`/purchase-orders/${id}/amend`)}>
                <FaEdit className="me-2" /> Amend PO
              </Button>
            )}
            {canCancel && (
              <Button variant="outline-danger" onClick={() => setShowCancelModal(true)}>
                <FaTimes className="me-2" /> Cancel
              </Button>
            )}
            {canCreateGRN && (
              <Button variant="primary" onClick={() => navigate(`/grn/new?poId=${po.id}`)}>
                <FaTruck className="me-2" /> Create GRN
              </Button>
            )}
          </div>
        </Card.Body>
      </Card>

      <Tabs defaultActiveKey="details" className="mb-4">
        {/* Details Tab */}
        <Tab eventKey="details" title={<><FaFileAlt className="me-2" />Details</>}>
          <Row className="g-4">
            {/* PO Info */}
            <Col lg={6}>
              <Card className="h-100">
                <Card.Header>
                  <h5 className="mb-0">PO Information</h5>
                </Card.Header>
                <Card.Body>
                  <Row className="g-3">
                    <Col sm={6}>
                      <small className="text-muted d-block">PO Number</small>
                      <strong>{po.poNumber}</strong>
                    </Col>
                    <Col sm={6}>
                      <small className="text-muted d-block">PO Date</small>
                      <strong>{format(new Date(po.poDate), 'PPP')}</strong>
                    </Col>
                    <Col sm={6}>
                      <small className="text-muted d-block">Delivery Date</small>
                      <strong>
                        {po.deliveryDate ? format(new Date(po.deliveryDate), 'PPP') : 'Not specified'}
                      </strong>
                    </Col>
                    <Col sm={6}>
                      <small className="text-muted d-block">Payment Terms</small>
                      <strong>{po.paymentTerms || 'Not specified'}</strong>
                    </Col>
                    <Col sm={6}>
                      <small className="text-muted d-block">Delivery Terms</small>
                      <strong>{po.deliveryTerms || 'Not specified'}</strong>
                    </Col>
                    <Col sm={6}>
                      <small className="text-muted d-block">Plant</small>
                      <strong>{po.plantName}</strong>
                    </Col>
                    {po.indentNumber && po.indentId && (
                      <Col sm={12}>
                        <small className="text-muted d-block">Indent Reference</small>
                        <Button
                          variant="link"
                          className="p-0"
                          onClick={() => navigate(`/indents/${po.indentId}`)}
                        >
                          {po.indentNumber}
                        </Button>
                      </Col>
                    )}
                    {po.remarks && (
                      <Col sm={12}>
                        <small className="text-muted d-block">Remarks</small>
                        <p className="mb-0">{po.remarks}</p>
                      </Col>
                    )}
                  </Row>
                </Card.Body>
              </Card>
            </Col>

            {/* Vendor Info */}
            <Col lg={6}>
              <Card className="h-100">
                <Card.Header>
                  <h5 className="mb-0">Vendor Information</h5>
                </Card.Header>
                <Card.Body>
                  <Row className="g-3">
                    <Col sm={6}>
                      <small className="text-muted d-block">Vendor Name</small>
                      <strong>{po.vendorName}</strong>
                    </Col>
                    <Col sm={6}>
                      <small className="text-muted d-block">Vendor Code</small>
                      <code>{po.vendorCode}</code>
                    </Col>
                    <Col sm={12}>
                      <Button
                        variant="outline-primary"
                        size="sm"
                        onClick={() => navigate(`/vendors/${po.vendorId}`)}
                      >
                        View Vendor Details
                      </Button>
                    </Col>
                  </Row>
                </Card.Body>
              </Card>
            </Col>

            {/* Items */}
            <Col xs={12}>
              <Card>
                <Card.Header>
                  <h5 className="mb-0">Items ({po.items.length})</h5>
                </Card.Header>
                <Card.Body className="p-0">
                  <div className="table-responsive">
                    <Table className="mb-0">
                      <thead className="bg-light">
                        <tr>
                          <th>#</th>
                          <th>Material</th>
                          <th>UOM</th>
                          <th className="text-end">Ordered</th>
                          <th className="text-end">Received</th>
                          <th className="text-end">Pending</th>
                          <th className="text-end">Rate (₹)</th>
                          <th className="text-end">Amount (₹)</th>
                          <th>Progress</th>
                        </tr>
                      </thead>
                      <tbody>
                        {po.items.map((item, index) => {
                          const itemProgress = item.orderedQuantity > 0
                            ? ((item.receivedQuantity || 0) / item.orderedQuantity) * 100
                            : 0;
                          return (
                            <tr key={item.id}>
                              <td>{index + 1}</td>
                              <td>
                                <div className="fw-medium">{item.materialCode}</div>
                                <small className="text-muted">{item.materialDescription}</small>
                              </td>
                              <td>
                                <Badge bg="secondary">{item.uomCode}</Badge>
                              </td>
                              <td className="text-end">{item.orderedQuantity}</td>
                              <td className="text-end">
                                <span className={item.receivedQuantity >= item.orderedQuantity ? 'text-success' : ''}>
                                  {item.receivedQuantity || 0}
                                </span>
                              </td>
                              <td className="text-end text-warning">{item.pendingQuantity || 0}</td>
                              <td className="text-end">
                                {new Intl.NumberFormat('en-IN').format(item.unitRate)}
                              </td>
                              <td className="text-end fw-medium">
                                {new Intl.NumberFormat('en-IN').format(item.totalAmount)}
                              </td>
                              <td style={{ width: '100px' }}>
                                <ProgressBar
                                  now={itemProgress}
                                  variant={itemProgress >= 100 ? 'success' : 'primary'}
                                  style={{ height: '8px' }}
                                />
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                      <tfoot className="bg-light">
                        <tr>
                          <td colSpan={7} className="text-end">Subtotal:</td>
                          <td className="text-end fw-medium">
                            {new Intl.NumberFormat('en-IN').format(subtotal)}
                          </td>
                          <td></td>
                        </tr>
                        <tr>
                          <td colSpan={7} className="text-end">Tax:</td>
                          <td className="text-end">
                            {new Intl.NumberFormat('en-IN').format(po.taxAmount)}
                          </td>
                          <td></td>
                        </tr>
                        <tr className="fw-bold">
                          <td colSpan={7} className="text-end">Grand Total:</td>
                          <td className="text-end text-primary">
                            ₹{new Intl.NumberFormat('en-IN').format(po.grandTotal)}
                          </td>
                          <td></td>
                        </tr>
                      </tfoot>
                    </Table>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Tab>

        {/* Amendments Tab */}
        {po.amendments && po.amendments.length > 0 && (
          <Tab eventKey="amendments" title={<><FaBoxes className="me-2" />Amendments</>}>
            <Card>
              <Card.Body className="p-0">
                <Table responsive className="mb-0">
                  <thead className="bg-light">
                    <tr>
                      <th>Version</th>
                      <th>Field</th>
                      <th>Original Value</th>
                      <th>New Value</th>
                      <th>Reason</th>
                      <th>Amended By</th>
                      <th>Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {po.amendments.map((amendment) => (
                      <tr key={amendment.id}>
                        <td><Badge bg="info">V{amendment.amendmentVersion}</Badge></td>
                        <td><strong>{amendment.fieldName}</strong></td>
                        <td className="text-muted">{amendment.originalValue}</td>
                        <td className="text-primary">{amendment.amendedValue}</td>
                        <td>{amendment.amendmentReason}</td>
                        <td>{amendment.amendedByName}</td>
                        <td>{format(new Date(amendment.amendedAt), 'PPP')}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Tab>
        )}

        {/* History Tab */}
        <Tab eventKey="history" title={<><FaHistory className="me-2" />History</>}>
          <Card>
            <Card.Body>
              <div className="timeline">
                <div className="timeline-item">
                  <div className="timeline-marker bg-primary"></div>
                  <div className="timeline-content">
                    <strong>Created</strong>
                    <p className="text-muted mb-0">
                      PO created on {format(new Date(po.createdAt), 'PPpp')}
                    </p>
                  </div>
                </div>
                {po.confirmedAt && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-success"></div>
                    <div className="timeline-content">
                      <strong>Confirmed</strong>
                      <p className="text-muted mb-0">
                        Confirmed by {po.confirmedByName} on {format(new Date(po.confirmedAt), 'PPpp')}
                      </p>
                    </div>
                  </div>
                )}
                {po.updatedAt !== po.createdAt && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-info"></div>
                    <div className="timeline-content">
                      <strong>Last Updated</strong>
                      <p className="text-muted mb-0">
                        Modified on {format(new Date(po.updatedAt), 'PPpp')}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </Card.Body>
          </Card>
        </Tab>
      </Tabs>

      {/* Confirm Modal */}
      <Modal show={showConfirmModal} onHide={() => setShowConfirmModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Confirm Purchase Order</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Are you sure you want to confirm PO <strong>{po.poNumber}</strong>?</p>
          <p className="text-muted">
            Total Amount: <strong>₹{new Intl.NumberFormat('en-IN').format(po.grandTotal)}</strong>
          </p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowConfirmModal(false)}>
            Cancel
          </Button>
          <Button
            variant="success"
            onClick={() => confirmMutation.mutate()}
            disabled={confirmMutation.isPending}
          >
            {confirmMutation.isPending && (
              <Spinner as="span" animation="border" size="sm" className="me-2" />
            )}
            Confirm PO
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Cancel Modal */}
      <Modal show={showCancelModal} onHide={() => setShowCancelModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Cancel Purchase Order</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form.Group>
            <Form.Label>Cancellation Reason <span className="text-danger">*</span></Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              required
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowCancelModal(false)}>
            Close
          </Button>
          <Button
            variant="danger"
            onClick={() => cancelMutation.mutate()}
            disabled={cancelMutation.isPending || !cancelReason.trim()}
          >
            {cancelMutation.isPending && (
              <Spinner as="span" animation="border" size="sm" className="me-2" />
            )}
            Cancel PO
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Timeline Styles */}
      <style>{`
        .timeline {
          position: relative;
          padding-left: 30px;
        }
        .timeline::before {
          content: '';
          position: absolute;
          left: 8px;
          top: 0;
          bottom: 0;
          width: 2px;
          background: #dee2e6;
        }
        .timeline-item {
          position: relative;
          padding-bottom: 20px;
        }
        .timeline-marker {
          position: absolute;
          left: -26px;
          width: 14px;
          height: 14px;
          border-radius: 50%;
          border: 2px solid #fff;
        }
        .timeline-content {
          padding-left: 10px;
        }
      `}</style>
    </div>
  );
};

export default PODetailPage;
