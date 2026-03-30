import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Card,
    Table,
    Button,
    Badge,
    Form,
    Modal,
    Alert,
    Row,
    Col,
    InputGroup,
    Spinner,
    ButtonGroup,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
    FaSearch,
    FaEye,
    FaCheck,
    FaTimes,
    FaFilter,
    FaClipboardList,
    FaSync,
    FaBoxOpen,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { issueNotesApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { IssueNote, IssueNoteStatus } from '../../api/issueNotes';

const IssueNoteApprovalPage: React.FC = () => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { hasAnyRole } = useAuth();

    // Filter state
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState<IssueNoteStatus | undefined>(IssueNoteStatus.PENDING_RM_APPROVAL);
    const [page, setPage] = useState(0);
    const pageSize = 10;

    // Modal state
    const [selectedNote, setSelectedNote] = useState<IssueNote | null>(null);
    const [showApproveModal, setShowApproveModal] = useState(false);
    const [showIssueModal, setShowIssueModal] = useState(false);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [remarks, setRemarks] = useState('');
    const [rejectionReason, setRejectionReason] = useState('');
    const [error, setError] = useState<string | null>(null);

    // Determine if user is stores or manager
    const isStores = hasAnyRole(['STOREKEEPER', 'ADMIN', 'SUPERADMIN']);
    const isManager = hasAnyRole(['PLANTMANAGER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN']);

    // Fetch issue notes pending action
    const { data, isLoading, refetch, isFetching } = useQuery({
        queryKey: ['issue-notes-approval', page, pageSize, searchTerm, statusFilter],
        queryFn: () =>
            issueNotesApi.list({
                page,
                size: pageSize,
                search: searchTerm || undefined,
                status: statusFilter,
            }),
    });

    // Approve mutation (for manager approval)
    const approveMutation = useMutation({
        mutationFn: (id: number) => issueNotesApi.approve(id, { remarks }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['issue-notes-approval'] });
            queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
            setShowApproveModal(false);
            setSelectedNote(null);
            setRemarks('');
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    // Issue mutation (for stores to issue materials)
    const issueMutation = useMutation({
        mutationFn: (id: number) => issueNotesApi.issue(id, { remarks }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['issue-notes-approval'] });
            queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
            queryClient.invalidateQueries({ queryKey: ['inventory'] });
            setShowIssueModal(false);
            setSelectedNote(null);
            setRemarks('');
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    // Reject mutation
    const rejectMutation = useMutation({
        mutationFn: (id: number) => issueNotesApi.reject(id, { reason: rejectionReason }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['issue-notes-approval'] });
            queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
            setShowRejectModal(false);
            setSelectedNote(null);
            setRejectionReason('');
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    // Status badge variant (backend 10 statuses)
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
            [IssueNoteStatus.RETURNED]: 'warning',
        };
        return variants[status] || 'secondary';
    };

    // Get status name
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

    const handleApprove = (note: IssueNote) => {
        setSelectedNote(note);
        setShowApproveModal(true);
    };

    const handleIssue = (note: IssueNote) => {
        setSelectedNote(note);
        setShowIssueModal(true);
    };

    const handleReject = (note: IssueNote) => {
        setSelectedNote(note);
        setShowRejectModal(true);
    };

    const confirmApprove = () => {
        if (selectedNote) {
            approveMutation.mutate(selectedNote.id);
        }
    };

    const confirmIssue = () => {
        if (selectedNote) {
            issueMutation.mutate(selectedNote.id);
        }
    };

    const confirmReject = () => {
        if (selectedNote && rejectionReason.trim()) {
            rejectMutation.mutate(selectedNote.id);
        }
    };

    const issueNotes = data?.content || [];
    const totalElements = data?.totalElements || 0;
    const totalPages = data?.totalPages || 0;

    return (
        <div>
            <PageHeader
                title="Issue Note Approvals"
                subtitle={`${totalElements} issue note${totalElements !== 1 ? 's' : ''} pending action`}
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Issue Notes', path: '/issue-notes' },
                    { label: 'Approvals' },
                ]}
                actions={
                    <Button
                        variant="outline-primary"
                        onClick={() => refetch()}
                        disabled={isFetching}
                    >
                        <FaSync className={`me-2 ${isFetching ? 'fa-spin' : ''}`} />
                        Refresh
                    </Button>
                }
            />

            {error && (
                <Alert variant="danger" dismissible onClose={() => setError(null)}>
                    {error}
                </Alert>
            )}

            {/* Filters */}
            <Card className="mb-4">
                <Card.Body>
                    <Row className="g-3">
                        <Col md={5}>
                            <InputGroup>
                                <InputGroup.Text>
                                    <FaSearch />
                                </InputGroup.Text>
                                <Form.Control
                                    type="text"
                                    placeholder="Search by issue note number, purpose..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </InputGroup>
                        </Col>
                        <Col md={4}>
                            <InputGroup>
                                <InputGroup.Text>
                                    <FaFilter />
                                </InputGroup.Text>
                                <Form.Select
                                    value={statusFilter ?? ''}
                                    onChange={(e) => setStatusFilter(e.target.value ? parseInt(e.target.value) : undefined)}
                                >
                                    <option value="">All Statuses</option>
                                    <option value={IssueNoteStatus.PENDING_RM_APPROVAL}>Pending RM Approval</option>
                                    <option value={IssueNoteStatus.APPROVED_BY_MANAGER}>Approved (Pending Issue)</option>
                                    <option value={IssueNoteStatus.ISSUED}>Issued</option>
                                    <option value={IssueNoteStatus.REJECTED_BY_RM}>Rejected</option>
                                </Form.Select>
                            </InputGroup>
                        </Col>
                        <Col md={3}>
                            <Button
                                variant="outline-secondary"
                                className="w-100"
                                onClick={() => {
                                    setSearchTerm('');
                                    setStatusFilter(IssueNoteStatus.PENDING_RM_APPROVAL);
                                    setPage(0);
                                }}
                            >
                                Clear Filters
                            </Button>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            {/* Issue Notes List */}
            <Card>
                <Card.Header className="d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">
                        <FaClipboardList className="me-2" />
                        Issue Notes Queue
                    </h5>
                    <Badge bg="warning" className="fs-6">
                        {totalElements} Items
                    </Badge>
                </Card.Header>
                <Card.Body className="p-0">
                    {isLoading ? (
                        <LoadingSpinner text="Loading issue notes..." />
                    ) : issueNotes.length === 0 ? (
                        <div className="text-center py-5">
                            <FaClipboardList size={48} className="text-muted mb-3" />
                            <h5>No Issue Notes Found</h5>
                            <p className="text-muted">
                                No issue notes match your current filter criteria.
                            </p>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="mb-0">
                                <thead className="bg-light">
                                    <tr>
                                        <th>Issue Note #</th>
                                        <th>Requester</th>
                                        <th>Department</th>
                                        <th>Purpose</th>
                                        <th>Status</th>
                                        <th>Items</th>
                                        <th>Date</th>
                                        <th className="text-center">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {issueNotes.map((note) => (
                                        <tr key={note.id}>
                                            <td>
                                                <Button
                                                    variant="link"
                                                    className="p-0 text-decoration-none"
                                                    onClick={() => navigate(`/issue-notes/${note.id}`)}
                                                >
                                                    {note.issueNumber}
                                                </Button>
                                            </td>
                                            <td>{note.requestedByName}</td>
                                            <td>{note.departmentName}</td>
                                            <td className="text-truncate" style={{ maxWidth: 200 }}>
                                                {note.purpose}
                                            </td>
                                            <td>
                                                <Badge bg={getStatusVariant(note.status)}>
                                                    {note.statusName || getStatusName(note.status)}
                                                </Badge>
                                            </td>
                                            <td>
                                                <Badge bg="secondary">{note.items?.length || 0}</Badge>
                                            </td>
                                            <td>
                                                {format(new Date(note.issueDate || note.createdAt || new Date()), 'dd MMM yyyy')}
                                            </td>
                                            <td>
                                                <ButtonGroup size="sm">
                                                    <Button
                                                        variant="outline-primary"
                                                        title="View Details"
                                                        onClick={() => navigate(`/issue-notes/${note.id}`)}
                                                    >
                                                        <FaEye />
                                                    </Button>
                                                    {(note.status === IssueNoteStatus.PENDING_RM_APPROVAL || note.status === IssueNoteStatus.RM_APPROVED) && isManager && (
                                                        <>
                                                            <Button
                                                                variant="success"
                                                                title="Approve"
                                                                onClick={() => handleApprove(note)}
                                                            >
                                                                <FaCheck />
                                                            </Button>
                                                            <Button
                                                                variant="danger"
                                                                title="Reject"
                                                                onClick={() => handleReject(note)}
                                                            >
                                                                <FaTimes />
                                                            </Button>
                                                        </>
                                                    )}
                                                    {(note.status === IssueNoteStatus.APPROVED_BY_MANAGER || note.status === IssueNoteStatus.PENDING_STORE_ISSUE) && isStores && (
                                                        <Button
                                                            variant="info"
                                                            title="Issue Materials"
                                                            onClick={() => handleIssue(note)}
                                                        >
                                                            <FaBoxOpen />
                                                        </Button>
                                                    )}
                                                </ButtonGroup>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Card.Body>

                {/* Pagination */}
                {totalPages > 1 && (
                    <Card.Footer className="d-flex justify-content-between align-items-center">
                        <span className="text-muted">
                            Showing {page * pageSize + 1} to{' '}
                            {Math.min((page + 1) * pageSize, totalElements)} of {totalElements}
                        </span>
                        <ButtonGroup>
                            <Button
                                variant="outline-secondary"
                                size="sm"
                                disabled={page === 0}
                                onClick={() => setPage(page - 1)}
                            >
                                Previous
                            </Button>
                            <Button
                                variant="outline-secondary"
                                size="sm"
                                disabled={page >= totalPages - 1}
                                onClick={() => setPage(page + 1)}
                            >
                                Next
                            </Button>
                        </ButtonGroup>
                    </Card.Footer>
                )}
            </Card>

            {/* Approve Modal */}
            <Modal show={showApproveModal} onHide={() => setShowApproveModal(false)} centered>
                <Modal.Header closeButton className="bg-success text-white">
                    <Modal.Title>
                        <FaCheck className="me-2" />
                        Approve Issue Note
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedNote && (
                        <>
                            <Alert variant="info" className="mb-3">
                                <strong>Issue Note:</strong> {selectedNote.issueNumber}
                                <br />
                                <strong>Requester:</strong> {selectedNote.requestedByName}
                                <br />
                                <strong>Purpose:</strong> {selectedNote.purpose}
                            </Alert>
                            <Form.Group>
                                <Form.Label>Approval Remarks (Optional)</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    value={remarks}
                                    onChange={(e) => setRemarks(e.target.value)}
                                    placeholder="Add any comments..."
                                />
                            </Form.Group>
                        </>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowApproveModal(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant="success"
                        onClick={confirmApprove}
                        disabled={approveMutation.isPending}
                    >
                        {approveMutation.isPending && (
                            <Spinner as="span" animation="border" size="sm" className="me-2" />
                        )}
                        Approve
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* Issue Modal */}
            <Modal show={showIssueModal} onHide={() => setShowIssueModal(false)} centered>
                <Modal.Header closeButton className="bg-info text-white">
                    <Modal.Title>
                        <FaBoxOpen className="me-2" />
                        Issue Materials
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedNote && (
                        <>
                            <Alert variant="warning" className="mb-3">
                                <strong>Issue Note:</strong> {selectedNote.issueNumber}
                                <br />
                                <strong>Requester:</strong> {selectedNote.requestedByName}
                                <br />
                                <small className="text-muted">
                                    This will update inventory and mark materials as issued.
                                </small>
                            </Alert>
                            <Form.Group>
                                <Form.Label>Issue Remarks (Optional)</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    value={remarks}
                                    onChange={(e) => setRemarks(e.target.value)}
                                    placeholder="Add any comments about the issue..."
                                />
                            </Form.Group>
                        </>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowIssueModal(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant="info"
                        onClick={confirmIssue}
                        disabled={issueMutation.isPending}
                    >
                        {issueMutation.isPending && (
                            <Spinner as="span" animation="border" size="sm" className="me-2" />
                        )}
                        Confirm Issue
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* Reject Modal */}
            <Modal show={showRejectModal} onHide={() => setShowRejectModal(false)} centered>
                <Modal.Header closeButton className="bg-danger text-white">
                    <Modal.Title>
                        <FaTimes className="me-2" />
                        Reject Issue Note
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedNote && (
                        <>
                            <Alert variant="warning" className="mb-3">
                                <strong>Issue Note:</strong> {selectedNote.issueNumber}
                                <br />
                                <strong>Requester:</strong> {selectedNote.requestedByName}
                            </Alert>
                            <Form.Group>
                                <Form.Label>
                                    Rejection Reason <span className="text-danger">*</span>
                                </Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    value={rejectionReason}
                                    onChange={(e) => setRejectionReason(e.target.value)}
                                    placeholder="Provide a reason for rejection (required)..."
                                    required
                                />
                            </Form.Group>
                        </>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowRejectModal(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant="danger"
                        onClick={confirmReject}
                        disabled={rejectMutation.isPending || !rejectionReason.trim()}
                    >
                        {rejectMutation.isPending && (
                            <Spinner as="span" animation="border" size="sm" className="me-2" />
                        )}
                        Reject
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default IssueNoteApprovalPage;
