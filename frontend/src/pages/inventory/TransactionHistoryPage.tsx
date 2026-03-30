import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
    Card,
    Row,
    Col,
    Form,
    Button,
    Badge,
    InputGroup,
    Alert,
} from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
    FaArrowLeft,
    FaSearch,
    FaSyncAlt,
    FaArrowUp,
    FaArrowDown,
    FaExchangeAlt,
    FaUndo,
    FaEdit,
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner } from '../../components/common';
import { inventoryApi, getErrorMessage } from '../../api';
import type { InventoryTransaction } from '../../api/inventory';

interface TransactionFilters {
    search: string;
    transactionType: string;
    fromDate: string;
    toDate: string;
}

const TransactionHistoryPage: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [page, setPage] = useState(0);
    const [pageSize] = useState(20);
    const [filters, setFilters] = useState<TransactionFilters>({
        search: '',
        transactionType: '',
        fromDate: '',
        toDate: '',
    });

    // Parse the ID - it might be in the format "plantId-materialId" or just "id"
    const parseId = () => {
        if (!id) return { plantId: 0, materialId: 0 };
        if (id.includes('-')) {
            const parts = id.split('-');
            return { plantId: Number(parts[0]), materialId: Number(parts[1]) };
        }
        return { plantId: 1, materialId: Number(id) }; // Default plantId
    };

    const { plantId, materialId } = parseId();

    // Fetch inventory item
    const { data: inventoryItem, isLoading: loadingItem } = useQuery({
        queryKey: ['inventory-item', plantId, materialId],
        queryFn: () => inventoryApi.getByPlantAndMaterial(plantId, materialId),
        enabled: plantId > 0 && materialId > 0,
    });

    // Fetch transactions
    const { data: transactionsData, isLoading, error, refetch } = useQuery({
        queryKey: ['inventory-transactions', plantId, materialId, page, pageSize, filters],
        queryFn: () =>
            inventoryApi.getTransactions({
                plantId,
                materialId,
                page,
                size: pageSize,
            }),
        enabled: plantId > 0 && materialId > 0,
    });

    // Transaction type options
    const transactionTypeOptions = [
        { value: '', label: 'All Types' },
        { value: 'RECEIPT', label: 'Receipt' },
        { value: 'ISSUE', label: 'Issue' },
        { value: 'RETURN', label: 'Return' },
        { value: 'ADJUSTMENT', label: 'Adjustment' },
        { value: 'TRANSFER', label: 'Transfer' },
    ];

    // Get transaction type icon and color
    const getTransactionIcon = (type: string) => {
        switch (type) {
            case 'RECEIPT':
                return { icon: <FaArrowDown className="me-1" />, variant: 'success', label: 'Receipt' };
            case 'ISSUE':
                return { icon: <FaArrowUp className="me-1" />, variant: 'warning', label: 'Issue' };
            case 'RETURN':
                return { icon: <FaUndo className="me-1" />, variant: 'info', label: 'Return' };
            case 'ADJUSTMENT':
                return { icon: <FaEdit className="me-1" />, variant: 'secondary', label: 'Adjustment' };
            case 'TRANSFER':
                return { icon: <FaExchangeAlt className="me-1" />, variant: 'primary', label: 'Transfer' };
            default:
                return { icon: null, variant: 'secondary', label: type };
        }
    };

    // Safe date formatter
    const formatDateTime = (dateStr: string | null | undefined): string => {
        if (!dateStr) return 'N/A';
        try {
            const date = new Date(dateStr);
            if (isNaN(date.getTime())) return 'N/A';
            return format(date, 'dd MMM yyyy HH:mm');
        } catch {
            return 'N/A';
        }
    };

    // Table columns
    const columns = [
        {
            key: 'createdAt',
            label: 'Date/Time',
            render: (row: Record<string, any>) => (
                <div>
                    <strong>{formatDateTime(row.createdAt)}</strong>
                </div>
            ),
        },
        {
            key: 'transactionType',
            label: 'Type',
            render: (row: Record<string, any>) => {
                const typeInfo = getTransactionIcon(row.transactionType);
                return (
                    <Badge bg={typeInfo.variant as any}>
                        {typeInfo.icon}
                        {typeInfo.label}
                    </Badge>
                );
            },
        },
        {
            key: 'quantity',
            label: 'Quantity',
            render: (row: Record<string, any>) => (
                <span className={`fw-bold ${row.quantity >= 0 ? 'text-success' : 'text-danger'}`}>
                    {row.quantity >= 0 ? '+' : ''}{row.quantity}
                </span>
            ),
        },
        {
            key: 'stockChange',
            label: 'Stock Change',
            render: (row: Record<string, any>) => (
                <div className="small">
                    <span className="text-muted">{row.previousQuantity}</span>
                    <span className="mx-2">→</span>
                    <span className="fw-bold">{row.newQuantity}</span>
                </div>
            ),
        },
        {
            key: 'reference',
            label: 'Reference',
            render: (row: Record<string, any>) => (
                <div>
                    {row.referenceNumber ? (
                        <code className="text-primary">{row.referenceNumber}</code>
                    ) : (
                        <span className="text-muted">-</span>
                    )}
                    {row.referenceType && (
                        <div className="small text-muted">{row.referenceType}</div>
                    )}
                </div>
            ),
        },
        {
            key: 'createdByName',
            label: 'By',
            render: (row: Record<string, any>) => (
                <span>{row.createdByName || 'System'}</span>
            ),
        },
        {
            key: 'remarks',
            label: 'Remarks',
            render: (row: Record<string, any>) => (
                <span className="text-truncate" style={{ maxWidth: '200px', display: 'block' }}>
                    {row.remarks || '-'}
                </span>
            ),
        },
    ];

    // Reset filters
    const resetFilters = () => {
        setFilters({ search: '', transactionType: '', fromDate: '', toDate: '' });
        setPage(0);
    };

    // Handle search
    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setPage(0);
    };

    if (loadingItem && plantId > 0 && materialId > 0) {
        return <LoadingSpinner fullPage text="Loading inventory item..." />;
    }

    return (
        <div>
            <PageHeader
                title="Transaction History"
                subtitle={
                    inventoryItem
                        ? `${inventoryItem.materialCode} - ${inventoryItem.materialDescription}`
                        : 'View stock movement history'
                }
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Inventory', path: '/inventory' },
                    { label: 'Transaction History' },
                ]}
                actions={
                    <Button variant="outline-secondary" onClick={() => navigate('/inventory')}>
                        <FaArrowLeft className="me-2" /> Back to Inventory
                    </Button>
                }
            />

            {/* Material Info (if available) */}
            {inventoryItem && (
                <Card className="mb-4">
                    <Card.Body>
                        <Row className="align-items-center">
                            <Col md={3}>
                                <small className="text-muted d-block">Material</small>
                                <strong className="text-primary">{inventoryItem.materialCode}</strong>
                                <div className="small text-muted">{inventoryItem.materialDescription}</div>
                            </Col>
                            <Col md={2}>
                                <small className="text-muted d-block">Current Stock</small>
                                <span className="fs-4 fw-bold text-success">
                                    {inventoryItem.quantity}
                                </span>
                                <span className="ms-1">{inventoryItem.uomCode}</span>
                            </Col>
                            <Col md={2}>
                                <small className="text-muted d-block">Reserved</small>
                                <span>{inventoryItem.reservedQuantity || 0}</span>
                            </Col>
                            <Col md={2}>
                                <small className="text-muted d-block">Available</small>
                                <span className="text-primary fw-bold">
                                    {inventoryItem.availableQuantity || inventoryItem.quantity}
                                </span>
                            </Col>
                            <Col md={3}>
                                <small className="text-muted d-block">Stock Levels</small>
                                <span className="text-warning">Min: {inventoryItem.minStockLevel || 0}</span>
                                <span className="mx-2">|</span>
                                <span className="text-info">Max: {inventoryItem.maxStockLevel || '-'}</span>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>
            )}

            {/* Filters */}
            <Card className="mb-4">
                <Card.Body>
                    <Form onSubmit={handleSearch}>
                        <Row className="g-3 align-items-end">
                            <Col lg={3} md={6}>
                                <Form.Select
                                    value={filters.transactionType}
                                    onChange={(e) => {
                                        setFilters({ ...filters, transactionType: e.target.value });
                                        setPage(0);
                                    }}
                                >
                                    {transactionTypeOptions.map((opt) => (
                                        <option key={opt.value} value={opt.value}>
                                            {opt.label}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>
                            <Col lg={2} md={3}>
                                <Form.Group>
                                    <Form.Label className="small mb-1">From Date</Form.Label>
                                    <Form.Control
                                        type="date"
                                        value={filters.fromDate}
                                        onChange={(e) => setFilters({ ...filters, fromDate: e.target.value })}
                                    />
                                </Form.Group>
                            </Col>
                            <Col lg={2} md={3}>
                                <Form.Group>
                                    <Form.Label className="small mb-1">To Date</Form.Label>
                                    <Form.Control
                                        type="date"
                                        value={filters.toDate}
                                        onChange={(e) => setFilters({ ...filters, toDate: e.target.value })}
                                    />
                                </Form.Group>
                            </Col>
                            <Col lg="auto">
                                <div className="d-flex gap-2">
                                    <Button variant="outline-secondary" onClick={() => refetch()}>
                                        <FaSyncAlt />
                                    </Button>
                                    {(filters.transactionType || filters.fromDate || filters.toDate) && (
                                        <Button variant="outline-danger" onClick={resetFilters}>
                                            Clear
                                        </Button>
                                    )}
                                </div>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            {/* Transaction Summary */}
            <Row className="g-3 mb-4">
                <Col sm={6} lg={3}>
                    <Card className="border-start border-4 border-success h-100">
                        <Card.Body className="py-3">
                            <div className="d-flex align-items-center">
                                <FaArrowDown className="text-success me-3" size={24} />
                                <div>
                                    <div className="text-muted small">Receipts</div>
                                    <div className="h5 mb-0">
                                        {transactionsData?.content?.filter((t: InventoryTransaction) => t.transactionType === 'RECEIPT').length || 0}
                                    </div>
                                </div>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
                <Col sm={6} lg={3}>
                    <Card className="border-start border-4 border-warning h-100">
                        <Card.Body className="py-3">
                            <div className="d-flex align-items-center">
                                <FaArrowUp className="text-warning me-3" size={24} />
                                <div>
                                    <div className="text-muted small">Issues</div>
                                    <div className="h5 mb-0">
                                        {transactionsData?.content?.filter((t: InventoryTransaction) => t.transactionType === 'ISSUE').length || 0}
                                    </div>
                                </div>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
                <Col sm={6} lg={3}>
                    <Card className="border-start border-4 border-info h-100">
                        <Card.Body className="py-3">
                            <div className="d-flex align-items-center">
                                <FaUndo className="text-info me-3" size={24} />
                                <div>
                                    <div className="text-muted small">Returns</div>
                                    <div className="h5 mb-0">
                                        {transactionsData?.content?.filter((t: InventoryTransaction) => t.transactionType === 'RETURN').length || 0}
                                    </div>
                                </div>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
                <Col sm={6} lg={3}>
                    <Card className="border-start border-4 border-secondary h-100">
                        <Card.Body className="py-3">
                            <div className="d-flex align-items-center">
                                <FaEdit className="text-secondary me-3" size={24} />
                                <div>
                                    <div className="text-muted small">Adjustments</div>
                                    <div className="h5 mb-0">
                                        {transactionsData?.content?.filter((t: InventoryTransaction) => t.transactionType === 'ADJUSTMENT').length || 0}
                                    </div>
                                </div>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            {/* Transactions Table */}
            <Card>
                <Card.Body className="p-0">
                    {error ? (
                        <Alert variant="danger" className="m-4">
                            {getErrorMessage(error)}
                        </Alert>
                    ) : (
                        <DataTable
                            columns={columns}
                            data={transactionsData?.content || []}
                            keyField="id"
                            loading={isLoading}
                            totalItems={transactionsData?.totalElements || 0}
                            currentPage={page}
                            pageSize={pageSize}
                            onPageChange={setPage}
                            emptyMessage="No transactions found"
                        />
                    )}
                </Card.Body>
            </Card>
        </div>
    );
};

export default TransactionHistoryPage;
