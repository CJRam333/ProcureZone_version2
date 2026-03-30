import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Card,
    Row,
    Col,
    Button,
    Table,
    Form,
    Alert,
    Badge,
    Modal,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaArrowLeft, FaCheckCircle, FaTimesCircle, FaClipboardCheck } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { grnApi, getErrorMessage } from '../../api';
import { GRNStatus } from '../../api/grn';

// For single-item GRN structure
interface QCState {
    receivedQuantity: number;
    acceptedQuantity: number;
    rejectedQuantity: number;
    rejectionReason: string;
}

const GRNInspectionPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    // Single-item QC state (since GRN is single-item based)
    const [qcState, setQcState] = useState<QCState | null>(null);
    const [globalRemarks, setGlobalRemarks] = useState('');
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [rejectReason, setRejectReason] = useState('');
    const [error, setError] = useState<string | null>(null);

    // Fetch GRN details
    const { data: grn, isLoading } = useQuery({
        queryKey: ['grn', id],
        queryFn: () => grnApi.getById(Number(id)),
        enabled: !!id,
    });

    // Initialize form state when GRN loads (single-item structure)
    useEffect(() => {
        if (grn) {
            setQcState({
                receivedQuantity: grn.receivedQuantity,
                acceptedQuantity: grn.receivedQuantity, // Default to full acceptance
                rejectedQuantity: 0,
                rejectionReason: '',
            });
        }
    }, [grn]);

    // QC Approve Mutation
    const approveMutation = useMutation({
        mutationFn: (data: any) => grnApi.approveQC(Number(id), data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['grn', id] });
            navigate(`/grn/${id}`);
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    // QC Reject Mutation (Full Rejection)
    const rejectMutation = useMutation({
        mutationFn: (data: any) => grnApi.rejectQC(Number(id), data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['grn', id] });
            navigate(`/grn/${id}`);
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    const handleQuantityChange = (field: 'accepted' | 'rejected', value: string) => {
        if (!qcState) return;
        const numValue = Number(value) || 0;

        // Initial validation against negative numbers
        if (numValue < 0) return;

        if (field === 'accepted') {
            const newAccepted = Math.min(numValue, qcState.receivedQuantity);
            setQcState(prev => prev ? ({
                ...prev,
                acceptedQuantity: newAccepted,
                rejectedQuantity: prev.receivedQuantity - newAccepted,
            }) : null);
        } else {
            const newRejected = Math.min(numValue, qcState.receivedQuantity);
            setQcState(prev => prev ? ({
                ...prev,
                rejectedQuantity: newRejected,
                acceptedQuantity: prev.receivedQuantity - newRejected,
            }) : null);
        }
    };

    const handleReasonChange = (reason: string) => {
        setQcState(prev => prev ? ({
            ...prev,
            rejectionReason: reason
        }) : null);
    };

    const validateQC = (): boolean => {
        if (!qcState) return false;
        // Check if rejected quantity has a reason
        if (qcState.rejectedQuantity > 0 && !qcState.rejectionReason.trim()) {
            setError('Please provide a rejection reason for the rejected quantity');
            return false;
        }
        return true;
    };

    const handleSubmit = () => {
        if (!validateQC() || !qcState) return;

        // Single-item payload
        approveMutation.mutate({
            acceptedQuantity: qcState.acceptedQuantity,
            rejectedQuantity: qcState.rejectedQuantity,
            rejectionReason: qcState.rejectedQuantity > 0 ? qcState.rejectionReason : undefined,
            remarks: globalRemarks,
        });
    };

    const handleRejectAll = () => {
        // Single-item rejection
        rejectMutation.mutate({
            rejectionReason: rejectReason || 'Full Rejection',
            remarks: rejectReason,
        });
        setShowRejectModal(false);
    };

    if (isLoading) return <LoadingSpinner />;
    if (!grn) return <Alert variant="danger">GRN not found</Alert>;

    if (grn.status !== GRNStatus.CREATED) {
        return (
            <Alert variant="warning">
                QC Inspection is not available for this GRN status ({grn.statusName}).
                <Button variant="link" onClick={() => navigate(`/grn/${id}`)}>Back to details</Button>
            </Alert>
        );
    }

    return (
        <div>
            <PageHeader
                title={`QC Inspection: ${grn.grnNumber}`}
                subtitle="Inspect materials and record accepted/rejected quantities"
                breadcrumbs={[
                    { label: 'GRN', path: '/grn' },
                    { label: grn.grnNumber, path: `/grn/${id}` },
                    { label: 'Inspection' },
                ]}
                actions={
                    <Button variant="outline-secondary" onClick={() => navigate(`/grn/${id}`)}>
                        <FaArrowLeft className="me-2" /> Cancel
                    </Button>
                }
            />

            {error && <Alert variant="danger" onClose={() => setError(null)} dismissible>{error}</Alert>}

            <Card className="mb-4">
                <Card.Body>
                    <div className="table-responsive">
                        <Table bordered hover>
                            <thead className="bg-light">
                                <tr>
                                    <th>Material Code & Desc</th>
                                    <th>UOM</th>
                                    <th className="text-end" style={{ width: '100px' }}>Received</th>
                                    <th style={{ width: '120px' }}>Accepted</th>
                                    <th style={{ width: '120px' }}>Rejected</th>
                                    <th>Rejection Reason</th>
                                </tr>
                            </thead>
                            <tbody>
                                {qcState && (
                                    <tr className={qcState.rejectedQuantity > 0 ? 'table-warning' : ''}>
                                        <td>
                                            <div className="fw-bold">{grn.grnNumber}</div>
                                            <small className="text-muted">{grn.vendorName}</small>
                                        </td>
                                        <td>-</td>
                                        <td className="text-end fw-medium">{qcState.receivedQuantity}</td>
                                        <td>
                                            <Form.Control
                                                type="number"
                                                min="0"
                                                max={qcState.receivedQuantity}
                                                value={qcState.acceptedQuantity}
                                                onChange={(e) => handleQuantityChange('accepted', e.target.value)}
                                                className={qcState.acceptedQuantity === qcState.receivedQuantity ? 'border-success' : ''}
                                            />
                                        </td>
                                        <td>
                                            <Form.Control
                                                type="number"
                                                min="0"
                                                max={qcState.receivedQuantity}
                                                value={qcState.rejectedQuantity}
                                                onChange={(e) => handleQuantityChange('rejected', e.target.value)}
                                                className={qcState.rejectedQuantity > 0 ? 'border-danger text-danger fw-bold' : ''}
                                            />
                                        </td>
                                        <td>
                                            <Form.Control
                                                type="text"
                                                placeholder={qcState.rejectedQuantity > 0 ? "Reason required *" : "Optional remarks"}
                                                value={qcState.rejectionReason}
                                                onChange={(e) => handleReasonChange(e.target.value)}
                                                disabled={qcState.rejectedQuantity === 0}
                                                isInvalid={qcState.rejectedQuantity > 0 && !qcState.rejectionReason}
                                            />
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </Table>
                    </div>
                </Card.Body>
            </Card>

            <Card>
                <Card.Body>
                    <Form.Group className="mb-4">
                        <Form.Label>Inspection Remarks</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={3}
                            value={globalRemarks}
                            onChange={(e) => setGlobalRemarks(e.target.value)}
                            placeholder="Overall comments for this inspection..."
                        />
                    </Form.Group>

                    <div className="d-flex justify-content-between">
                        <Button
                            variant="outline-danger"
                            onClick={() => setShowRejectModal(true)}
                            disabled={rejectMutation.isPending || approveMutation.isPending}
                        >
                            <FaTimesCircle className="me-2" /> Reject Entire GRN
                        </Button>

                        <Button
                            variant="success"
                            onClick={handleSubmit}
                            disabled={rejectMutation.isPending || approveMutation.isPending}
                            size="lg"
                        >
                            {approveMutation.isPending ? (
                                <LoadingSpinner text="Submitting..." size="sm" />
                            ) : (
                                <>
                                    <FaCheckCircle className="me-2" /> Submit QC Results
                                </>
                            )}
                        </Button>
                    </div>
                </Card.Body>
            </Card>

            {/* Reject Entire GRN Modal */}
            <Modal show={showRejectModal} onHide={() => setShowRejectModal(false)}>
                <Modal.Header closeButton className="bg-danger text-white">
                    <Modal.Title>Reject Entire GRN?</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Alert variant="warning">
                        Are you sure you want to reject the entire received quantity for ALL items in this GRN?
                        This action cannot be undone.
                    </Alert>
                    <Form.Group>
                        <Form.Label>Rejection Reason <span className="text-danger">*</span></Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={3}
                            value={rejectReason}
                            onChange={(e) => setRejectReason(e.target.value)}
                            placeholder="Please explain why the entire shipment is being rejected..."
                        />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowRejectModal(false)}>Cancel</Button>
                    <Button
                        variant="danger"
                        onClick={handleRejectAll}
                        disabled={!rejectReason.trim()}
                    >
                        Confirm Rejection
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default GRNInspectionPage;
