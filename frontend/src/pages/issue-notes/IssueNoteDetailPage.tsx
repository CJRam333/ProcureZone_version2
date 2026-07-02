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
import { format } from 'date-fns';
import {
  FaArrowLeft,
  FaEdit,
  FaPaperPlane,
  FaCheck,
  FaTimes,
  FaPrint,
  FaHistory,
  FaFileAlt,
  FaBoxOpen,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { issueNotesApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { IssueNoteStatus } from '../../api/issueNotes';

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

  // Fetch issue note details
  const { data: issueNote, isLoading, error: fetchError } = useQuery({
    queryKey: ['issue-note', id],
    queryFn: () => issueNotesApi.getById(Number(id)),
    enabled: !!id,
  });

  // Mutations
  const submitMutation = useMutation({
    mutationFn: () => issueNotesApi.submit(Number(id)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-note', id] });
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const approveMutation = useMutation({
    mutationFn: () => issueNotesApi.approve(Number(id), { remarks: approvalComments }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-note', id] });
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
      setShowApproveModal(false);
      setApprovalComments('');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const rejectMutation = useMutation({
    mutationFn: () => issueNotesApi.reject(Number(id), { reason: rejectionReason }),
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

  // Permissions based on status enum (backend 1-based, 10 statuses)
  const canEdit = issueNote?.status === IssueNoteStatus.CREATED && hasAnyRole(['SUPERADMIN', 'ADMIN', 'USER', 'ISSUECONFIRM', 'SUPERVISOR']);
  const canSubmit = issueNote?.status === IssueNoteStatus.CREATED && hasAnyRole(['SUPERADMIN', 'ADMIN', 'USER', 'ISSUECONFIRM', 'SUPERVISOR']);
  const canApprove = (issueNote?.status === IssueNoteStatus.PENDING_RM_APPROVAL || issueNote?.status === IssueNoteStatus.RM_APPROVED) && hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'DEPTHEAD', 'SUPERVISOR']);
  // Two-column check: approvedStatus=3 (RM approved) AND storesByStatus=1 (pending store issue)
  const canIssue = issueNote?.approvedStatus === 3 && issueNote?.storesByStatus === 1 && hasAnyRole(['SUPERADMIN', 'ADMIN', 'ISSUECONFIRM']);

  // Status color mapping based on backend enum values (1-10)
  const getStatusVariant = (status: IssueNoteStatus): string => {
    const variants: Record<number, string> = {
      [IssueNoteStatus.CREATED]: 'secondary',
      [IssueNoteStatus.PENDING_RM_APPROVAL]: 'warning',
      [IssueNoteStatus.RM_APPROVED]: 'info',
      [IssueNoteStatus.APPROVED_BY_MANAGER]: 'primary',
      [IssueNoteStatus.REJECTED_BY_RM]: 'danger',
      [IssueNoteStatus.REJECTED_BY_MANAGER]: 'danger',
      [IssueNoteStatus.PENDING_STORE_ISSUE]: 'warning',
      [IssueNoteStatus.ISSUED]: 'success',
      [IssueNoteStatus.REJECTED_BY_STORES]: 'danger',
      [IssueNoteStatus.RETURNED]: 'info',
    };
    return variants[status] || 'secondary';
  };

  // Safe date formatter
  const formatDate = (dateStr: string | null | undefined): string => {
    if (!dateStr) return 'N/A';
    try {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return 'N/A';
      return format(date, 'PPP');
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

  const getStatusName = (status: IssueNoteStatus): string => {
    const names: Record<number, string> = {
      [IssueNoteStatus.CREATED]: 'Created',
      [IssueNoteStatus.PENDING_RM_APPROVAL]: 'Pending RM Approval',
      [IssueNoteStatus.RM_APPROVED]: 'RM Approved',
      [IssueNoteStatus.APPROVED_BY_MANAGER]: 'Manager Approved',
      [IssueNoteStatus.REJECTED_BY_RM]: 'Rejected by RM',
      [IssueNoteStatus.REJECTED_BY_MANAGER]: 'Rejected by Manager',
      [IssueNoteStatus.PENDING_STORE_ISSUE]: 'Pending Store Issue',
      [IssueNoteStatus.ISSUED]: 'Issued',
      [IssueNoteStatus.REJECTED_BY_STORES]: 'Rejected by Stores',
      [IssueNoteStatus.RETURNED]: 'Returned',
    };
    return names[status] || 'Unknown';
  };

  return (
    <div>
      <PageHeader
        title={`Issue Note ${issueNote.issueNumber || issueNote.issueNoteNumber || 'N/A'}`}
        subtitle={`Requested by ${issueNote.requestedByName || 'N/A'} on ${formatDate(issueNote.createdAt)}`}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Issue Notes', path: '/issue-notes' },
          { label: issueNote.issueNumber || issueNote.issueNoteNumber || 'Detail' },
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
              <Badge bg={getStatusVariant(issueNote.status)} className="fs-6">
                {issueNote.statusName || getStatusName(issueNote.status)}
              </Badge>
            </div>
            <div className="vr d-none d-sm-block" />
            <div>
              <small className="text-muted d-block">Department</small>
              <strong>{issueNote.departmentName || 'N/A'}</strong>
            </div>
            <div className="vr d-none d-sm-block" />
            <div>
              <small className="text-muted d-block">Plant</small>
              <strong>{issueNote.plantName || 'N/A'}</strong>
            </div>
            <div className="vr d-none d-sm-block" />
            <div>
              <small className="text-muted d-block">Total Items</small>
              <strong className="text-primary fs-5">{issueNote.items?.length || 0}</strong>
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
                  <FaCheck className="me-2" /> Approve
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
            {/* Basic Info */}
            <Col lg={8}>
              <Card className="h-100">
                <Card.Header>
                  <h5 className="mb-0">Issue Note Information</h5>
                </Card.Header>
                <Card.Body>
                  <Row className="g-3">
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Issue Note Number</small>
                        <strong>{issueNote.issueNumber}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Issue Date</small>
                        <strong>{formatDate(issueNote.issueDate)}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Requested By</small>
                        <strong>{issueNote.requestedByName}</strong>
                      </div>
                    </Col>
                    <Col sm={6}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Created Date</small>
                        <strong>{formatDate(issueNote.createdAt)}</strong>
                      </div>
                    </Col>
                    <Col sm={12}>
                      <div className="mb-3">
                        <small className="text-muted d-block">Purpose</small>
                        <p className="mb-0">{issueNote.purpose}</p>
                      </div>
                    </Col>
                    {issueNote.remarks && (
                      <Col sm={12}>
                        <div>
                          <small className="text-muted d-block">Remarks</small>
                          <p className="mb-0">{issueNote.remarks}</p>
                        </div>
                      </Col>
                    )}
                    {issueNote.issuedByName && (
                      <Col sm={6}>
                        <div className="mb-3">
                          <small className="text-muted d-block">Issued By</small>
                          <strong>{issueNote.issuedByName}</strong>
                        </div>
                      </Col>
                    )}
                  </Row>
                </Card.Body>
              </Card>
            </Col>

            {/* Status Info */}
            <Col lg={4}>
              <Card className="h-100">
                <Card.Header>
                  <h5 className="mb-0">Workflow Status</h5>
                </Card.Header>
                <Card.Body>
                  <div className="workflow-timeline">
                    {/* Draft/Created */}
                    <div className="d-flex mb-3">
                      <div
                        className={`rounded-circle me-3 d-flex align-items-center justify-content-center ${issueNote.status >= IssueNoteStatus.CREATED ? 'bg-success' : 'bg-secondary'
                          }`}
                        style={{ width: 32, height: 32, minWidth: 32 }}
                      >
                        <FaCheck className="text-white" size={12} />
                      </div>
                      <div className="flex-grow-1">
                        <strong>Created</strong>
                        <div className="small text-muted">
                          {issueNote.requestedByName} on {formatDate(issueNote.createdAt)}
                        </div>
                      </div>
                    </div>

                    {/* Pending RM Approval */}
                    <div className="d-flex mb-3">
                      <div
                        className={`rounded-circle me-3 d-flex align-items-center justify-content-center ${issueNote.status >= IssueNoteStatus.PENDING_RM_APPROVAL
                          ? issueNote.status === IssueNoteStatus.PENDING_RM_APPROVAL
                            ? 'bg-warning'
                            : 'bg-success'
                          : 'bg-secondary'
                          }`}
                        style={{ width: 32, height: 32, minWidth: 32 }}
                      >
                        {issueNote.status >= IssueNoteStatus.RM_APPROVED ? (
                          <FaCheck className="text-white" size={12} />
                        ) : (
                          <span className="text-white">2</span>
                        )}
                      </div>
                      <div className="flex-grow-1">
                        <strong>RM Approval</strong>
                        <div className="small text-muted">
                          {issueNote.status === IssueNoteStatus.PENDING_RM_APPROVAL
                            ? 'Pending'
                            : issueNote.status >= IssueNoteStatus.RM_APPROVED
                              ? 'Approved'
                              : 'Waiting'}
                        </div>
                      </div>
                    </div>

                    {/* Issued */}
                    <div className="d-flex mb-3">
                      <div
                        className={`rounded-circle me-3 d-flex align-items-center justify-content-center ${issueNote.status >= IssueNoteStatus.ISSUED
                          ? 'bg-success'
                          : issueNote.status === IssueNoteStatus.APPROVED_BY_MANAGER || issueNote.status === IssueNoteStatus.PENDING_STORE_ISSUE
                            ? 'bg-warning'
                            : 'bg-secondary'
                          }`}
                        style={{ width: 32, height: 32, minWidth: 32 }}
                      >
                        {issueNote.status >= IssueNoteStatus.ISSUED ? (
                          <FaCheck className="text-white" size={12} />
                        ) : (
                          <span className="text-white">3</span>
                        )}
                      </div>
                      <div className="flex-grow-1">
                        <strong>Issue Materials</strong>
                        <div className="small text-muted">
                          {issueNote.status >= IssueNoteStatus.ISSUED
                            ? `Issued${issueNote.issuedByName ? ` by ${issueNote.issuedByName}` : ''}`
                            : issueNote.status === IssueNoteStatus.APPROVED_BY_MANAGER || issueNote.status === IssueNoteStatus.PENDING_STORE_ISSUE
                              ? 'Ready for issue'
                              : 'Waiting'}
                        </div>
                      </div>
                    </div>
                  </div>

                  {(issueNote.status === IssueNoteStatus.REJECTED_BY_RM || issueNote.status === IssueNoteStatus.REJECTED_BY_MANAGER || issueNote.status === IssueNoteStatus.REJECTED_BY_STORES) && (
                    <Alert variant="danger" className="mt-3 mb-0">
                      <strong>Rejected</strong>
                      <div className="mt-2">This issue note has been rejected.</div>
                    </Alert>
                  )}
                </Card.Body>
              </Card>
            </Col>

            {/* Items */}
            <Col xs={12}>
              <Card>
                <Card.Header>
                  <h5 className="mb-0">Items ({issueNote.items?.length || 0})</h5>
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
                          <th className="text-end">Requested Qty</th>
                          <th className="text-end">Approved Qty</th>
                          <th className="text-end">Issued Qty</th>
                          <th>Batch #</th>
                          <th>Remarks</th>
                        </tr>
                      </thead>
                      <tbody>
                        {issueNote.items?.map((item, index) => (
                          <tr key={item.id}>
                            <td>{index + 1}</td>
                            <td>
                              <code>{item.materialCode}</code>
                            </td>
                            <td>{item.materialDescription}</td>
                            <td>
                              <Badge bg="secondary">{item.uomCode}</Badge>
                            </td>
                            <td className="text-end">{item.requestedQuantity}</td>
                            <td className="text-end">{item.approvedQuantity || '-'}</td>
                            <td className="text-end fw-medium">
                              {item.issuedQuantity || '-'}
                            </td>
                            <td>{item.batchNumber || '-'}</td>
                            <td>{item.remarks || '-'}</td>
                          </tr>
                        ))}
                      </tbody>
                      <tfoot className="bg-light">
                        <tr>
                          <td colSpan={4} className="text-end fw-bold">
                            Totals:
                          </td>
                          <td className="text-end fw-bold">
                            {issueNote.items?.reduce((sum, item) => sum + (item.requestedQuantity ?? item.quantity ?? 0), 0) || 0}
                          </td>
                          <td className="text-end fw-bold">
                            {issueNote.items?.reduce((sum, item) => sum + (item.approvedQuantity || 0), 0) || '-'}
                          </td>
                          <td className="text-end fw-bold text-primary">
                            {issueNote.items?.reduce((sum, item) => sum + (item.issuedQuantity || 0), 0) || '-'}
                          </td>
                          <td colSpan={2}></td>
                        </tr>
                      </tfoot>
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
                      {issueNote.requestedByName} created this issue note on{' '}
                      {formatDate(issueNote.createdAt)}
                    </p>
                  </div>
                </div>
                {issueNote.status >= IssueNoteStatus.PENDING_RM_APPROVAL && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-warning"></div>
                    <div className="timeline-content">
                      <strong>Submitted for Approval</strong>
                      <p className="text-muted mb-0">Awaiting approval</p>
                    </div>
                  </div>
                )}
                {issueNote.status >= IssueNoteStatus.APPROVED_BY_MANAGER && issueNote.status !== IssueNoteStatus.REJECTED_BY_RM && issueNote.status !== IssueNoteStatus.REJECTED_BY_MANAGER && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-success"></div>
                    <div className="timeline-content">
                      <strong>Approved</strong>
                      <p className="text-muted mb-0">Issue note approved</p>
                    </div>
                  </div>
                )}
                {issueNote.status === IssueNoteStatus.ISSUED && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-info"></div>
                    <div className="timeline-content">
                      <strong>Materials Issued</strong>
                      <p className="text-muted mb-0">
                        {issueNote.issuedByName
                          ? `Issued by ${issueNote.issuedByName}`
                          : 'Materials have been issued'}
                      </p>
                    </div>
                  </div>
                )}
                {(issueNote.status === IssueNoteStatus.REJECTED_BY_RM || issueNote.status === IssueNoteStatus.REJECTED_BY_MANAGER || issueNote.status === IssueNoteStatus.REJECTED_BY_STORES) && (
                  <div className="timeline-item">
                    <div className="timeline-marker bg-danger"></div>
                    <div className="timeline-content">
                      <strong>Rejected</strong>
                      <p className="text-muted mb-0">Issue note was rejected</p>
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
          <Modal.Title>Approve Issue Note</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Are you sure you want to approve issue note <strong>{issueNote.issueNumber}</strong>?</p>
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
            onClick={() => approveMutation.mutate()}
            disabled={approveMutation.isPending}
          >
            {approveMutation.isPending && (
              <Spinner as="span" animation="border" size="sm" className="me-2" />
            )}
            Approve
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Reject Modal */}
      <Modal show={showRejectModal} onHide={() => setShowRejectModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Reject Issue Note</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Are you sure you want to reject issue note <strong>{issueNote.issueNumber}</strong>?</p>
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
            onClick={() => rejectMutation.mutate()}
            disabled={rejectMutation.isPending || !rejectionReason.trim()}
          >
            {rejectMutation.isPending && (
              <Spinner as="span" animation="border" size="sm" className="me-2" />
            )}
            Reject
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Issue Modal */}
      <Modal show={showIssueModal} onHide={() => setShowIssueModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Issue Materials</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>You are about to issue materials for <strong>{issueNote.issueNumber}</strong>.</p>
          <Alert variant="info">
            <strong>Items to Issue:</strong>
            <ul className="mb-0 mt-2">
              {issueNote.items?.map((item) => (
                <li key={item.id}>
                  {item.materialCode}: {item.approvedQuantity || item.requestedQuantity} {item.uomCode}
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
            <FaBoxOpen className="me-2" /> Issue Materials
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Stores Reject Modal */}
      <Modal show={showStoresRejectModal} onHide={() => setShowStoresRejectModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Reject (Stores)</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Reject issue note <strong>{issueNote.issueNumber}</strong> — materials cannot be issued from stores?</p>
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
