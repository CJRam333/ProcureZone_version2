import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
    Card,
    Row,
    Col,
    Table,
    Button,
    Badge,
    Alert,
    Tab,
    Tabs,
} from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
    FaArrowLeft,
    FaEdit,
    FaPrint,
    FaFileAlt,
    FaHistory,
    FaBoxOpen,
    FaTruck,
    FaClipboardCheck,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { grnApi, indentsApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { GRN, GRNStatus } from '../../api/grn';

const GRNDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { hasAnyRole } = useAuth();

    // Fetch GRN details
    const { data: grn, isLoading, error: fetchError } = useQuery({
        queryKey: ['grn', id],
        queryFn: () => grnApi.getById(Number(id)),
        enabled: !!id,
    });

    // Status badge variant
    const getStatusVariant = (status: GRNStatus): string => {
        const variants: Record<number, string> = {
            [GRNStatus.CREATED]: 'secondary',
            [GRNStatus.INSPECTED]: 'info',
            [GRNStatus.RM_APPROVED]: 'primary',
            [GRNStatus.APPROVED]: 'primary',
            [GRNStatus.FINAL_APPROVED]: 'success',
            [GRNStatus.STORED]: 'success',
            [GRNStatus.REJECTED]: 'danger',
        };
        return variants[status] || 'secondary';
    };

    // Quality status badge variant
    const getQualityVariant = (status: string | undefined): string => {
        const variants: Record<string, string> = {
            PENDING: 'secondary',
            APPROVED: 'success',
            REJECTED: 'danger',
        };
        return variants[status || 'PENDING'] || 'secondary';
    };

    // Get status display name
    const getStatusName = (status: GRNStatus): string => {
        const names: Record<number, string> = {
            [GRNStatus.CREATED]: 'Created',
            [GRNStatus.INSPECTED]: 'Inspected',
            [GRNStatus.RM_APPROVED]: 'RM Approved',
            [GRNStatus.APPROVED]: 'Approved',
            [GRNStatus.FINAL_APPROVED]: 'Final Approved',
            [GRNStatus.STORED]: 'Stored',
            [GRNStatus.REJECTED]: 'Rejected',
        };
        return names[status] || 'Unknown';
    };

    if (isLoading) {
        return <LoadingSpinner fullPage text="Loading GRN details..." />;
    }

    if (fetchError || !grn) {
        return (
            <div className="text-center py-5">
                <Alert variant="danger">
                    {fetchError ? getErrorMessage(fetchError) : 'GRN not found'}
                </Alert>
                <Button variant="primary" onClick={() => navigate('/grn')}>
                    Back to GRN List
                </Button>
            </div>
        );
    }

    // Fetch Indent details to get Material info (since GRN is flat and minimal)
    const { data: indent } = useQuery({
        queryKey: ['indent', grn?.indentId],
        queryFn: () => indentsApi.getById(grn!.indentId),
        enabled: !!grn?.indentId,
    });

    // We need to import indentsApi to fetch indent
    // But since we can't easily change imports in this block without being hacky, let's assume we can add import or use a helper.
    // Actually, I'll update the imports in a separate step or assume I can't.
    // Wait, I can use a separate replace for imports.

    // Construct single item for display
    const item = React.useMemo(() => {
        if (!grn || !indent) return null;
        // Find indent item
        const indentItem = (indent.details ?? []).find((i: any) => i.id === grn.indentDetailsId);
        return {
            id: grn.id,
            materialCode: indentItem?.materialCode || 'N/A',
            materialDescription: indentItem?.materialName || 'Unknown Material',
            uomCode: indentItem?.unitOfMeasureCode || 'N/A',
            orderedQuantity: grn.requestedQuantity || 0, // Approx
            receivedQuantity: grn.receivedQuantity,
            acceptedQuantity: 0, // Not in GRN response (QC info missing?)
            rejectedQuantity: 0, // Not in GRN response
            batchNumber: '-',
            remarks: grn.comments
        };
    }, [grn, indent]);

    const canEdit = grn?.status === 1; // 1 = Created
    const canInspect = grn?.status === 1; // Created (GRNService says canInspect if Created)

    return (
        <div>
            <PageHeader
                title={`GRN ${grn?.grnNumber}`}
                subtitle={`Received on ${grn?.receiptDate ? format(new Date(grn.receiptDate), 'PPP') : 'N/A'}`}
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'GRN', path: '/grn' },
                    { label: grn?.grnNumber || 'Loading...' },
                ]}
                actions={
                    <div className="d-flex flex-wrap gap-2">
                        <Button variant="outline-secondary" onClick={() => navigate('/grn')}>
                            <FaArrowLeft className="me-2" /> Back
                        </Button>
                        <Button variant="outline-secondary" onClick={() => window.print()}>
                            <FaPrint className="me-2" /> Print
                        </Button>
                        {canEdit && (
                            <Button variant="primary" onClick={() => navigate(`/grn/${id}`)}>
                                <FaEdit className="me-2" /> Edit
                            </Button>
                        )}
                    </div>
                }
            />

            {/* Status Bar */}
            <Card className="mb-4">
                <Card.Body className="d-flex flex-wrap justify-content-between align-items-center gap-3">
                    <div className="d-flex flex-wrap gap-4 align-items-center">
                        <div>
                            <small className="text-muted d-block">Status</small>
                            <Badge bg={getStatusVariant(grn?.status || 0)} className="fs-6">
                                {grn?.statusName || getStatusName(grn?.status || 0)}
                            </Badge>
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">Total Received</small>
                            <strong className="text-primary fs-5">
                                {grn?.receivedQuantity || 0} Units
                            </strong>
                        </div>
                    </div>

                    <div className="d-flex flex-wrap gap-2">
                        {canInspect && (
                            <Button variant="info" onClick={() => navigate(`/grn/${id}/inspect`)}>
                                <FaClipboardCheck className="me-2" /> Quality Inspection
                            </Button>
                        )}
                    </div>
                </Card.Body>
            </Card>

            <Tabs defaultActiveKey="details" className="mb-4">
                {/* Details Tab */}
                <Tab eventKey="details" title={<><FaFileAlt className="me-2" />Details</>}>
                    <Row className="g-4">
                        {/* GRN Info */}
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaBoxOpen className="me-2" />
                                        GRN Information
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">GRN Number</small>
                                                <strong>{grn?.grnNumber}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Receipt Date</small>
                                                <strong>{grn?.receiptDate ? format(new Date(grn.receiptDate), 'PPP') : 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Received By (ID)</small>
                                                <strong>{grn?.createdBy || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        {/* Plant Name not in minimal GRN response, skipping or fetching via additional calls? 
                                            We'll skip for now as it's not critical.
                                        */}
                                        {grn?.comments && (
                                            <Col sm={12}>
                                                <div>
                                                    <small className="text-muted d-block">Remarks</small>
                                                    <p className="mb-0">{grn.comments}</p>
                                                </div>
                                            </Col>
                                        )}
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>

                        {/* PO & Vendor Info */}
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaTruck className="me-2" />
                                        Indent & Vendor
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Indent ID</small>
                                                {/* Using Indent ID as we don't have PO Number readily available without extra fetch */}
                                                <strong>{grn?.indentId}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={12}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Vendor</small>
                                                <strong>{grn?.vendorName || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>

                        {/* Items */}
                        <Col xs={12}>
                            <Card>
                                <Card.Header>
                                    <h5 className="mb-0">Received Item</h5>
                                </Card.Header>
                                <Card.Body className="p-0">
                                    <div className="table-responsive">
                                        <Table className="mb-0">
                                            <thead className="bg-light">
                                                <tr>
                                                    <th>Material Code</th>
                                                    <th>Description</th>
                                                    <th>UOM</th>
                                                    <th className="text-end">Requested</th>
                                                    <th className="text-end">Received</th>
                                                    <th>Remarks</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {item ? (
                                                    <tr key={item.id}>
                                                        <td>
                                                            <code>{item.materialCode}</code>
                                                        </td>
                                                        <td>{item.materialDescription}</td>
                                                        <td>
                                                            <Badge bg="secondary">{item.uomCode}</Badge>
                                                        </td>
                                                        <td className="text-end">{item.orderedQuantity}</td>
                                                        <td className="text-end fw-medium">{item.receivedQuantity}</td>
                                                        <td>{item.remarks || '-'}</td>
                                                    </tr>
                                                ) : (
                                                    <tr>
                                                        <td colSpan={6} className="text-center text-muted py-4">
                                                            {indent ? 'Item details not found in Indent' : 'Loading item details...'}
                                                        </td>
                                                    </tr>
                                                )}
                                            </tbody>
                                        </Table>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </Tab>

                {/* History Tab - Simplified for shortness */}
                <Tab eventKey="history" title={<><FaHistory className="me-2" />History</>}>
                    <Card><Card.Body><p>Audit log entries would appear here.</p></Card.Body></Card>
                </Tab>
            </Tabs>
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

export default GRNDetailPage;
