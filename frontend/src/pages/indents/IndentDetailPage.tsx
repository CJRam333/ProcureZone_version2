import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Card,
  Row,
  Col,
  Table,
  Button,
  Badge,
  Form,
  Modal,
  Alert,
  Tab,
  Tabs,
  Spinner,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { format, isValid, parseISO } from 'date-fns';
import {
  FaArrowLeft,
  FaEdit,
  FaPaperPlane,
  FaCheck,
  FaTimes,
  FaPrint,
  FaHistory,
  FaFileAlt,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { indentsApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { INDENT_STATUS_COLORS } from '../../constants/indentStatus';

// Safe date formatter that handles null/undefined/invalid dates
const formatDate = (dateValue: string | Date | null | undefined, formatStr: string = 'dd MMM yyyy'): string => {
  if (!dateValue) return 'N/A';
  try {
    const date = typeof dateValue === 'string' ? parseISO(dateValue) : dateValue;
    return isValid(date) ? format(date, formatStr) : 'N/A';
  } catch {
    return 'N/A';
  }
};

// Status helpers based on backend IndentStatus IDs (1-based)
const STATUS_DRAFT = 1;
const STATUS_SUBMITTED = 2;
const STATUS_DEPT_HEAD_APPROVED = 3;
const STATUS_REJECTED = 4;           // ID 4 = Rejected (legacy DB compatible)
const STATUS_PROCUREMENT_APPROVED = 5;
// ID 6 = PO Created (legacy DB compatible — no constant needed beyond the label maps)

const getStatusLabel = (_statusId: number | null | undefined, displayStatus: string | null | undefined): string => {
  return displayStatus || 'Unknown';
};

const getStatusVariant = (_statusId: number | null | undefined, displayStatus?: string | null): string => {
  return (displayStatus ? INDENT_STATUS_COLORS[displayStatus] : undefined) ?? 'secondary';
};

const IndentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { hasAnyRole } = useAuth();

  const [showApproveModal, setShowApproveModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [approvalComments, setApprovalComments] = useState('');
  const [rejectionReason, setRejectionReason] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [procSubStatus, setProcSubStatus] = useState<number>(5);
  const [procPoNumber, setProcPoNumber] = useState('');
  const [procDeliveryDate, setProcDeliveryDate] = useState('');
  const [procRemarks, setProcRemarks] = useState('');

  // Validate id parameter
  const numericId = id ? Number(id) : Number.NaN;
  const isValidId = !Number.isNaN(numericId) && numericId > 0;

  // Fetch indent details
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const { data: indent, isLoading, error: fetchError } = useQuery<any>({
    queryKey: ['indent', id],
    queryFn: () => indentsApi.getById(numericId),
    enabled: isValidId,
  });

  // Mutations
  const submitMutation = useMutation({
    mutationFn: () => indentsApi.submit(numericId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['indent', id] });
      queryClient.invalidateQueries({ queryKey: ['indents'] });
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const approveMutation = useMutation({
    mutationFn: () => indentsApi.approve(numericId, { remarks: approvalComments }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['indent', id] });
      queryClient.invalidateQueries({ queryKey: ['indents'] });
      setShowApproveModal(false);
      setApprovalComments('');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const rejectMutation = useMutation({
    mutationFn: () => indentsApi.reject(numericId, { reason: rejectionReason }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['indent', id] });
      queryClient.invalidateQueries({ queryKey: ['indents'] });
      setShowRejectModal(false);
      setRejectionReason('');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const procurementUpdateMutation = useMutation({
    mutationFn: () => indentsApi.procurementUpdate(numericId, {
      procurementSubStatus: procSubStatus,
      ...(procSubStatus === 7 && { poNumber: procPoNumber, deliveryDate: procDeliveryDate }),
      ...(procSubStatus === 8 && { remarks: procRemarks }),
      ...(procSubStatus === 9 && { deliveryDate: procDeliveryDate }),
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['indent', id] });
      queryClient.invalidateQueries({ queryKey: ['indents'] });
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  if (isLoading) {
    return <LoadingSpinner fullPage text="Loading indent details..." />;
  }

  if (!isValidId || fetchError || !indent) {
    let errorMessage = 'Indent not found';
    if (!isValidId) errorMessage = 'Invalid indent ID';
    else if (fetchError) errorMessage = getErrorMessage(fetchError);

    return (
      <div className="text-center py-5">
        <Alert variant="danger">{errorMessage}</Alert>
        <Button variant="primary" onClick={() => navigate('/indents')}>
          Back to Indents
        </Button>
      </div>
    );
  }

  // Map backend fields to usable variables (handle both naming conventions)
  const statusId = indent.statusId ?? indent.status;
  const displayStatus = indent.displayStatus ?? indent.statusName ?? '';
  const employeeName = indent.employeeName ?? indent.requestedByName ?? 'Unknown';
  const createdDate = indent.indentDate ?? indent.createdAt;
  const deliveryDate = indent.deliveryDate ?? indent.requiredDate;
  const comments = indent.comments ?? indent.purpose ?? '';
  const remarks = indent.remarks ?? '';
  const companyName = indent.companyName ?? '';
  const departmentName = indent.departmentName ?? '';
  const plantName = indent.plantName ?? '';
  const sectionName = indent.sectionName ?? '';
  const approvedByName = indent.approvedByName ?? '';
  const approvedByDate = indent.approvedByDate ?? indent.approvedAt;
  const finalApprovedByName = indent.finalApprovedByName ?? '';
  const finalApprovedDate = indent.finalApprovedDate ?? '';

  // Get details array — backend IndentResponse uses 'details' (canonical)
  const details = indent.details ?? [];

  // Calculate total value from details
  const totalValue = details.reduce(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (sum: number, item: any) => {
      const qty = item.quantity ?? item.requestedQuantity ?? 0;
      const rate = item.pricing ?? item.estimatedRate ?? 0;
      return sum + (Number(qty) * Number(rate));
    },
    0
  );

  // Three-column workflow state from backend (IndentResponse exposes these directly)
  const approvedStatusId: number = (indent.approvedStatusId as number | null | undefined) ?? 1;
  const finalStatusId: number = (indent.finalStatusId as number | null | undefined) ?? 1;
  const procurementStatusIdVal: number = (indent.procurementStatusId as number | null | undefined) ?? 1;

  // L1: submitted, awaiting RM review (approvedStatus=1)
  const awaitingL1 = statusId >= STATUS_SUBMITTED && approvedStatusId === 1;
  // L2: RM approved, awaiting Dept Head (approvedStatus=3, finalStatus=1)
  const awaitingL2 = approvedStatusId === 3 && finalStatusId === 1;
  // Procurement: Dept Head approved (finalStatus=4, procurementStatus=4)
  const awaitingProcurement = finalStatusId === 4 && procurementStatusIdVal === 4;

  // Permissions — multi-stage approval
  const canEdit = statusId === STATUS_DRAFT && hasAnyRole(['SUPERADMIN', 'ADMIN', 'USER', 'DEPTHEAD', 'PLANTMANAGER', 'SUPERVISOR']);
  const canSubmit = statusId === STATUS_DRAFT && hasAnyRole(['SUPERADMIN', 'ADMIN', 'USER', 'DEPTHEAD', 'PLANTMANAGER', 'SUPERVISOR']);

  // Approve permission gated on EXACT workflow stage so each role sees the button only when it's their turn:
  // L1 (RM Review):      SUPERVISOR acts when approvedStatus=1
  // L2 (Dept Head):      DEPTHEAD/PLANTMANAGER act when approvedStatus=3 and finalStatus=1
  // Procurement:         PROCUREMENT acts when finalStatus=4 and procurementStatus=4
  const canApprove =
    (awaitingL1 && hasAnyRole(['SUPERADMIN', 'ADMIN', 'SUPERVISOR'])) ||
    (awaitingL2 && hasAnyRole(['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'PLANTMANAGER'])) ||
    (awaitingProcurement && hasAnyRole(['SUPERADMIN', 'ADMIN', 'PROCUREMENT']));

  // Dynamic approve button label — describes the workflow stage
  const approveLabel = awaitingL1 ? 'Approve (RM Review)'
    : awaitingL2 ? 'Approve (Dept Head)'
    : awaitingProcurement ? 'Procurement Approve'
    : 'Approve';

  const isProcurementStage = finalStatusId === 4 && procurementStatusIdVal >= 4 && procurementStatusIdVal <= 9;
  const canUpdateProcurement = isProcurementStage && hasAnyRole(['SUPERADMIN', 'ADMIN', 'PROCUREMENT']);

  return (
    <div>
      <PageHeader
        title={`Indent ${indent.indentNumber}`}
        subtitle={`Requested by ${employeeName} on ${formatDate(createdDate)}`}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Indents', path: '/indents' },
          { label: indent.indentNumber },
        ]}
        actions={
          <div className="d-flex flex-wrap gap-2">
            <Button variant="outline-secondary" onClick={() => navigate('/indents')}>
              <FaArrowLeft className="me-2" /> Back
            </Button>
            <Button variant="outline-secondary" onClick={() => window.print()}>
              <FaPrint className="me-2" /> Print
            </Button>
            {canEdit && (
              <Button variant="primary" onClick={() => navigate(`/indents/${id}/edit`)}>
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
          <div className="d-flex flex-wrap gap-3 align-items-center">
            <div>
              <small className="text-muted d-block">Status</small>
              <Badge bg={getStatusVariant(statusId, displayStatus)} className="fs-6">
                {getStatusLabel(statusId, displayStatus)}
              </Badge>
            </div>
            <div className="vr d-none d-sm-block" />
            <div>
              <small className="text-muted d-block">Items</small>
              <strong>{details.length}</strong>
            </div>
          </div>

          <div className="d-flex flex-wrap gap-2">
            {canSubmit && (
              <Button
                variant="warning"
                onClick={() => submitMutation.mutate()}
                disabled={submitMutation.isPending}
              >
                {submitMutation.isPending ? (
                  <Spinner as="span" animation="border" size="sm" className="me-2" />
                ) : (
                  <FaPaperPlane className="me-2" />
                )}
                Submit for Approval
              </Button>
            )}
            {canApprove && (
              <>
                <Button variant="success" onClick={() => setShowApproveModal(true)}>
                  <FaCheck className="me-2" /> {approveLabel}
                </Button>
                <Button variant="danger" onClick={() => setShowRejectModal(true)}>
                  <FaTimes className="me-2" /> Reject
                </Button>
              </>
            )}
          </div>
        </Card.Body>
      </Card>

      {canUpdateProcurement && (
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Procurement Status Update</h5>
          </Card.Header>
          <Card.Body>
            <Row className="g-3 align-items-end">
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Status</Form.Label>
                  <Form.Select value={procSubStatus} onChange={(e) => { setProcSubStatus(Number(e.target.value)); setProcPoNumber(''); setProcDeliveryDate(''); setProcRemarks(''); }}>
                    <option value={5}>Quotations Collected</option>
                    <option value={6}>Negotiation Done</option>
                    <option value={7}>PO Released</option>
                    <option value={8}>Hold</option>
                    <option value={9}>Cash Buy</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              {procSubStatus === 7 && (
                <>
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>PO Number</Form.Label>
                      <Form.Control value={procPoNumber} onChange={(e) => setProcPoNumber(e.target.value)} placeholder="PO Number" />
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>Delivery Date</Form.Label>
                      <Form.Control type="date" value={procDeliveryDate} onChange={(e) => setProcDeliveryDate(e.target.value)} />
                    </Form.Group>
                  </Col>
                </>
              )}
              {procSubStatus === 8 && (
                <Col md={5}>
                  <Form.Group>
                    <Form.Label>Remarks</Form.Label>
                    <Form.Control value={procRemarks} onChange={(e) => setProcRemarks(e.target.value)} placeholder="Reason for hold..." />
                  </Form.Group>
                </Col>
              )}
              {procSubStatus === 9 && (
                <Col md={3}>
                  <Form.Group>
                    <Form.Label>Delivery Date</Form.Label>
                    <Form.Control type="date" value={procDeliveryDate} onChange={(e) => setProcDeliveryDate(e.target.value)} />
                  </Form.Group>
                </Col>
              )}
              <Col md="auto">
                <Button
                  variant="primary"
                  onClick={() => procurementUpdateMutation.mutate()}
                  disabled={procurementUpdateMutation.isPending}
                >
                  {procurementUpdateMutation.isPending && <Spinner as="span" animation="border" size="sm" className="me-2" />}
                  Update Status
                </Button>
              </Col>
            </Row>
          </Card.Body>
        </Card>
      )}

      <Tabs defaultActiveKey="details" className="mb-4">
        {/* Details Tab */}
        <Tab eventKey="details" title={<><FaFileAlt className="me-2" />Details</>}>
          <Row className="g-4">
            {/* Basic Info */}
            <Col lg={8}>
              <Card className="h-100">
                <Card.Header>
                  <h5 className="mb-0">Indent Information</h5>
                </Card.Header>
                <Card.Body>
                  <Row className="g-3">
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Indent Number</small>
                        <strong>{indent.indentNumber}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Indent Date</small>
                        <strong>{formatDate(createdDate)}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Company</small>
                        <strong>{companyName || 'N/A'}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Department</small>
                        <strong>{departmentName || 'N/A'}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Plant</small>
                        <strong>{plantName || 'N/A'}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Section</small>
                        <strong>{sectionName || 'N/A'}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Requested By</small>
                        <strong>{employeeName}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Delivery Date</small>
                        <strong>{deliveryDate ? formatDate(deliveryDate) : 'Not specified'}</strong>
                      </div>
                    </Col>
                    {comments && (
                      <Col sm={12}>
                        <div className="mb-3">
                          <small className="text-muted d-block">Comments</small>
                          <p className="mb-0">{comments}</p>
                        </div>
                      </Col>
                    )}
                    {remarks && (
                      <Col sm={12}>
                        <div>
                          <small className="text-muted d-block">Remarks</small>
                          <p className="mb-0">{remarks}</p>
                        </div>
                      </Col>
                    )}
                  </Row>
                </Card.Body>
              </Card>
            </Col>

            {/* Approval Info */}
            <Col lg={4}>
              <Card className="h-100">
                <Card.Header>
                  <h5 className="mb-0">Approval Status</h5>
                </Card.Header>
                <Card.Body>
                  <div className="approval-timeline">
                    {/* L1 - Submitted */}
                    <div className="d-flex mb-3">
                      <div
                        className={`rounded-circle me-3 d-flex align-items-center justify-content-center ${
                          statusId >= STATUS_SUBMITTED ? 'bg-success' : 'bg-secondary'
                        }`}
                        style={{ width: 32, height: 32, minWidth: 32 }}
                      >
                        {statusId >= STATUS_SUBMITTED ? <FaCheck className="text-white" /> : <span className="text-white">1</span>}
                      </div>
                      <div className="flex-grow-1">
                        <strong>Submitted</strong>
                        {statusId === STATUS_DRAFT && <div className="small text-muted">Not yet submitted</div>}
                        {statusId >= STATUS_SUBMITTED && <div className="small text-success">Submitted</div>}
                      </div>
                    </div>

                    {/* L2 - Dept Head Approval */}
                    <div className="d-flex mb-3">
                      <div
                        className={`rounded-circle me-3 d-flex align-items-center justify-content-center ${
                          statusId >= STATUS_DEPT_HEAD_APPROVED && statusId !== STATUS_REJECTED
                            ? 'bg-success'
                            : statusId === STATUS_SUBMITTED
                            ? 'bg-warning'
                            : 'bg-secondary'
                        }`}
                        style={{ width: 32, height: 32, minWidth: 32 }}
                      >
                        {statusId >= STATUS_DEPT_HEAD_APPROVED && statusId !== STATUS_REJECTED
                          ? <FaCheck className="text-white" />
                          : <span className="text-white">2</span>}
                      </div>
                      <div className="flex-grow-1">
                        <strong>Dept Head Approval</strong>
                        {statusId === STATUS_SUBMITTED && <div className="small text-warning">Pending</div>}
                        {statusId >= STATUS_DEPT_HEAD_APPROVED && statusId !== STATUS_REJECTED && approvedByName && (
                          <div className="small text-muted">
                            {approvedByName}
                            {approvedByDate && ` on ${formatDate(approvedByDate, 'dd MMM yyyy HH:mm')}`}
                          </div>
                        )}
                      </div>
                    </div>

                    {/* L3 - Final Approval */}
                    <div className="d-flex mb-3">
                      <div
                        className={`rounded-circle me-3 d-flex align-items-center justify-content-center ${
                          statusId >= STATUS_PROCUREMENT_APPROVED && statusId !== STATUS_REJECTED
                            ? 'bg-success'
                            : statusId >= STATUS_DEPT_HEAD_APPROVED && statusId < STATUS_REJECTED
                            ? 'bg-warning'
                            : 'bg-secondary'
                        }`}
                        style={{ width: 32, height: 32, minWidth: 32 }}
                      >
                        {statusId >= STATUS_PROCUREMENT_APPROVED && statusId !== STATUS_REJECTED
                          ? <FaCheck className="text-white" />
                          : <span className="text-white">3</span>}
                      </div>
                      <div className="flex-grow-1">
                        <strong>Final Approval</strong>
                        {statusId >= STATUS_DEPT_HEAD_APPROVED && statusId < STATUS_PROCUREMENT_APPROVED && statusId !== STATUS_REJECTED && (
                          <div className="small text-warning">Pending</div>
                        )}
                        {finalApprovedByName && (
                          <div className="small text-muted">
                            {finalApprovedByName}
                            {finalApprovedDate && ` on ${formatDate(finalApprovedDate, 'dd MMM yyyy HH:mm')}`}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {statusId === STATUS_REJECTED && (
                    <Alert variant="danger" className="mt-3 mb-0">
                      <strong>Rejected</strong>
                      {remarks && <div className="mt-2">{remarks}</div>}
                    </Alert>
                  )}
                </Card.Body>
              </Card>
            </Col>

            {/* Items Table */}
            <Col xs={12}>
              <Card>
                <Card.Header>
                  <h5 className="mb-0">Items ({details.length})</h5>
                </Card.Header>
                <Card.Body className="p-0">
                  <div className="table-responsive">
                    <Table className="mb-0">
                      <thead className="bg-light">
                        <tr>
                          <th>#</th>
                          <th>Material Code</th>
                          <th>Description</th>
                          <th>UOM</th>
                          <th className="text-end">Qty</th>
                          {/* <th className="text-end">Est. Rate (₹)</th> */}
                          <th className="text-end">Est. Value (₹)</th>
                          <th>Remarks</th>
                        </tr>
                      </thead>
                      <tbody>
                        {details.length === 0 ? (
                          <tr>
                            <td colSpan={8} className="text-center text-muted py-4">
                              No items found
                            </td>
                          </tr>
                        ) : (
                          // eslint-disable-next-line @typescript-eslint/no-explicit-any
                          details.map((item: any, index: number) => {
                            const qty = Number(item.quantity ?? item.requestedQuantity ?? 0);
                            const rate = Number(item.pricing ?? item.estimatedRate ?? 0);
                            return (
                              <tr key={item.id || index}>
                                <td>{index + 1}</td>
                                <td><code>{item.materialCode || 'N/A'}</code></td>
                                <td>{item.materialName || item.materialDescription || 'N/A'}</td>
                                <td><Badge bg="secondary">{item.unitOfMeasureCode || item.uomCode || 'N/A'}</Badge></td>
                                <td className="text-end">{qty}</td>
                                {/* <td className="text-end">{new Intl.NumberFormat('en-IN').format(rate)}</td> */}
                                <td className="text-end fw-medium">
                                  {new Intl.NumberFormat('en-IN').format(qty * rate)}
                                </td>
                                <td>{item.purpose || item.remarks || '-'}</td>
                              </tr>
                            );
                          })
                        )}
                      </tbody>
                    </Table>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Tab>

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
                      {employeeName} created this indent on {formatDate(createdDate, 'dd MMM yyyy HH:mm')}
                    </p>
                  </div>
                </div>
                {statusId >= STATUS_SUBMITTED && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-warning"></div>
                    <div className="timeline-content">
                      <strong>Submitted for Approval</strong>
                      <p className="text-muted mb-0">Awaiting department head approval</p>
                    </div>
                  </div>
                )}
                {statusId >= STATUS_DEPT_HEAD_APPROVED && statusId !== STATUS_REJECTED && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-success"></div>
                    <div className="timeline-content">
                      <strong>Dept Head Approved</strong>
                      {approvedByName && (
                        <p className="text-muted mb-0">
                          Approved by {approvedByName}
                          {approvedByDate && ` on ${formatDate(approvedByDate, 'dd MMM yyyy HH:mm')}`}
                        </p>
                      )}
                    </div>
                  </div>
                )}
                {statusId === STATUS_REJECTED && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-danger"></div>
                    <div className="timeline-content">
                      <strong>Rejected</strong>
                      {remarks && <p className="mt-1 mb-0"><em>Reason: {remarks}</em></p>}
                    </div>
                  </div>
                )}
              </div>
            </Card.Body>
          </Card>
        </Tab>
      </Tabs>

      {/* Approve Modal */}
      <Modal show={showApproveModal} onHide={() => setShowApproveModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Approve Indent</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Are you sure you want to approve indent <strong>{indent.indentNumber}</strong>?</p>
          <Form.Group>
            <Form.Label>Comments (Optional)</Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={approvalComments}
              onChange={(e) => setApprovalComments(e.target.value)}
              placeholder="Add any comments..."
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowApproveModal(false)}>Cancel</Button>
          <Button
            variant="success"
            onClick={() => approveMutation.mutate()}
            disabled={approveMutation.isPending}
          >
            {approveMutation.isPending && <Spinner as="span" animation="border" size="sm" className="me-2" />}
            Approve
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Reject Modal */}
      <Modal show={showRejectModal} onHide={() => setShowRejectModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Reject Indent</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Are you sure you want to reject indent <strong>{indent.indentNumber}</strong>?</p>
          <Form.Group>
            <Form.Label>Rejection Reason <span className="text-danger">*</span></Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              placeholder="Provide a reason for rejection..."
              required
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowRejectModal(false)}>Cancel</Button>
          <Button
            variant="danger"
            onClick={() => rejectMutation.mutate()}
            disabled={rejectMutation.isPending || !rejectionReason.trim()}
          >
            {rejectMutation.isPending && <Spinner as="span" animation="border" size="sm" className="me-2" />}
            Reject
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
          box-shadow: 0 0 0 2px currentColor;
        }
        .timeline-content {
          padding-left: 10px;
        }
      `}
      </style>
    </div>
  );
};

export default IndentDetailPage;
