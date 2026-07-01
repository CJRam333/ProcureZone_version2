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
    FaClipboardCheck,
    FaSync,
    FaCheckDouble,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { indentsApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { Indent } from '../../api/indents';
import { INDENT_STATUS_COLORS } from '../../constants/indentStatus';

const IndentApprovalPage: React.FC = () => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { user, hasAnyRole } = useAuth();

    // Filter state
    const [searchTerm, setSearchTerm] = useState('');
    const [priorityFilter, setPriorityFilter] = useState<string>('');
    const [page, setPage] = useState(0);
    const pageSize = 10;

    // Selection state
    const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());

    // Modal state
    const [selectedIndent, setSelectedIndent] = useState<Indent | null>(null);
    const [showApproveModal, setShowApproveModal] = useState(false);
    const [showBulkApproveModal, setShowBulkApproveModal] = useState(false);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [approvalRemarks, setApprovalRemarks] = useState('');
    const [bulkApprovalRemarks, setBulkApprovalRemarks] = useState('');
    const [rejectionReason, setRejectionReason] = useState('');
    const [error, setError] = useState<string | null>(null);

    // Fetch pending approvals
    const { data, isLoading, refetch, isFetching } = useQuery({
        queryKey: ['pending-approvals', page, pageSize, searchTerm, priorityFilter],
        queryFn: () =>
            indentsApi.getPendingApproval({
                page,
                size: pageSize,
                search: searchTerm || undefined,
                priority: priorityFilter || undefined,
            }),
    });

    // Approve mutation
    const approveMutation = useMutation({
        mutationFn: (id: number) =>
            indentsApi.approve(id, { remarks: approvalRemarks }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pending-approvals'] });
            queryClient.invalidateQueries({ queryKey: ['indents'] });
            setShowApproveModal(false);
            setSelectedIndent(null);
            setApprovalRemarks('');
            setSelectedIds(prev => {
                const newSet = new Set(prev);
                if (selectedIndent) newSet.delete(selectedIndent.id);
                return newSet;
            });
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    // Bulk Approve Mutation
    const bulkApproveMutation = useMutation({
        mutationFn: async (ids: number[]) => {
            const promises = ids.map(id => indentsApi.approve(id, { remarks: bulkApprovalRemarks }));
            return Promise.all(promises);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pending-approvals'] });
            queryClient.invalidateQueries({ queryKey: ['indents'] });
            setShowBulkApproveModal(false);
            setBulkApprovalRemarks('');
            setSelectedIds(new Set());
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    // Reject mutation
    const rejectMutation = useMutation({
        mutationFn: (id: number) =>
            indentsApi.reject(id, { reason: rejectionReason }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pending-approvals'] });
            queryClient.invalidateQueries({ queryKey: ['indents'] });
            setShowRejectModal(false);
            setSelectedIndent(null);
            setRejectionReason('');
            setSelectedIds(prev => {
                const newSet = new Set(prev);
                if (selectedIndent) newSet.delete(selectedIndent.id);
                return newSet;
            });
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    // Priority badge variant
    const getPriorityVariant = (priority: string): string => {
        const variants: Record<string, string> = {
            LOW: 'secondary',
            MEDIUM: 'info',
            HIGH: 'warning',
            URGENT: 'danger',
        };
        return variants[priority] || 'secondary';
    };

    const getStatusVariant = (indent: Indent): string => {
        return INDENT_STATUS_COLORS[indent.displayStatus ?? ''] ?? 'secondary';
    };

    const getStatusName = (indent: Indent): string => {
        return indent.displayStatus ?? 'Unknown';
    };

    const handleApprove = (indent: Indent) => {
        setSelectedIndent(indent);
        setShowApproveModal(true);
    };

    const handleReject = (indent: Indent) => {
        setSelectedIndent(indent);
        setShowRejectModal(true);
    };

    const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.checked && data?.content) {
            const allIds = data.content.map(i => i.id);
            setSelectedIds(new Set(allIds));
        } else {
            setSelectedIds(new Set());
        }
    };

    const handleSelectOne = (id: number) => {
        const newSet = new Set(selectedIds);
        if (newSet.has(id)) {
            newSet.delete(id);
        } else {
            newSet.add(id);
        }
        setSelectedIds(newSet);
    };

    const confirmApprove = () => {
        if (selectedIndent) {
            approveMutation.mutate(selectedIndent.id);
        }
    };

    const confirmBulkApprove = () => {
        if (selectedIds.size > 0) {
            bulkApproveMutation.mutate(Array.from(selectedIds));
        }
    }

    const confirmReject = () => {
        if (selectedIndent && rejectionReason.trim()) {
            rejectMutation.mutate(selectedIndent.id);
        }
    };

    const indents = data?.content || [];
    const totalElements = data?.totalElements || 0;
    const totalPages = data?.totalPages || 0;

    // Determine approval level based on user roles
    const getApprovalLevel = (): string => {
        if (hasAnyRole(['SUPERADMIN', 'ADMIN'])) return 'All Levels';
        if (hasAnyRole(['INDENT_L3_APPROVER', 'PLANTMANAGER'])) return 'Level 3 (Final)';
        if (hasAnyRole(['INDENT_L2_APPROVER', 'DEPTHEAD'])) return 'Level 2 (Department)';
        if (hasAnyRole(['SUPERVISOR'])) return 'Level 1 (RM Review)';
        if (hasAnyRole(['INDENT_L1_APPROVER'])) return 'Level 1 (Manager)';
        return 'Approval Queue';
    };

    return (
        <div>
            <PageHeader
                title="Indent Approval Queue"
                subtitle={`${getApprovalLevel()} - ${totalElements} pending approval${totalElements !== 1 ? 's' : ''}`}
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Indents', path: '/indents' },
                    { label: 'Approvals' },
                ]}
                actions={
                    <div className="d-flex gap-2">
                        {selectedIds.size > 0 && (
                            <Button
                                variant="success"
                                onClick={() => setShowBulkApproveModal(true)}
                            >
                                <FaCheckDouble className="me-2" />
                                Approve Selected ({selectedIds.size})
                            </Button>
                        )}
                        <Button
                            variant="outline-primary"
                            onClick={() => refetch()}
                            disabled={isFetching}
                        >
                            <FaSync className={`me-2 ${isFetching ? 'fa-spin' : ''}`} />
                            Refresh
                        </Button>
                    </div>
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
                        <Col md={6}>
                            <InputGroup>
                                <InputGroup.Text>
                                    <FaSearch />
                                </InputGroup.Text>
                                <Form.Control
                                    type="text"
                                    placeholder="Search by indent number, purpose, or requester..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </InputGroup>
                        </Col>
                        <Col md={3}>
                            <InputGroup>
                                <InputGroup.Text>
                                    <FaFilter />
                                </InputGroup.Text>
                                <Form.Select
                                    value={priorityFilter}
                                    onChange={(e) => setPriorityFilter(e.target.value)}
                                >
                                    <option value="">All Priorities</option>
                                    <option value="LOW">Low</option>
                                    <option value="MEDIUM">Medium</option>
                                    <option value="HIGH">High</option>
                                    <option value="URGENT">Urgent</option>
                                </Form.Select>
                            </InputGroup>
                        </Col>
                        <Col md={3}>
                            <Button
                                variant="outline-secondary"
                                className="w-100"
                                onClick={() => {
                                    setSearchTerm('');
                                    setPriorityFilter('');
                                    setPage(0);
                                    setSelectedIds(new Set());
                                }}
                            >
                                Clear Filters
                            </Button>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            {/* Approval Queue */}
            <Card>
                <Card.Header className="d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">
                        <FaClipboardCheck className="me-2" />
                        Pending Approvals
                    </h5>
                    <Badge bg="warning" className="fs-6">
                        {totalElements} Pending
                    </Badge>
                </Card.Header>
                <Card.Body className="p-0">
                    {isLoading ? (
                        <LoadingSpinner text="Loading pending approvals..." />
                    ) : indents.length === 0 ? (
                        <div className="text-center py-5">
                            <FaClipboardCheck size={48} className="text-muted mb-3" />
                            <h5>No Pending Approvals</h5>
                            <p className="text-muted">
                                All indents requiring your approval have been processed.
                            </p>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="mb-0">
                                <thead className="bg-light">
                                    <tr>
                                        <th style={{ width: 40 }}>
                                            <Form.Check
                                                type="checkbox"
                                                checked={indents.length > 0 && selectedIds.size === indents.length}
                                                onChange={handleSelectAll}
                                            />
                                        </th>
                                        <th>Indent #</th>
                                        <th>Requester</th>
                                        <th>Department</th>
                                        <th>Purpose</th>
                                        <th>Priority</th>
                                        <th>Status</th>
                                        <th className="text-end">Value (₹)</th>
                                        <th>Requested</th>
                                        <th className="text-center">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {indents.map((indent) => (
                                        <tr key={indent.id} className={selectedIds.has(indent.id) ? 'table-active' : ''}>
                                            <td>
                                                <Form.Check
                                                    type="checkbox"
                                                    checked={selectedIds.has(indent.id)}
                                                    onChange={() => handleSelectOne(indent.id)}
                                                />
                                            </td>
                                            <td>
                                                <Button
                                                    variant="link"
                                                    className="p-0 text-decoration-none"
                                                    onClick={() => navigate(`/indents/${indent.id}`)}
                                                >
                                                    {indent.indentNumber}
                                                </Button>
                                            </td>
                                            <td>{indent.requestedByName}</td>
                                            <td>{indent.departmentName}</td>
                                            <td className="text-truncate" style={{ maxWidth: 200 }}>
                                                {indent.purpose}
                                            </td>
                                            <td>
                                                <Badge bg={getPriorityVariant(indent.priority || 'LOW')}>
                                                    {indent.priority || 'N/A'}
                                                </Badge>
                                            </td>
                                            <td>
                                                <Badge bg={getStatusVariant(indent)}>
                                                    {getStatusName(indent)}
                                                </Badge>
                                            </td>
                                            <td className="text-end">
                                                {new Intl.NumberFormat('en-IN').format(
                                                    indent.totalEstimatedValue || 0
                                                )}
                                            </td>
                                            <td>
                                                {indent.createdAt ? format(new Date(indent.createdAt), 'dd MMM yyyy') : 'N/A'}
                                            </td>
                                            <td>
                                                <ButtonGroup size="sm">
                                                    <Button
                                                        variant="outline-primary"
                                                        title="View Details"
                                                        onClick={() => navigate(`/indents/${indent.id}`)}
                                                    >
                                                        <FaEye />
                                                    </Button>
                                                    <Button
                                                        variant="success"
                                                        title="Approve"
                                                        onClick={() => handleApprove(indent)}
                                                    >
                                                        <FaCheck />
                                                    </Button>
                                                    <Button
                                                        variant="danger"
                                                        title="Reject"
                                                        onClick={() => handleReject(indent)}
                                                    >
                                                        <FaTimes />
                                                    </Button>
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
                        Approve Indent
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedIndent && (
                        <>
                            <Alert variant="info" className="mb-3">
                                <strong>Indent:</strong> {selectedIndent.indentNumber}
                                <br />
                                <strong>Requester:</strong> {selectedIndent.requestedByName}
                                <br />
                                <strong>Value:</strong>{' '}
                                {new Intl.NumberFormat('en-IN', {
                                    style: 'currency',
                                    currency: 'INR',
                                }).format(selectedIndent.totalEstimatedValue || 0)}
                            </Alert>
                            <Form.Group>
                                <Form.Label>Approval Remarks (Optional)</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    value={approvalRemarks}
                                    onChange={(e) => setApprovalRemarks(e.target.value)}
                                    placeholder="Add any comments for the requester..."
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
                        Approve Indent
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* Bulk Approve Modal */}
            <Modal show={showBulkApproveModal} onHide={() => setShowBulkApproveModal(false)} centered>
                <Modal.Header closeButton className="bg-success text-white">
                    <Modal.Title>
                        <FaCheckDouble className="me-2" />
                        Bulk Approve Indents
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Alert variant="info">
                        You are about to approve <strong>{selectedIds.size} selected indents</strong>.
                        This action cannot be undone efficiently.
                    </Alert>
                    <Form.Group>
                        <Form.Label>Common Approval Remarks (Optional)</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={3}
                            value={bulkApprovalRemarks}
                            onChange={(e) => setBulkApprovalRemarks(e.target.value)}
                            placeholder="Enter remarks applicable to all selected indents..."
                        />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowBulkApproveModal(false)}>Cancel</Button>
                    <Button
                        variant="success"
                        onClick={confirmBulkApprove}
                        disabled={bulkApproveMutation.isPending}
                    >
                        {bulkApproveMutation.isPending && (
                            <Spinner as="span" animation="border" size="sm" className="me-2" />
                        )}
                        Approve All ({selectedIds.size})
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* Reject Modal */}
            <Modal show={showRejectModal} onHide={() => setShowRejectModal(false)} centered>
                <Modal.Header closeButton className="bg-danger text-white">
                    <Modal.Title>
                        <FaTimes className="me-2" />
                        Reject Indent
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedIndent && (
                        <>
                            <Alert variant="warning" className="mb-3">
                                <strong>Indent:</strong> {selectedIndent.indentNumber}
                                <br />
                                <strong>Requester:</strong> {selectedIndent.requestedByName}
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
                                <Form.Text className="text-muted">
                                    This reason will be visible to the requester.
                                </Form.Text>
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
                        Reject Indent
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default IndentApprovalPage;
