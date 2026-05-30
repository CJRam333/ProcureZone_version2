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
    FaBuilding,
    FaPhone,
    FaEnvelope,
    FaMapMarkerAlt,
    FaFileAlt,
    FaHistory,
    FaShoppingCart,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { vendorsApi, purchaseOrdersApi, getErrorMessage } from '../../api';
import type { Vendor } from '../../api/vendors';
import { useAuth } from '../../contexts/AuthContext';

const VendorDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { hasAnyRole } = useAuth();

    // Fetch vendor details
    const { data: vendor, isLoading, error: fetchError } = useQuery({
        queryKey: ['vendor', id],
        queryFn: () => vendorsApi.getById(Number(id)),
        enabled: !!id,
    });

    // Fetch vendor's purchase orders
    const { data: poData } = useQuery({
        queryKey: ['vendor-pos', id],
        queryFn: () => purchaseOrdersApi.list({ vendorId: Number(id), page: 0, size: 10 }),
        enabled: !!id,
    });

    const canEdit = hasAnyRole(['ADMIN', 'SUPERADMIN', 'PROCUREMENT']);

    if (isLoading) {
        return <LoadingSpinner fullPage text="Loading vendor details..." />;
    }

    if (fetchError || !vendor) {
        return (
            <div className="text-center py-5">
                <Alert variant="danger">
                    {fetchError ? getErrorMessage(fetchError) : 'Vendor not found'}
                </Alert>
                <Button variant="primary" onClick={() => navigate('/vendors')}>
                    Back to Vendors
                </Button>
            </div>
        );
    }

    // Get first category name
    const categoryNames = vendor.categories?.map(c => c.categoryName).join(', ') || 'General';

    return (
        <div>
            <PageHeader
                title={vendor.vendorName}
                subtitle={`Vendor Code: ${vendor.vendorCode}`}
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Vendors', path: '/vendors' },
                    { label: vendor.vendorCode },
                ]}
                actions={
                    <div className="d-flex flex-wrap gap-2">
                        <Button variant="outline-secondary" onClick={() => navigate('/vendors')}>
                            <FaArrowLeft className="me-2" /> Back
                        </Button>
                        <Button variant="outline-secondary" onClick={() => window.print()}>
                            <FaPrint className="me-2" /> Print
                        </Button>
                        {canEdit && (
                            <Button variant="primary" onClick={() => navigate(`/vendors/${id}/edit`)}>
                                <FaEdit className="me-2" /> Edit
                            </Button>
                        )}
                    </div>
                }
            />

            {/* Status Banner */}
            <Card className="mb-4">
                <Card.Body className="d-flex justify-content-between align-items-center">
                    <div className="d-flex align-items-center gap-4">
                        <div>
                            <small className="text-muted d-block">Status</small>
                            <Badge bg={vendor.isActive ? 'success' : 'secondary'} className="fs-6">
                                {vendor.isActive ? 'ACTIVE' : 'INACTIVE'}
                            </Badge>
                            {vendor.isBlacklisted && (
                                <Badge bg="danger" className="ms-2 fs-6">BLACKLISTED</Badge>
                            )}
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">Categories</small>
                            <strong>{categoryNames}</strong>
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">Rating</small>
                            <strong>{vendor.rating || 'N/A'}</strong>
                        </div>
                    </div>
                </Card.Body>
            </Card>

            <Tabs defaultActiveKey="details" className="mb-4">
                {/* Details Tab */}
                <Tab eventKey="details" title={<><FaFileAlt className="me-2" />Details</>}>
                    <Row className="g-4">
                        {/* Basic Info */}
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaBuilding className="me-2" />
                                        Vendor Information
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Vendor Code</small>
                                                <strong>{vendor.vendorCode}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Vendor Name</small>
                                                <strong>{vendor.vendorName}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">GST Number</small>
                                                <strong>{vendor.gstNumber || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">PAN Number</small>
                                                <strong>{vendor.panNumber || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Credit Days</small>
                                                <strong>{vendor.creditDays || 0} days</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Credit Limit</small>
                                                <strong>₹{new Intl.NumberFormat('en-IN').format(vendor.creditLimit || 0)}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div>
                                                <small className="text-muted d-block">Payment Terms</small>
                                                <strong>{vendor.paymentTerms || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>

                        {/* Contact Info */}
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaPhone className="me-2" />
                                        Contact Information
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">
                                                    <FaPhone className="me-1" /> Phone
                                                </small>
                                                <strong>{vendor.phone || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">
                                                    <FaEnvelope className="me-1" /> Email
                                                </small>
                                                <strong>{vendor.email || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Contact Person</small>
                                                <strong>{vendor.contactPerson || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Mobile</small>
                                                <strong>{vendor.mobile || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>

                        {/* Address */}
                        <Col xs={12}>
                            <Card>
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaMapMarkerAlt className="me-2" />
                                        Address
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col md={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Address</small>
                                                <strong>{vendor.address || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col md={3}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">City</small>
                                                <strong>{vendor.city || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col md={3}>
                                            <div>
                                                <small className="text-muted d-block">State</small>
                                                <strong>{vendor.state || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col md={3}>
                                            <div>
                                                <small className="text-muted d-block">PIN Code</small>
                                                <strong>{vendor.pincode || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col md={3}>
                                            <div>
                                                <small className="text-muted d-block">Country</small>
                                                <strong>{vendor.country || 'India'}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>

                        {/* Bank Details */}
                        <Col xs={12}>
                            <Card>
                                <Card.Header>
                                    <h5 className="mb-0">Bank Details</h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col md={3}>
                                            <div>
                                                <small className="text-muted d-block">Bank Name</small>
                                                <strong>{vendor.bankName || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col md={3}>
                                            <div>
                                                <small className="text-muted d-block">Branch</small>
                                                <strong>{vendor.bankBranch || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col md={3}>
                                            <div>
                                                <small className="text-muted d-block">Account Number</small>
                                                <strong>{vendor.accountNumber || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col md={3}>
                                            <div>
                                                <small className="text-muted d-block">IFSC Code</small>
                                                <strong>{vendor.ifscCode || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </Tab>

                {/* Purchase Orders Tab */}
                <Tab eventKey="orders" title={<><FaShoppingCart className="me-2" />Purchase Orders</>}>
                    <Card>
                        <Card.Body className="p-0">
                            <div className="table-responsive">
                                <Table hover className="mb-0">
                                    <thead className="bg-light">
                                        <tr>
                                            <th>PO Number</th>
                                            <th>Date</th>
                                            <th>Status</th>
                                            <th className="text-end">Value (₹)</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {poData?.content?.length ? (
                                            poData.content.map((po) => (
                                                <tr key={po.id}>
                                                    <td>
                                                        <Button
                                                            variant="link"
                                                            className="p-0"
                                                            onClick={() => navigate(`/purchase-orders/${po.id}`)}
                                                        >
                                                            {po.poNumber}
                                                        </Button>
                                                    </td>
                                                    <td>{po.poDate ? format(new Date(po.poDate), 'dd MMM yyyy') : 'N/A'}</td>
                                                    <td>
                                                        <Badge bg={po.poStatus >= 2 ? 'success' : 'warning'}>
                                                            {po.poStatusName || 'PENDING'}
                                                        </Badge>
                                                    </td>
                                                    <td className="text-end">
                                                        {new Intl.NumberFormat('en-IN').format(po.netAmount || 0)}
                                                    </td>
                                                    <td>
                                                        <Button
                                                            size="sm"
                                                            variant="outline-primary"
                                                            onClick={() => navigate(`/purchase-orders/${po.id}`)}
                                                        >
                                                            View
                                                        </Button>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan={5} className="text-center text-muted py-4">
                                                    No purchase orders found for this vendor
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
                                        <strong>Vendor Created</strong>
                                        <p className="text-muted mb-0">
                                            Added on {vendor.createdAt ? format(new Date(vendor.createdAt), 'PPpp') : 'N/A'}
                                        </p>
                                    </div>
                                </div>
                                {vendor.updatedAt && vendor.updatedAt !== vendor.createdAt && (
                                    <div className="timeline-item">
                                        <div className="timeline-marker bg-info"></div>
                                        <div className="timeline-content">
                                            <strong>Last Updated</strong>
                                            <p className="text-muted mb-0">
                                                Modified on {format(new Date(vendor.updatedAt), 'PPpp')}
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

export default VendorDetailPage;
