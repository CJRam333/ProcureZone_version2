import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
    Card,
    Row,
    Col,
    Button,
    Badge,
    Alert,
} from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
    FaArrowLeft,
    FaEdit,
    FaPrint,
    FaBoxOpen,
    FaWarehouse,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { materialsApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';

const MaterialDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { hasAnyRole } = useAuth();

    const { data: material, isLoading, error: fetchError } = useQuery({
        queryKey: ['material', id],
        queryFn: () => materialsApi.getById(Number(id)),
        enabled: !!id,
    });

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
                <Button variant="primary" onClick={() => navigate('/masters/materials')}>
                    Back to Materials
                </Button>
            </div>
        );
    }

    const formatDate = (d: string | undefined) => {
        if (!d) return 'N/A';
        try { return format(new Date(d), 'dd MMM yyyy'); } catch { return 'N/A'; }
    };

    return (
        <div>
            <PageHeader
                title={material.materialCode || 'Material'}
                subtitle={material.materialName || material.description || ''}
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Materials', path: '/masters/materials' },
                    { label: material.materialCode || 'Detail' },
                ]}
                actions={
                    <div className="d-flex flex-wrap gap-2">
                        <Button variant="outline-secondary" onClick={() => navigate('/masters/materials')}>
                            <FaArrowLeft className="me-2" /> Back
                        </Button>
                        <Button variant="outline-secondary" onClick={() => window.print()}>
                            <FaPrint className="me-2" /> Print
                        </Button>
                        {canEdit && (
                            <Button variant="primary" onClick={() => navigate(`/masters/materials/${id}/edit`)}>
                                <FaEdit className="me-2" /> Edit
                            </Button>
                        )}
                    </div>
                }
            />

            {/* Status Banner */}
            <Card className="mb-4">
                <Card.Body className="d-flex flex-wrap align-items-center gap-4">
                    <div>
                        <small className="text-muted d-block">Status</small>
                        <Badge bg={material.isActive ? 'success' : 'secondary'} className="fs-6">
                            {material.isActive ? 'ACTIVE' : 'INACTIVE'}
                        </Badge>
                    </div>
                    <div className="vr d-none d-sm-block" />
                    <div>
                        <small className="text-muted d-block">
                            <FaWarehouse className="me-1" />Total Stock
                        </small>
                        <strong className="text-primary fs-5">{material.stockQuantity ?? 0}</strong>
                    </div>
                    <div className="vr d-none d-sm-block" />
                    <div>
                        <small className="text-muted d-block">Last Modified</small>
                        <strong>{formatDate(material.lastModifiedDate)}</strong>
                    </div>
                </Card.Body>
            </Card>

            {/* Details */}
            <Row className="g-4">
                <Col lg={6}>
                    <Card className="h-100">
                        <Card.Header>
                            <h5 className="mb-0">
                                <FaBoxOpen className="me-2" />Material Information
                            </h5>
                        </Card.Header>
                        <Card.Body>
                            <Row className="g-3">
                                <Col sm={6}>
                                    <small className="text-muted d-block">Material Code</small>
                                    <strong className="text-primary fs-5">{material.materialCode}</strong>
                                </Col>
                                <Col sm={6}>
                                    <small className="text-muted d-block">Status</small>
                                    <Badge bg={material.isActive ? 'success' : 'secondary'}>
                                        {material.isActive ? 'ACTIVE' : 'INACTIVE'}
                                    </Badge>
                                </Col>
                                <Col sm={12}>
                                    <small className="text-muted d-block">Name</small>
                                    <strong>{material.materialName || '-'}</strong>
                                </Col>
                                <Col sm={12}>
                                    <small className="text-muted d-block">Description</small>
                                    <span>{material.description || '-'}</span>
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col lg={6}>
                    <Card className="h-100">
                        <Card.Header>
                            <h5 className="mb-0">
                                <FaWarehouse className="me-2" />Stock
                            </h5>
                        </Card.Header>
                        <Card.Body>
                            <div className="p-3 bg-light rounded">
                                <div className="d-flex justify-content-between align-items-center">
                                    <span className="text-muted">Total Stock Across All Plants:</span>
                                    <strong className="text-primary fs-4">{material.stockQuantity ?? 0}</strong>
                                </div>
                            </div>
                            <div className="mt-3">
                                <small className="text-muted d-block">Last Modified</small>
                                <strong>{formatDate(material.lastModifiedDate)}</strong>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default MaterialDetailPage;
