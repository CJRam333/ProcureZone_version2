import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
    Card,
    Row,
    Col,
    Button,
    Badge,
    Alert,
    Tab,
    Tabs,
    Table,
} from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
    FaArrowLeft,
    FaEdit,
    FaPrint,
    FaBoxOpen,
    FaInfoCircle,
    FaHistory,
    FaWarehouse,
    FaLayerGroup,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { materialsApi, inventoryApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import type { Material } from '../../api/materials';

const MaterialDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { hasAnyRole } = useAuth();

    // Fetch material details
    const { data: material, isLoading, error: fetchError } = useQuery({
        queryKey: ['material', id],
        queryFn: () => materialsApi.getById(Number(id)),
        enabled: !!id,
    });

    // Fetch inventory for this material
    const { data: inventoryData } = useQuery({
        queryKey: ['material-inventory', id],
        queryFn: () => inventoryApi.list({ page: 0, size: 100 }),
        enabled: !!id,
    });

    const materialInventory = inventoryData?.content?.filter(
        (inv) => inv.materialId === Number(id)
    ) || [];

    const canEdit = hasAnyRole(['ADMIN', 'SUPERADMIN']);

    if (isLoading) {
        return <LoadingSpinner fullPage text="Loading material details..." />;
    }

    if (fetchError || !material) {
        return (
            <div className="text-center py-5">
                <Alert variant="danger">
                    {fetchError ? getErrorMessage(fetchError) : 'Material not found'}
                </Alert>
                <Button variant="primary" onClick={() => navigate('/materials')}>
                    Back to Materials
                </Button>
            </div>
        );
    }

    const totalStock = materialInventory.reduce(
        (sum, inv) => sum + (inv.quantity || 0),
        0
    );

    return (
        <div>
            <PageHeader
                title={material.materialCode || material.code || 'Material'}
                subtitle={material.description || ''}
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Materials', path: '/materials' },
                    { label: material.materialCode || material.code || 'Detail' },
                ]}
                actions={
                    <div className="d-flex flex-wrap gap-2">
                        <Button variant="outline-secondary" onClick={() => navigate('/materials')}>
                            <FaArrowLeft className="me-2" /> Back
                        </Button>
                        <Button variant="outline-secondary" onClick={() => window.print()}>
                            <FaPrint className="me-2" /> Print
                        </Button>
                        {canEdit && (
                            <Button variant="primary" onClick={() => navigate(`/materials/${id}/edit`)}>
                                <FaEdit className="me-2" /> Edit
                            </Button>
                        )}
                    </div>
                }
            />

            {/* Status Banner */}
            <Card className="mb-4">
                <Card.Body className="d-flex flex-wrap justify-content-between align-items-center gap-3">
                    <div className="d-flex flex-wrap align-items-center gap-4">
                        <div>
                            <small className="text-muted d-block">Status</small>
                            <Badge bg={material.isActive ? 'success' : 'secondary'} className="fs-6">
                                {material.isActive ? 'ACTIVE' : 'INACTIVE'}
                            </Badge>
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">Category</small>
                            <strong>{material.categoryName || 'N/A'}</strong>
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">UOM</small>
                            <Badge bg="info">{material.uomCode || 'N/A'}</Badge>
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">Total Stock</small>
                            <strong className="text-primary fs-5">{totalStock}</strong>
                        </div>
                    </div>
                </Card.Body>
            </Card>

            <Tabs defaultActiveKey="details" className="mb-4">
                {/* Details Tab */}
                <Tab eventKey="details" title={<><FaInfoCircle className="me-2" />Details</>}>
                    <Row className="g-4">
                        {/* Basic Info */}
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaBoxOpen className="me-2" />
                                        Material Information
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Material Code</small>
                                                <strong className="text-primary fs-5">{material.materialCode}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Status</small>
                                                <Badge bg={material.isActive ? 'success' : 'secondary'}>
                                                    {material.isActive ? 'ACTIVE' : 'INACTIVE'}
                                                </Badge>
                                            </div>
                                        </Col>
                                        <Col sm={12}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Description</small>
                                                <strong>{material.description || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Category</small>
                                                <strong>{material.categoryName || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Unit of Measure</small>
                                                <strong>{material.uomCode}</strong>
                                                {material.uomName && <span className="text-muted ms-2">({material.uomName})</span>}
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div>
                                                <small className="text-muted d-block">HSN Code</small>
                                                <strong>{material.hsnCode || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div>
                                                <small className="text-muted d-block">GST Rate</small>
                                                <strong>{material.gstRate ? `${material.gstRate}%` : 'N/A'}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>

                        {/* Stock Levels */}
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaLayerGroup className="me-2" />
                                        Stock Levels
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col sm={4}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Min Stock</small>
                                                <strong className="text-danger fs-5">{material.minStockLevel || 0}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={4}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Reorder Level</small>
                                                <strong className="text-warning fs-5">{material.reorderLevel || 0}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={4}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Max Stock</small>
                                                <strong className="text-info fs-5">{material.maxStockLevel || '-'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={12}>
                                            <div className="p-3 bg-light rounded">
                                                <div className="d-flex justify-content-between align-items-center">
                                                    <span className="text-muted">Total Across All Plants:</span>
                                                    <strong className="text-primary fs-4">{totalStock} {material.uomCode}</strong>
                                                </div>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </Tab>

                {/* Inventory Tab */}
                <Tab eventKey="inventory" title={<><FaWarehouse className="me-2" />Inventory</>}>
                    <Card>
                        <Card.Header className="d-flex justify-content-between align-items-center">
                            <h5 className="mb-0">Stock by Plant</h5>
                            <Badge bg="primary" className="fs-6">
                                Total: {totalStock} {material.uomCode}
                            </Badge>
                        </Card.Header>
                        <Card.Body className="p-0">
                            <div className="table-responsive">
                                <Table hover className="mb-0">
                                    <thead className="bg-light">
                                        <tr>
                                            <th>Plant</th>
                                            <th className="text-end">Quantity</th>
                                            <th className="text-end">Reserved</th>
                                            <th className="text-end">Available</th>
                                            <th>Status</th>
                                            <th>Last Updated</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {materialInventory.length > 0 ? (
                                            materialInventory.map((inv) => (
                                                <tr key={inv.id}>
                                                    <td>{inv.plantName}</td>
                                                    <td className="text-end fw-bold">{inv.quantity}</td>
                                                    <td className="text-end text-warning">{inv.reservedQuantity || 0}</td>
                                                    <td className="text-end text-success">{inv.availableQuantity || inv.quantity}</td>
                                                    <td>
                                                        {inv.quantity < (inv.minStockLevel || 0) ? (
                                                            <Badge bg="danger">Low Stock</Badge>
                                                        ) : inv.quantity < (inv.reorderLevel || 0) ? (
                                                            <Badge bg="warning">Reorder</Badge>
                                                        ) : (
                                                            <Badge bg="success">OK</Badge>
                                                        )}
                                                    </td>
                                                    <td>
                                                        {inv.updatedAt ? format(new Date(inv.updatedAt), 'dd MMM yyyy') : 'N/A'}
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan={6} className="text-center text-muted py-4">
                                                    No inventory records found for this material
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </Table>
                            </div>
                        </Card.Body>
                    </Card>
                </Tab>

                {/* History Tab */}
                <Tab eventKey="history" title={<><FaHistory className="me-2" />History</>}>
                    <Card>
                        <Card.Body>
                            <div className="timeline">
                                <div className="timeline-item">
                                    <div className="timeline-marker bg-primary"></div>
                                    <div className="timeline-content">
                                        <strong>Material Created</strong>
                                        <p className="text-muted mb-0">
                                            Added on {material.createdAt ? format(new Date(material.createdAt), 'PPpp') : 'N/A'}
                                        </p>
                                    </div>
                                </div>
                                {material.updatedAt && material.updatedAt !== material.createdAt && (
                                    <div className="timeline-item">
                                        <div className="timeline-marker bg-info"></div>
                                        <div className="timeline-content">
                                            <strong>Last Updated</strong>
                                            <p className="text-muted mb-0">
                                                Modified on {format(new Date(material.updatedAt), 'PPpp')}
                                            </p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </Card.Body>
                    </Card>
                </Tab>
            </Tabs>

            {/* Timeline Styles */}
            <style>{`
        .timeline { position: relative; padding-left: 30px; }
        .timeline::before { content: ''; position: absolute; left: 8px; top: 0; bottom: 0; width: 2px; background: #dee2e6; }
        .timeline-item { position: relative; padding-bottom: 20px; }
        .timeline-marker { position: absolute; left: -26px; width: 14px; height: 14px; border-radius: 50%; border: 2px solid #fff; }
        .timeline-content { padding-left: 10px; }
      `}</style>
        </div>
    );
};

export default MaterialDetailPage;
