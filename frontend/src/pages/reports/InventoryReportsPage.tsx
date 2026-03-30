import React, { useState } from 'react';
import { Container, Row, Col, Card, Form, Button, Table, Spinner, Alert, Badge } from 'react-bootstrap';
import { FiDownload, FiRefreshCw, FiAlertTriangle, FiPackage } from 'react-icons/fi';
import { useQuery } from '@tanstack/react-query';
import reportsApi from '../../api/reports';
import { companiesApi, plantsApi, locationsApi } from '../../api';

interface StockStatusItem {
    materialCode: string;
    materialName: string;
    materialGroup: string;
    uom: string;
    currentQuantity: number;
    reservedQuantity: number;
    availableQuantity: number;
    minStockLevel: number;
    maxStockLevel: number;
    reorderLevel: number;
    stockStatus: 'Critical' | 'Low' | 'Normal' | 'Excess';
    companyName: string;
    plantName: string;
    locationName: string;
    lastTransactionDate: string | null;
}

interface LowStockItem {
    materialCode: string;
    materialName: string;
    uom: string;
    availableQuantity: number;
    reorderLevel: number;
    minStockLevel: number;
    shortfall: number;
    companyName: string;
    plantName: string;
    locationName: string;
}

const InventoryReportsPage: React.FC = () => {
    const [reportType, setReportType] = useState<'stock-status' | 'low-stock' | 'material-usage' | 'value'>('stock-status');
    const [companyId, setCompanyId] = useState<number | undefined>();
    const [plantId, setPlantId] = useState<number | undefined>();
    const [locationId, setLocationId] = useState<number | undefined>();
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    // Stock Status Query
    const { data: stockStatus, isLoading: loadingStock, refetch: refetchStock } = useQuery({
        queryKey: ['inventory-stock-status', companyId, plantId, locationId],
        queryFn: () => reportsApi.getInventoryStockStatus({ companyId, plantId, locationId }),
        enabled: reportType === 'stock-status',
    });

    // Low Stock Alerts Query
    const { data: lowStock, isLoading: loadingLowStock, refetch: refetchLowStock } = useQuery({
        queryKey: ['inventory-low-stock', companyId],
        queryFn: () => reportsApi.getLowStockAlerts(companyId),
        enabled: reportType === 'low-stock',
    });

    // Master data queries for dropdowns
    const { data: companies } = useQuery({
        queryKey: ['companies-list'],
        queryFn: () => companiesApi.getAll(0, 100),
    });

    const { data: plants } = useQuery({
        queryKey: ['plants-list'],
        queryFn: () => plantsApi.getAll(0, 100),
    });

    const { data: locations } = useQuery({
        queryKey: ['locations-list'],
        queryFn: () => locationsApi.getAll(0, 100),
    });

    // Material Usage Query
    const { data: materialUsage, isLoading: loadingUsage, refetch: refetchUsage } = useQuery({
        queryKey: ['material-usage', startDate, endDate],
        queryFn: () => reportsApi.getMaterialUsageReport(startDate, endDate),
        enabled: reportType === 'material-usage' && !!startDate && !!endDate,
    });

    // Inventory Value Query
    const { data: inventoryValue, isLoading: loadingValue, refetch: refetchValue } = useQuery({
        queryKey: ['inventory-value', companyId],
        queryFn: () => reportsApi.getInventoryValueReport(companyId),
        enabled: reportType === 'value',
    });

    const getStockBadge = (status: string) => {
        const variants = {
            'Critical': 'danger',
            'Low': 'warning',
            'Normal': 'success',
            'Excess': 'info',
        };
        return <Badge bg={variants[status as keyof typeof variants] || 'secondary'}>{status}</Badge>;
    };

    const handleRefresh = () => {
        switch (reportType) {
            case 'stock-status':
                refetchStock();
                break;
            case 'low-stock':
                refetchLowStock();
                break;
            case 'material-usage':
                refetchUsage();
                break;
            case 'value':
                refetchValue();
                break;
        }
    };

    const isLoading = loadingStock || loadingLowStock || loadingUsage || loadingValue;

    return (
        <Container fluid className="py-4">
            <Row className="mb-4">
                <Col>
                    <div className="d-flex justify-content-between align-items-center">
                        <div>
                            <h2 className="mb-1">
                                <FiPackage className="me-2" />
                                Inventory Reports
                            </h2>
                            <p className="text-muted mb-0">View and analyze inventory data</p>
                        </div>
                        <Button variant="outline-primary" onClick={handleRefresh} disabled={isLoading}>
                            <FiRefreshCw className={isLoading ? 'spinner-border spinner-border-sm' : ''} />
                            {isLoading ? ' Loading...' : ' Refresh'}
                        </Button>
                    </div>
                </Col>
            </Row>

            {/* Filters Card */}
            <Card className="mb-4 shadow-sm">
                <Card.Body>
                    <Row className="g-3">
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label>Report Type</Form.Label>
                                <Form.Select
                                    value={reportType}
                                    onChange={(e) => setReportType(e.target.value as any)}
                                >
                                    <option value="stock-status">Stock Status</option>
                                    <option value="low-stock">Low Stock Alerts</option>
                                    <option value="material-usage">Material Usage</option>
                                    <option value="value">Inventory Value</option>
                                </Form.Select>
                            </Form.Group>
                        </Col>

                        {(reportType === 'stock-status' || reportType === 'low-stock' || reportType === 'value') && (
                            <Col md={3}>
                                <Form.Group>
                                    <Form.Label>Company</Form.Label>
                                    <Form.Select
                                        value={companyId || ''}
                                        onChange={(e) => setCompanyId(e.target.value ? Number(e.target.value) : undefined)}
                                    >
                                        <option value="">All Companies</option>
                                        {companies?.content?.map((company: any) => (
                                            <option key={company.id} value={company.id}>
                                                {company.name}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                        )}

                        {reportType === 'stock-status' && (
                            <>
                                <Col md={3}>
                                    <Form.Group>
                                        <Form.Label>Plant</Form.Label>
                                        <Form.Select
                                            value={plantId || ''}
                                            onChange={(e) => setPlantId(e.target.value ? Number(e.target.value) : undefined)}
                                        >
                                            <option value="">All Plants</option>
                                            {plants?.content?.map((plant: any) => (
                                                <option key={plant.id} value={plant.id}>
                                                    {plant.name}
                                                </option>
                                            ))}
                                        </Form.Select>
                                    </Form.Group>
                                </Col>
                                <Col md={3}>
                                    <Form.Group>
                                        <Form.Label>Location</Form.Label>
                                        <Form.Select
                                            value={locationId || ''}
                                            onChange={(e) => setLocationId(e.target.value ? Number(e.target.value) : undefined)}
                                        >
                                            <option value="">All Locations</option>
                                            {locations?.content?.map((location: any) => (
                                                <option key={location.id} value={location.id}>
                                                    {location.name}
                                                </option>
                                            ))}
                                        </Form.Select>
                                    </Form.Group>
                                </Col>
                            </>
                        )}

                        {reportType === 'material-usage' && (
                            <>
                                <Col md={3}>
                                    <Form.Group>
                                        <Form.Label>Start Date</Form.Label>
                                        <Form.Control
                                            type="date"
                                            value={startDate}
                                            onChange={(e) => setStartDate(e.target.value)}
                                        />
                                    </Form.Group>
                                </Col>
                                <Col md={3}>
                                    <Form.Group>
                                        <Form.Label>End Date</Form.Label>
                                        <Form.Control
                                            type="date"
                                            value={endDate}
                                            onChange={(e) => setEndDate(e.target.value)}
                                        />
                                    </Form.Group>
                                </Col>
                            </>
                        )}

                        <Col md={3} className="d-flex align-items-end">
                            <Button variant="success" className="w-100">
                                <FiDownload className="me-2" />
                                Export to Excel
                            </Button>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            {/* Results Card */}
            <Card className="shadow-sm">
                <Card.Body>
                    {isLoading ? (
                        <div className="text-center py-5">
                            <Spinner animation="border" variant="primary" />
                            <p className="mt-3 text-muted">Loading report data...</p>
                        </div>
                    ) : (
                        <>
                            {/* Stock Status Report */}
                            {reportType === 'stock-status' && stockStatus && (
                                <div className="table-responsive">
                                    <Table striped bordered hover>
                                        <thead className="bg-light">
                                            <tr>
                                                <th>Material Code</th>
                                                <th>Material Name</th>
                                                <th>Group</th>
                                                <th>Current Qty</th>
                                                <th>Reserved</th>
                                                <th>Available</th>
                                                <th>Min Level</th>
                                                <th>Reorder Level</th>
                                                <th>Status</th>
                                                <th>Location</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {(stockStatus as StockStatusItem[]).map((item, index) => (
                                                <tr key={index}>
                                                    <td><strong>{item.materialCode}</strong></td>
                                                    <td>{item.materialName}</td>
                                                    <td>{item.materialGroup}</td>
                                                    <td>{item.currentQuantity.toFixed(2)} {item.uom}</td>
                                                    <td>{item.reservedQuantity.toFixed(2)} {item.uom}</td>
                                                    <td>{item.availableQuantity.toFixed(2)} {item.uom}</td>
                                                    <td>{item.minStockLevel.toFixed(2)} {item.uom}</td>
                                                    <td>{item.reorderLevel.toFixed(2)} {item.uom}</td>
                                                    <td>{getStockBadge(item.stockStatus)}</td>
                                                    <td>{item.locationName}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </Table>
                                    {stockStatus.length === 0 && (
                                        <Alert variant="info">No stock data available</Alert>
                                    )}
                                </div>
                            )}

                            {/* Low Stock Alerts */}
                            {reportType === 'low-stock' && lowStock && (
                                <div className="table-responsive">
                                    <Alert variant="warning" className="mb-3">
                                        <FiAlertTriangle className="me-2" />
                                        <strong>{lowStock.length}</strong> materials are below reorder level
                                    </Alert>
                                    <Table striped bordered hover>
                                        <thead className="bg-warning bg-opacity-10">
                                            <tr>
                                                <th>Material Code</th>
                                                <th>Material Name</th>
                                                <th>Available Qty</th>
                                                <th>Reorder Level</th>
                                                <th>Shortfall</th>
                                                <th>Location</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {(lowStock as LowStockItem[]).map((item, index) => (
                                                <tr key={index} className={item.availableQuantity <= item.minStockLevel ? 'table-danger' : 'table-warning'}>
                                                    <td><strong>{item.materialCode}</strong></td>
                                                    <td>{item.materialName}</td>
                                                    <td>{item.availableQuantity.toFixed(2)} {item.uom}</td>
                                                    <td>{item.reorderLevel.toFixed(2)} {item.uom}</td>
                                                    <td><strong className="text-danger">{item.shortfall.toFixed(2)} {item.uom}</strong></td>
                                                    <td>{item.locationName}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </Table>
                                    {lowStock.length === 0 && (
                                        <Alert variant="success">All materials are above reorder level ✓</Alert>
                                    )}
                                </div>
                            )}

                            {/* Material Usage Report */}
                            {reportType === 'material-usage' && materialUsage && (
                                <div className="table-responsive">
                                    <Table striped bordered hover>
                                        <thead className="bg-light">
                                            <tr>
                                                <th>Material Code</th>
                                                <th>Material Name</th>
                                                <th>Total In</th>
                                                <th>Total Out</th>
                                                <th>Net Movement</th>
                                                <th>Transactions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {materialUsage.map((item: any, index: number) => (
                                                <tr key={index}>
                                                    <td><strong>{item.materialCode}</strong></td>
                                                    <td>{item.materialName}</td>
                                                    <td className="text-success">{item.totalIn.toFixed(2)} {item.uom}</td>
                                                    <td className="text-danger">{item.totalOut.toFixed(2)} {item.uom}</td>
                                                    <td><strong>{(item.totalIn - item.totalOut).toFixed(2)} {item.uom}</strong></td>
                                                    <td>{item.transactionCount}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </Table>
                                </div>
                            )}

                            {/* Inventory Value Report */}
                            {reportType === 'value' && inventoryValue && (
                                <div className="table-responsive">
                                    <Table striped bordered hover>
                                        <thead className="bg-light">
                                            <tr>
                                                <th>Material Group</th>
                                                <th>Material Count</th>
                                                <th>Total Quantity</th>
                                                <th>Total Value (₹)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {inventoryValue.map((item: any, index: number) => (
                                                <tr key={index}>
                                                    <td><strong>{item.materialGroup}</strong></td>
                                                    <td>{item.materialCount}</td>
                                                    <td>{item.totalQuantity.toFixed(2)}</td>
                                                    <td><strong>₹ {item.totalValue.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong></td>
                                                </tr>
                                            ))}
                                        </tbody>
                                        <tfoot className="bg-light">
                                            <tr>
                                                <td><strong>TOTAL</strong></td>
                                                <td><strong>{inventoryValue.reduce((sum: number, item: any) => sum + item.materialCount, 0)}</strong></td>
                                                <td><strong>{inventoryValue.reduce((sum: number, item: any) => sum + item.totalQuantity, 0).toFixed(2)}</strong></td>
                                                <td><strong>₹ {inventoryValue.reduce((sum: number, item: any) => sum + item.totalValue, 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong></td>
                                            </tr>
                                        </tfoot>
                                    </Table>
                                </div>
                            )}
                        </>
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

export default InventoryReportsPage;
