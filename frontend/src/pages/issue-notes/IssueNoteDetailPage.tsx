import React, { useState, useEffect } from 'react';
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
  OverlayTrigger,
  Popover,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
  FaArrowLeft,
  FaEdit,
  FaPaperPlane,
  FaCheck,
  FaTimes,
  FaPrint,
  FaFileAlt,
  FaBoxOpen,
  FaHistory,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { issueNotesApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { INDENT_STATUS_COLORS } from '../../constants/indentStatus';

// Friendly labels for quantity-history stage codes (issue notes only carry 'RM').
const stageLabel = (stage: string | undefined): string => {
  const s = (stage ?? '').toUpperCase();
  if (s === 'RM') return 'RM';
  if (s === 'DEPTHEAD' || s === 'DEPT_HEAD') return 'Dept. Head';
  return stage ?? '';
};

const IssueNoteDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { hasAnyRole } = useAuth();

  const [showApproveModal, setShowApproveModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [showIssueModal, setShowIssueModal] = useState(false);
  const [showStoresRejectModal, setShowStoresRejectModal] = useState(false);
  const [approvalComments, setApprovalComments] = useState('');
  const [rejectionReason, setRejectionReason] = useState('');
  const [issueRemarks, setIssueRemarks] = useState('');
  const [storesRejectReason, setStoresRejectReason] = useState('');
  const [error, setError] = useState<string | null>(null);
  // Per-line editable quantity during the RM's approval turn, keyed by detail id.
  const [qtyEdits, setQtyEdits] = useState<Record<number, number>>({});

  // Fetch issue note details
  const { data: issueNote, isLoading, error: fetchError } = useQuery({
    queryKey: ['issue-note', id],
    queryFn: () => issueNotesApi.getById(Number(id)),
    enabled: !!id,
  });

  // Seed the editable-quantity map from each line's current effective quantity.
  useEffect(() => {
    const list = issueNote?.details ?? [];
    if (list.length > 0) {
      const init: Record<number, number> = {};
      list.forEach((item) => {
        init[item.id] = Number(item.currentEffectiveQuantity ?? item.quantity ?? 0);
      });
      setQtyEdits(init);
    }
  }, [issueNote]);

  // Mutations
  const submitMutation = useMutation({
    mutationFn: () => issueNotesApi.submit(Number(id)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-note', id] });
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // RM approval — the only approval stage (flow: User → RM → Stores)
  const rmApproveMutation = useMutation({
    mutationFn: () => {
      // rmApprove is only reachable on the RM turn, so always send every line's quantity;
      // the backend audits only the values that actually changed.
      const list = issueNote?.details ?? [];
      const items = list.map((item) => ({
        detailId: item.id,
        rmQuantity: qtyEdits[item.id] ?? Number(item.currentEffectiveQuantity ?? item.quantity ?? 0),
      }));
      return issueNotesApi.rmApprove(Number(id), { remarks: approvalComments, items });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-note', id] });
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
      setShowApproveModal(false);
      setApprovalComments('');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const rmRejectMutation = useMutation({
    mutationFn: () => issueNotesApi.rmReject(Number(id), { reason: rejectionReason }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-note', id] });
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
      setShowRejectModal(false);
      setRejectionReason('');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const issueMutation = useMutation({
    mutationFn: () => issueNotesApi.issue(Number(id), { remarks: issueRemarks }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-note', id] });
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
      setShowIssueModal(false);
      setIssueRemarks('');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const storesRejectMutation = useMutation({
    mutationFn: () => issueNotesApi.storesReject(Number(id), { reason: storesRejectReason }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-note', id] });
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
      setShowStoresRejectModal(false);
      setStoresRejectReason('');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // Safe date formatter
  const formatDate = (dateStr: string | null | undefined): string => {
    if (!dateStr) return 'N/A';
    try {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return 'N/A';
      return format(date, 'dd MMM yyyy');
    } catch {
      return 'N/A';
    }
  };

  if (isLoading) {
    return <LoadingSpinner fullPage text="Loading issue note details..." />;
  }

  if (fetchError || !issueNote) {
    return (
      <div className="text-center py-5">
        <Alert variant="danger">
          {fetchError ? getErrorMessage(fetchError) : 'Issue note not found'}
        </Alert>
        <Button variant="primary" onClick={() => navigate('/issue-notes')}>
          Back to Issue Notes
        </Button>
      </div>
    );
  }

  // Two-column workflow state (User → RM → Stores; no Dept Head stage):
  // approvedStatus: 1=pending RM, 2=RM rejected, 3=RM approved
  // storesByStatus: 1=pending stores, 2=stores rejected, 11=goods issued
  const approvedStatusId = issueNote.approvedStatus ?? 1;
  const storesByStatusId = issueNote.storesByStatus ?? 1;
  const displayStatus = issueNote.displayStatus ?? issueNote.statusDescription ?? 'Unknown';

  // True unsubmitted draft: Spring status=1 (Draft) AND no workflow progress in the
  // two-column model. Migrated legacy rows also carry status=1 (legacy active flag),
  // so the two-column check hides Edit/Submit on legacy rows already in the workflow.
  const isTrueDraft = issueNote.status === 1 && approvedStatusId === 1 && storesByStatusId === 1;

  const canEdit = isTrueDraft && hasAnyRole(['SUPERADMIN', 'ADMIN', 'USER', 'ISSUECONFIRM', 'SUPERVISOR']);
  const canSubmit = isTrueDraft && hasAnyRole(['SUPERADMIN', 'ADMIN', 'USER', 'ISSUECONFIRM', 'SUPERVISOR']);
  // RM stage: submitted (status=2) and RM decision still pending
  const canRmAct = issueNote.status === 2 && approvedStatusId === 1
    && hasAnyRole(['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'SUPERVISOR']);
  // Stores stage: RM approved, stores action pending. PROCUREMENT fills the stores role in this
  // deployment, so it must see the Goods Issued / Reject (Stores) buttons alongside ISSUECONFIRM.
  const canIssue = approvedStatusId === 3 && storesByStatusId === 1
    && hasAnyRole(['SUPERADMIN', 'ADMIN', 'ISSUECONFIRM', 'PROCUREMENT']);

  const details = issueNote.details ?? [];

  return (
    <div>
      <PageHeader
        title={`Issue Note ${issueNote.issueNoteNumber || 'N/A'}`}
        subtitle={`Requested by ${issueNote.employeeName || 'N/A'} on ${formatDate(issueNote.issueDate)}`}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Issue Notes', path: '/issue-notes' },
          { label: issueNote.issueNoteNumber || 'Detail' },
        ]}
        actions={
          <div className="d-flex flex-wrap gap-2">
            <Button variant="outline-secondary" onClick={() => navigate('/issue-notes')}>
              <FaArrowLeft className="me-2" /> Back
            </Button>
            <Button variant="outline-secondary" onClick={() => window.print()}>
              <FaPrint className="me-2" /> Print
            </Button>
            {canEdit && (
              <Button variant="primary" onClick={() => navigate(`/issue-notes/${id}/edit`)}>
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
              <Badge bg={INDENT_STATUS_COLORS[displayStatus] || 'secondary'} className="fs-6">
                {displayStatus}
              </Badge>
            </div>

            <div className="vr d-none d-sm-block" />
            <div>
              <small className="text-muted d-block">Total Items</small>
              <strong className="text-primary fs-5">{details.length}</strong>
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
            {canRmAct && (
              <>
                <Button variant="success" onClick={() => setShowApproveModal(true)}>
                  <FaCheck className="me-2" /> Approve (RM Review)
                </Button>
                <Button variant="danger" onClick={() => setShowRejectModal(true)}>
                  <FaTimes className="me-2" /> Reject
                </Button>
              </>
            )}
            {canIssue && (
              <>
                <Button variant="info" onClick={() => setShowIssueModal(true)}>
                  <FaBoxOpen className="me-2" /> Goods Issued
                </Button>
                <Button variant="danger" onClick={() => setShowStoresRejectModal(true)}>
                  <FaTimes className="me-2" /> Reject (Stores)
                </Button>
              </>
            )}
          </div>
        </Card.Body>
      </Card>

      <Tabs defaultActiveKey="details" className="mb-4">
        {/* Details Tab */}
        <Tab eventKey="details" title={<><FaFileAlt className="me-2" />Details</>}>
          <Row className="g-4">
            {/* Items — promoted directly under the header, into the space
                the removed Issue Note Information card vacated */}
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
                          <th>Company</th>
                          <th>UOM</th>
                          <th className="text-end">Quantity</th>
                        </tr>
                      </thead>
                      <tbody>
                        {details.length === 0 ? (
                          <tr>
                            <td colSpan={6} className="text-center text-muted py-4">No items found</td>
                          </tr>
                        ) : (
                          details.map((item, index) => {
                            const qty = Number(item.quantity ?? 0);
                            // Read-only display uses the effective (possibly reduced) quantity.
                            const effectiveQty = Number(item.currentEffectiveQuantity ?? item.quantity ?? 0);
                            // Convenience ceiling only (server enforces truthfully): RM caps at the original.
                            const maxQty = qty;
                            const history = item.quantityHistory ?? [];
                            const historyIcon = history.length > 0 ? (
                              <OverlayTrigger
                                trigger="click"
                                rootClose
                                placement="left"
                                overlay={
                                  <Popover id={`qty-history-${item.id}`}>
                                    <Popover.Header as="h6">Quantity History</Popover.Header>
                                    <Popover.Body>
                                      <div>Requested: {qty}</div>
                                      {history.map((h, hi) => (
                                        <div key={hi}>
                                          → {stageLabel(h.stage)}: {h.newQuantity}
                                          {' '}(by {h.editedByName || 'Unknown'}, {formatDate(h.editedAt)})
                                        </div>
                                      ))}
                                    </Popover.Body>
                                  </Popover>
                                }
                              >
                                <Button variant="link" size="sm" className="p-0 ms-1 align-baseline" title="Quantity edit history">
                                  <FaHistory />
                                </Button>
                              </OverlayTrigger>
                            ) : null;
                            return (
                              <tr key={item.id}>
                                <td>{index + 1}</td>
                                <td><code>{item.materialCode || 'N/A'}</code></td>
                                <td>{item.materialName || 'N/A'}</td>
                                <td>{item.companies || '—'}</td>
                                <td><Badge bg="secondary">{item.uomCode || 'N/A'}</Badge></td>
                                <td className="text-end fw-medium">
                                  {canRmAct ? (
                                    <div className="d-flex flex-column align-items-end">
                                      <div className="d-flex align-items-center justify-content-end">
                                        <Form.Control
                                          type="number"
                                          size="sm"
                                          min={0}
                                          max={maxQty}
                                          value={qtyEdits[item.id] ?? effectiveQty}
                                          onChange={(e) =>
                                            setQtyEdits((prev) => ({ ...prev, [item.id]: Number(e.target.value) }))
                                          }
                                          style={{ width: 90, textAlign: 'right' }}
                                        />
                                        {historyIcon}
                                      </div>
                                      <small className="text-muted">max: {maxQty}</small>
                                    </div>
                                  ) : (
                                    <span>{effectiveQty}{historyIcon}</span>
                                  )}
                                </td>
                              </tr>
                            );
                          })
                        )}
                      </tbody>
                      <tfoot className="bg-light">
                        <tr>
                          <td colSpan={5} className="text-end fw-bold">Total Quantity:</td>
                          <td className="text-end fw-bold">
                            {details.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0)}
                          </td>
                        </tr>
                      </tfoot>
                    </Table>
                  </div>
                </Card.Body>
              </Card>
            </Col>

            {/* Additional Information — purpose & comments preserved from the
                removed Issue Note Information card */}
            <Col lg={8}>
              <Card className="h-100">
                <Card.Header>
                  <h5 className="mb-0">Additional Information</h5>
                </Card.Header>
                <Card.Body>
                  {issueNote.purpose || issueNote.comments ? (
                    <Row className="g-3">
                      {issueNote.purpose && (
                        <Col sm={12}>
                          <div className="mb-3">
                            <small className="text-muted d-block">Purpose</small>
                            <p className="mb-0">{issueNote.purpose}</p>
                          </div>
                        </Col>
                      )}
                      {issueNote.comments && (
                        <Col sm={12}>
                          <div>
                            <small className="text-muted d-block">Comments</small>
                            <p className="mb-0" style={{ whiteSpace: 'pre-line' }}>{issueNote.comments}</p>
                          </div>
                        </Col>
                      )}
                    </Row>
                  ) : (
                    <p className="text-muted mb-0">No additional information.</p>
                  )}
                </Card.Body>
              </Card>
            </Col>

            {/* Workflow Status — three stages driven PURELY by the two-column model */}
            <Col lg={4}>
              <Card className="h-100">
                <Card.Header>
                  <h5 className="mb-0">Workflow Status</h5>
                </Card.Header>
                <Card.Body>
                  {(() => {
                    // Stage states from the two-column workflow model — never from the
                    // Spring single-column status (legacy rows carry status=1 as an active flag):
                    // RM Review: approvedStatus 1=pending, 3=approved, 2=rejected
                    // Stores:    storesByStatus 11=issued, 2=rejected, 1=pending (once RM approved)
                    const rmApproved = approvedStatusId === 3;
                    const rmRejected = approvedStatusId === 2;
                    const rmPending = approvedStatusId === 1;
                    const storesIssued = rmApproved && storesByStatusId === 11;
                    const storesRejected = rmApproved && storesByStatusId === 2;
                    const storesPending = rmApproved && storesByStatusId === 1;

                    type StageState = 'done' | 'rejected' | 'pending' | 'unreached';
                    const stages: { label: string; state: StageState; detail?: React.ReactNode }[] = [
                      {
                        // Any persisted issue note has entered the workflow.
                        label: 'Submitted',
                        state: 'done',
                        detail: (
                          <span className="text-muted">
                            {issueNote.employeeName || 'N/A'} on {formatDate(issueNote.issueDate)}
                          </span>
                        ),
                      },
                      {
                        label: 'RM Review',
                        state: rmRejected ? 'rejected' : rmApproved ? 'done' : rmPending ? 'pending' : 'unreached',
                        detail: rmRejected
                          ? <span className="text-danger">Rejected</span>
                          : rmApproved
                            ? <span className="text-success">
                              Approved{issueNote.rmApprovedByName ? ` by ${issueNote.rmApprovedByName}` : ''}
                              {issueNote.rmApprovedByDate ? ` on ${formatDate(issueNote.rmApprovedByDate)}` : ''}
                            </span>
                            : <span className="text-warning">Pending</span>,
                      },
                      {
                        label: 'Stores',
                        state: storesIssued ? 'done' : storesRejected ? 'rejected' : storesPending ? 'pending' : 'unreached',
                        detail: storesIssued
                          ? <span className="text-success">
                            Goods Issued{issueNote.issuedByName ? ` by ${issueNote.issuedByName}` : ''}
                          </span>
                          : storesRejected
                            ? <span className="text-danger">Rejected by Stores</span>
                            : storesPending
                              ? <span className="text-warning">Awaiting Stores Issue</span>
                              : undefined,
                      },
                    ];

                    return (
                      <div className="workflow-timeline">
                        {stages.map((stage, i) => (
                          <div className="d-flex mb-3" key={stage.label}>
                            <div
                              className={`rounded-circle me-3 d-flex align-items-center justify-content-center ${stage.state === 'done' ? 'bg-success'
                                : stage.state === 'rejected' ? 'bg-danger'
                                  : stage.state === 'pending' ? 'bg-warning'
                                    : 'bg-secondary'
                                }`}
                              style={{ width: 32, height: 32, minWidth: 32 }}
                            >
                              {stage.state === 'done' ? <FaCheck className="text-white" size={12} />
                                : stage.state === 'rejected' ? <FaTimes className="text-white" size={12} />
                                  : <span className="text-white">{i + 1}</span>}
                            </div>
                            <div className="flex-grow-1">
                              <strong className={stage.state === 'unreached' ? 'text-muted' : undefined}>{stage.label}</strong>
                              {stage.detail && <div className="small">{stage.detail}</div>}
                            </div>
                          </div>
                        ))}
                      </div>
                    );
                  })()}

                  {(approvedStatusId === 2 || (approvedStatusId === 3 && storesByStatusId === 2)) && (
                    <Alert variant="danger" className="mt-3 mb-0">
                      <strong>{approvedStatusId === 2 ? 'Rejected by RM' : 'Rejected by Stores'}</strong>
                    </Alert>
                  )}
                </Card.Body>
              </Card>
            </Col>

          </Row>
        </Tab>
      </Tabs>

      {/* RM Approve Modal */}
      <Modal show={showApproveModal} onHide={() => setShowApproveModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Approve Issue Note (RM Review)</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Are you sure you want to approve issue note <strong>{issueNote.issueNoteNumber}</strong>?</p>
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
          <Button variant="secondary" onClick={() => setShowApproveModal(false)}>
            Cancel
          </Button>
          <Button
            variant="success"
            onClick={() => rmApproveMutation.mutate()}
            disabled={rmApproveMutation.isPending}
          >
            {rmApproveMutation.isPending && (
              <Spinner as="span" animation="border" size="sm" className="me-2" />
            )}
            Approve
          </Button>
        </Modal.Footer>
      </Modal>

      {/* RM Reject Modal */}
      <Modal show={showRejectModal} onHide={() => setShowRejectModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Reject Issue Note (RM Review)</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Are you sure you want to reject issue note <strong>{issueNote.issueNoteNumber}</strong>?</p>
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
          <Button variant="secondary" onClick={() => setShowRejectModal(false)}>
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={() => rmRejectMutation.mutate()}
            disabled={rmRejectMutation.isPending || !rejectionReason.trim()}
          >
            {rmRejectMutation.isPending && (
              <Spinner as="span" animation="border" size="sm" className="me-2" />
            )}
            Reject
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Issue Modal */}
      <Modal show={showIssueModal} onHide={() => setShowIssueModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Goods Issued</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>You are about to issue materials for <strong>{issueNote.issueNoteNumber}</strong>.</p>
          <Alert variant="info">
            <strong>Items to Issue:</strong>
            <ul className="mb-0 mt-2">
              {details.map((item) => (
                <li key={item.id}>
                  {item.materialCode || `Material #${item.materialId}`}: {item.quantity} {item.uomCode || ''}
                </li>
              ))}
            </ul>
          </Alert>
          <Form.Group>
            <Form.Label>Remarks (Optional)</Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={issueRemarks}
              onChange={(e) => setIssueRemarks(e.target.value)}
              placeholder="Add any issue remarks..."
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowIssueModal(false)}>
            Cancel
          </Button>
          <Button
            variant="info"
            onClick={() => issueMutation.mutate()}
            disabled={issueMutation.isPending}
          >
            {issueMutation.isPending && (
              <Spinner as="span" animation="border" size="sm" className="me-2" />
            )}
            <FaBoxOpen className="me-2" /> Goods Issued
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Stores Reject Modal */}
      <Modal show={showStoresRejectModal} onHide={() => setShowStoresRejectModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Reject (Stores)</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Reject issue note <strong>{issueNote.issueNoteNumber}</strong> — materials cannot be issued from stores?</p>
          <Form.Group>
            <Form.Label>Rejection Reason <span className="text-danger">*</span></Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={storesRejectReason}
              onChange={(e) => setStoresRejectReason(e.target.value)}
              placeholder="Provide a reason (e.g. insufficient stock)..."
              required
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowStoresRejectModal(false)}>
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={() => storesRejectMutation.mutate()}
            disabled={storesRejectMutation.isPending || !storesRejectReason.trim()}
          >
            {storesRejectMutation.isPending && (
              <Spinner as="span" animation="border" size="sm" className="me-2" />
            )}
            Reject (Stores)
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
      `}</style>
    </div>
  );
};

export default IssueNoteDetailPage;
