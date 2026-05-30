import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Card,
    Row,
    Col,
    Button,
    Table,
    Badge,
    Modal,
    Form,
    Alert,
    Spinner,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    FaArrowLeft,
    FaEdit,
    FaFileContract,
    FaHistory,
    FaCheckCircle,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner, StatusBadge } from '../../components/common';
import { purchaseOrdersApi, getErrorMessage } from '../../api';
import { POStatus, AmendPORequest } from '../../api/purchaseOrders';

const POAmendmentPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [showAmendModal, setShowAmendModal] = useState(false);
    const [selectedField, setSelectedField] = useState<{
        key: string;
        label: string;
        currentValue: string;
        type: 'text' | 'date' | 'number';
    } | null>(null);

    const [newValue, setNewValue] = useState('');
    const [reason, setReason] = useState('');
    const [error, setError] = useState<string | null>(null);

    // Fetch PO details
    const { data: po, isLoading } = useQuery({
        queryKey: ['po', id],
        queryFn: () => purchaseOrdersApi.getById(Number(id)),
        enabled: !!id,
    });

    // Fetch amendment history — returns POAmendmentHistory wrapper
    const { data: amendmentHistory } = useQuery({
        queryKey: ['po-amendments', id],
        queryFn: () => purchaseOrdersApi.getAmendments(Number(id)),
        enabled: !!id,
    });
    const amendments = amendmentHistory?.amendments ?? [];

    // Amendment mutation — sends AmendPORequest (full field values, not fieldName/newValue pair)
    const amendMutation = useMutation({
        mutationFn: (data: AmendPORequest) =>
            purchaseOrdersApi.amend(Number(id), data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['po', id] });
            queryClient.invalidateQueries({ queryKey: ['po-amendments', id] });
            setShowAmendModal(false);
            setNewValue('');
            setReason('');
            setError(null);
        },
        onError: (err) => {
            setError(getErrorMessage(err));
        },
    });

    const handleAmendClick = (
        field: string,
        label: string,
        value: string,
        type: 'text' | 'date' | 'number' = 'text'
    ) => {
        setSelectedField({ key: field, label, currentValue: value, type });
        setNewValue(value);
        setReason('');
        setError(null);
        setShowAmendModal(true);
    };

    const handleSubmitAmendment = () => {
        if (!selectedField || !newValue.trim() || !reason.trim()) return;
        if (reason.trim().length < 10) {
            setError('Amendment reason must be at least 10 characters');
            return;
        }

        // Build AmendPORequest by mapping the selected field key to the correct request field
        const request: AmendPORequest = { amendmentReason: reason.trim() };
        switch (selectedField.key) {
            case 'deliveryDate':     request.deliveryDate = newValue; break;
            case 'deliveryAddress':  request.deliveryAddress = newValue; break;
            case 'paymentTerms':     request.paymentTerms = newValue; break;
            case 'termsConditions':  request.termsConditions = newValue; break;
            case 'notes':            request.notes = newValue; break;
            case 'priority':         request.priority = newValue; break;
            default:
                setError(`Field "${selectedField.key}" is not amendable`);
                return;
        }
        amendMutation.mutate(request);
    };

    if (isLoading) return <LoadingSpinner />;
    if (!po) return <Alert variant="danger">Purchase Order not found</Alert>;

    // Amendable when APPROVED, SENT_TO_VENDOR, or PARTIALLY_RECEIVED
    const isAmendable = [
        POStatus.APPROVED,
        POStatus.SENT_TO_VENDOR,
        POStatus.PARTIALLY_RECEIVED,
    ].includes(po.poStatus);

    return (
        <div>
            <PageHeader
                title={`Amend PO: ${po.poNumber}`}
                subtitle="Request changes to confirmed Purchase Orders"
                breadcrumbs={[
                    { label: 'Purchase Orders', path: '/purchase-orders' },
                    { label: po.poNumber, path: `/purchase-orders/${id}` },
                    { label: 'Amend' },
                ]}
                actions={
                    <Button variant="outline-secondary" onClick={() => navigate(`/purchase-orders/${id}`)}>
                        <FaArrowLeft className="me-2" /> Back to PO
                    </Button>
                }
            />

            {!isAmendable && (
                <Alert variant="warning" className="mb-4">
                    <FaHistory className="me-2" />
                    This Purchase Order cannot be amended in its current status ({po.poStatusName ?? String(po.poStatus)}).
                    Only Approved, Sent-to-Vendor, or Partially-Received POs can be amended.
                </Alert>
            )}

            <Row>
                <Col lg={8}>
                    {/* Amendable Fields Card */}
                    <Card className="mb-4">
                        <Card.Header>
                            <h5 className="mb-0">
                                <FaFileContract className="me-2" />
                                Amendable Details
                            </h5>
                        </Card.Header>
                        <Card.Body>
                            <Table responsive hover>
                                <thead>
                                    <tr>
                                        <th>Field</th>
                                        <th>Current Value</th>
                                        <th style={{ width: '100px' }}>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Delivery Date</td>
                                        <td>
                                            {po.deliveryDate
                                                ? new Date(po.deliveryDate).toLocaleDateString()
                                                : po.expectedDeliveryDate
                                                ? new Date(po.expectedDeliveryDate).toLocaleDateString()
                                                : '-'}
                                        </td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                disabled={!isAmendable}
                                                onClick={() => handleAmendClick(
                                                    'deliveryDate',
                                                    'Delivery Date',
                                                    (po.deliveryDate ?? po.expectedDeliveryDate ?? '').split('T')[0],
                                                    'date'
                                                )}
                                            >
                                                <FaEdit /> Amend
                                            </Button>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Payment Terms</td>
                                        <td>{po.paymentTerms ?? '-'}</td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                disabled={!isAmendable}
                                                onClick={() => handleAmendClick('paymentTerms', 'Payment Terms', po.paymentTerms ?? '')}
                                            >
                                                <FaEdit /> Amend
                                            </Button>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Terms & Conditions</td>
                                        <td>{po.termsConditions ?? '-'}</td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                disabled={!isAmendable}
                                                onClick={() => handleAmendClick('termsConditions', 'Terms & Conditions', po.termsConditions ?? '')}
                                            >
                                                <FaEdit /> Amend
                                            </Button>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Delivery Address</td>
                                        <td>{po.deliveryAddress ?? '-'}</td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                disabled={!isAmendable}
                                                onClick={() => handleAmendClick('deliveryAddress', 'Delivery Address', po.deliveryAddress ?? '')}
                                            >
                                                <FaEdit /> Amend
                                            </Button>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Notes</td>
                                        <td>{po.notes ?? '-'}</td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                disabled={!isAmendable}
                                                onClick={() => handleAmendClick('notes', 'Notes', po.notes ?? '')}
                                            >
                                                <FaEdit /> Amend
                                            </Button>
                                        </td>
                                    </tr>
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>

                    {/* Amendment History */}
                    <Card>
                        <Card.Header>
                            <h5 className="mb-0">
                                <FaHistory className="me-2" />
                                Amendment History
                                {amendmentHistory && (
                                    <Badge bg="secondary" className="ms-2">{amendmentHistory.totalAmendments}</Badge>
                                )}
                            </h5>
                        </Card.Header>
                        <Card.Body>
                            {amendments.length > 0 ? (
                                <div className="table-responsive">
                                    <Table>
                                        <thead>
                                            <tr>
                                                <th>Version</th>
                                                <th>Field</th>
                                                <th>Old Value</th>
                                                <th>New Value</th>
                                                <th>Reason</th>
                                                <th>Date</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {amendments.map((amendment) => (
                                                <tr key={amendment.id}>
                                                    <td>
                                                        <Badge bg="secondary">v{amendment.amendmentVersion}</Badge>
                                                    </td>
                                                    <td className="fw-medium">{amendment.fieldName}</td>
                                                    <td className="text-muted"><small>{amendment.originalValue}</small></td>
                                                    <td className="text-primary"><small>{amendment.amendedValue}</small></td>
                                                    <td>{amendment.amendmentReason}</td>
                                                    <td>{new Date(amendment.amendedDate).toLocaleDateString()}</td>
                                                    <td>
                                                        <Badge bg="success">{amendment.status}</Badge>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </Table>
                                </div>
                            ) : (
                                <div className="text-center py-4 text-muted">
                                    No amendments have been made to this Purchase Order.
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                <Col lg={4}>
                    {/* PO Summary Card */}
                    <Card className="mb-4 bg-light">
                        <Card.Body>
                            <h6 className="text-muted text-uppercase mb-3">PO Summary</h6>
                            <div className="mb-3">
                                <label className="text-muted small d-block">Vendor</label>
                                <strong>{po.vendorName}</strong>
                            </div>
                            <div className="mb-3">
                                <label className="text-muted small d-block">PO Date</label>
                                <strong>{new Date(po.poDate).toLocaleDateString()}</strong>
                            </div>
                            <div className="mb-3">
                                <label className="text-muted small d-block">Net Amount</label>
                                <div className="h4 text-primary">
                                    {new Intl.NumberFormat('en-IN', {
                                        style: 'currency',
                                        currency: 'INR',
                                    }).format(po.netAmount)}
                                </div>
                            </div>
                            <div className="mb-3">
                                <label className="text-muted small d-block">Status</label>
                                <StatusBadge status={po.poStatusName ?? String(po.poStatus)} />
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            {/* Amendment Modal */}
            <Modal show={showAmendModal} onHide={() => setShowAmendModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Amend {selectedField?.label}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {error && <Alert variant="danger">{error}</Alert>}
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Current Value</Form.Label>
                            <Form.Control
                                type="text"
                                value={selectedField?.currentValue}
                                disabled
                                className="bg-light"
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>New Value <span className="text-danger">*</span></Form.Label>
                            <Form.Control
                                type={selectedField?.type || 'text'}
                                value={newValue}
                                onChange={(e) => setNewValue(e.target.value)}
                                autoFocus
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>
                                Reason for Amendment <span className="text-danger">*</span>
                                <small className="text-muted ms-1">(min. 10 characters)</small>
                            </Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="Please explain why this change is required..."
                            />
                            <Form.Text className="text-muted">
                                {reason.length}/10 minimum characters
                            </Form.Text>
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowAmendModal(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleSubmitAmendment}
                        disabled={!newValue.trim() || !reason.trim() || reason.trim().length < 10 || amendMutation.isPending}
                    >
                        {amendMutation.isPending ? (
                            <>
                                <Spinner as="span" animation="border" size="sm" className="me-2" />
                                Submitting...
                            </>
                        ) : (
                            <>
                                <FaCheckCircle className="me-2" />
                                Submit Amendment
                            </>
                        )}
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default POAmendmentPage;
