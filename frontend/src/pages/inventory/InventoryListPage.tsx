import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  Row,
  Col,
  Form,
  Button,
  Badge,
  InputGroup,
  ProgressBar,
  Alert,
} from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import {
  FaSearch,
  FaSyncAlt,
  FaFilter,
  FaBoxes,
  FaExclamationTriangle,
  FaArrowDown,
  FaArrowUp,
  FaHistory,
  FaChartBar,
} from 'react-icons/fa';
import { PageHeader, DataTable, StatusBadge } from '../../components/common';
import { inventoryApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';

interface InventoryFilters {
  search: string;
  stockStatus: string;
  locationId: string;
}

const InventoryListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<InventoryFilters>({
    search: '',
    stockStatus: '',
    locationId: '',
  });

  // Fetch inventory
  const { data, isLoading, refetch, error } = useQuery({
    queryKey: ['inventory', page, pageSize, filters],
    queryFn: () =>
      inventoryApi.getStock({
        page,
        size: pageSize,
        search: filters.search || undefined,
        stockStatus: filters.stockStatus || undefined,
        locationId: filters.locationId ? Number(filters.locationId) : undefined,
      }),
  });

  // Fetch stock summary
  const { data: summary } = useQuery({
    queryKey: ['inventory-summary'],
    queryFn: async () => inventoryApi.getSummary() as any,
  });

  // Stock status options
  const stockStatusOptions = [
    { value: '', label: 'All Stock Levels' },
    { value: 'OUT_OF_STOCK', label: 'Out of Stock' },
    { value: 'LOW_STOCK', label: 'Low Stock' },
    { value: 'NORMAL', label: 'Normal' },
    { value: 'OVERSTOCK', label: 'Overstock' },
  ];

  // Get stock status
  const getStockStatus = (current: number, min: number, max: number) => {
    if (current <= 0) return { label: 'Out of Stock', variant: 'danger' };
    if (current <= min) return { label: 'Low Stock', variant: 'warning' };
    if (max > 0 && current >= max) return { label: 'Overstock', variant: 'info' };
    return { label: 'Normal', variant: 'success' };
  };

  // Calculate stock level percentage
  const getStockPercentage = (current: number, min: number, max: number) => {
    if (max <= 0) return 50; // Default if no max defined
    return Math.min(100, (current / max) * 100);
  };

  // Table columns - updated to match backend InventoryResponse
  const columns = [
    {
      key: 'materialCode',
      label: 'Material',
      render: (row: any) => (
        <div>
          <code className="fw-bold text-primary">{row.materialCode}</code>
          <div className="small text-muted">{row.materialName}</div>
        </div>
      ),
    },
    {
      key: 'plant',
      label: 'Plant / Company',
      render: (row: any) => (
        <div>
          <div className="fw-medium">{row.plantName}</div>
          <small className="text-muted">{row.companyName}</small>
        </div>
      ),
    },
    {
      key: 'uom',
      label: 'UOM',
      render: (row: any) => (
        <Badge bg="secondary">{row.uomCode}</Badge>
      ),
    },
    {
      key: 'currentStock',
      label: 'Current Stock',
      render: (row: any) => {
        const currentStock = row.currentBalance || row.availableQuantity || 0;
        const minLevel = row.minLevel || 0;
        const maxLevel = row.maxLevel || 100;
        return (
          <div className="text-center">
            <div className="fs-5 fw-bold">{currentStock}</div>
            <div style={{ width: '100px' }}>
              <ProgressBar
                now={getStockPercentage(currentStock, minLevel, maxLevel)}
                variant={getStockStatus(currentStock, minLevel, maxLevel).variant as any}
                style={{ height: '6px' }}
              />
            </div>
          </div>
        );
      },
    },
    {
      key: 'levels',
      label: 'Min / Max',
      render: (row: any) => (
        <div className="text-center">
          <span className="text-warning">{row.minLevel || 0}</span>
          <span className="mx-1">/</span>
          <span className="text-info">{row.maxLevel || '-'}</span>
        </div>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      render: (row: any) => {
        const currentStock = row.currentBalance || row.availableQuantity || 0;
        // Use backend flags if available
        if (row.isCriticalStock) {
          return <StatusBadge status="Critical" variant="danger" />;
        }
        if (row.isLowStock) {
          return <StatusBadge status="Low Stock" variant="warning" />;
        }
        if (row.isOverstock) {
          return <StatusBadge status="Overstock" variant="info" />;
        }
        const status = getStockStatus(currentStock, row.minLevel || 0, row.maxLevel || 100);
        return (
          <StatusBadge
            status={status.label}
            variant={status.variant as any}
          />
        );
      },
    },
    {
      key: 'value',
      label: 'Stock Value',
      render: (row: any) => (
        <strong>
          {new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(
            row.totalValue || (row.currentBalance || 0) * (row.avgRate || 0)
          )}
        </strong>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: any) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            onClick={() => navigate(`/inventory/${row.materialId}/plant/${row.plantId}/history`)}
            title="View History"
          >
            <FaHistory />
          </Button>
          {hasAnyRole(['ADMIN', 'STOREKEEPER', 'SUPERADMIN']) && (
            <Button
              variant="outline-secondary"
              size="sm"
              onClick={() => navigate(`/inventory/adjust?materialId=${row.materialId}&plantId=${row.plantId}`)}
              title="Adjust Stock"
            >
              <FaChartBar />
            </Button>
          )}
        </div>
      ),
    },
  ];

  // Reset filters
  const resetFilters = () => {
    setFilters({ search: '', stockStatus: '', locationId: '' });
    setPage(0);
  };

  // Handle search
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
  };

  return (
    <div>
      <PageHeader
        title="Inventory Management"
        subtitle="Monitor stock levels and inventory movements"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Inventory' },
        ]}
        actions={
          <div className="d-flex gap-2">
            <Button variant="outline-secondary" onClick={() => navigate('/inventory/movements')}>
              <FaHistory className="me-2" /> Movement History
            </Button>
            <Button variant="outline-secondary" onClick={() => navigate('/inventory/reports')}>
              <FaChartBar className="me-2" /> Reports
            </Button>
          </div>
        }
      />

      {/* Stock Alerts */}
      {summary?.lowStockCount > 0 && (
        <Alert variant="warning" className="d-flex align-items-center mb-4">
          <FaExclamationTriangle className="me-3" size={24} />
          <div>
            <strong>{summary.lowStockCount} items</strong> are below minimum stock level.
            <Button
              variant="link"
              className="p-0 ms-2"
              onClick={() => setFilters({ ...filters, stockStatus: 'LOW_STOCK' })}
            >
              View all →
            </Button>
          </div>
        </Alert>
      )}

      {/* Stats Cards */}
      <Row className="g-3 mb-4">
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-primary h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Total Items</div>
                  <div className="h3 mb-0">{summary?.totalItems || 0}</div>
                </div>
                <FaBoxes size={28} className="text-primary opacity-50" />
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-success h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Total Value</div>
                  <div className="h4 mb-0 text-success">
                    {new Intl.NumberFormat('en-IN', {
                      style: 'currency',
                      currency: 'INR',
                      notation: 'compact',
                    }).format(summary?.totalValue || 0)}
                  </div>
                </div>
                <FaArrowUp size={28} className="text-success opacity-50" />
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-warning h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Low Stock</div>
                  <div className="h3 mb-0 text-warning">{summary?.lowStockCount || 0}</div>
                </div>
                <FaExclamationTriangle size={28} className="text-warning opacity-50" />
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-danger h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Out of Stock</div>
                  <div className="h3 mb-0 text-danger">{summary?.outOfStockCount || 0}</div>
                </div>
                <FaArrowDown size={28} className="text-danger opacity-50" />
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Search & Filters */}
      <Card className="mb-4">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="g-3 align-items-end">
              <Col lg={4} md={6}>
                <InputGroup>
                  <Form.Control
                    placeholder="Search material code, description..."
                    value={filters.search}
                    onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                  />
                  <Button type="submit" variant="primary">
                    <FaSearch />
                  </Button>
                </InputGroup>
              </Col>
              <Col lg={3} md={4}>
                <Form.Select
                  value={filters.stockStatus}
                  onChange={(e) => {
                    setFilters({ ...filters, stockStatus: e.target.value });
                    setPage(0);
                  }}
                >
                  {stockStatusOptions.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </Form.Select>
              </Col>
              <Col lg="auto">
                <div className="d-flex gap-2">
                  <Button
                    variant="outline-secondary"
                    onClick={() => setShowFilters(!showFilters)}
                  >
                    <FaFilter className="me-2" /> Filters
                  </Button>
                  <Button variant="outline-secondary" onClick={() => refetch()}>
                    <FaSyncAlt />
                  </Button>
                  {(filters.search || filters.stockStatus || filters.locationId) && (
                    <Button variant="outline-danger" onClick={resetFilters}>
                      Clear
                    </Button>
                  )}
                </div>
              </Col>
            </Row>

            {/* Extended Filters */}
            {showFilters && (
              <Row className="g-3 mt-3 pt-3 border-top">
                <Col md={4}>
                  <Form.Group>
                    <Form.Label className="small">Warehouse / Location</Form.Label>
                    <Form.Select
                      value={filters.locationId}
                      onChange={(e) => setFilters({ ...filters, locationId: e.target.value })}
                    >
                      <option value="">All Locations</option>
                      {/* Locations would be loaded dynamically */}
                    </Form.Select>
                  </Form.Group>
                </Col>
              </Row>
            )}
          </Form>
        </Card.Body>
      </Card>

      {/* Data Table */}
      <Card>
        <Card.Body className="p-0">
          {error ? (
            <div className="p-4 text-center text-danger">{getErrorMessage(error)}</div>
          ) : (
            <DataTable
              columns={columns}
              data={data?.content || []}
              keyField="id"
              loading={isLoading}
              totalItems={data?.totalElements || 0}
              currentPage={page}
              pageSize={pageSize}
              onPageChange={setPage}
              emptyMessage="No inventory items found"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default InventoryListPage;
