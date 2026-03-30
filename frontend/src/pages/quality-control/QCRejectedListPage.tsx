import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Row, Col, Badge, Button, InputGroup, Table } from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import {
  FaSearch, FaFilter, FaTimesCircle, FaEye, FaUndo, FaFileExport, FaTruck
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, Column } from '../../components/common';
import { grnApi, GRN, getErrorMessage } from '../../api';

const QCRejectedListPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [returnStatusFilter, setReturnStatusFilter] = useState<string>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Fetch rejected items
  const { data: rejectedData, isLoading, error } = useQuery({
    queryKey: ['qc-rejected', searchTerm, returnStatusFilter, dateFrom, dateTo, currentPage],
    queryFn: () => grnApi.list({
      search: searchTerm,
      status: 5, // Rejected status
      fromDate: dateFrom || undefined,
      toDate: dateTo || undefined,
      page: currentPage,
      size: pageSize,
    }),
  });

  const handleExport = () => {
    // Export to CSV
    const data = rejectedData?.content || [];
    const headers = ['GRN No', 'Vendor', 'Received Qty', 'Status', 'Date'];
    const rows = data.map((item: GRN) => [
      item.grnNumber,
      item.vendorName,
      item.receivedQuantity,
      item.statusName,
      new Date(item.receiptDate).toLocaleDateString(),
    ]);

    const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `qc-rejected-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
  };

  const columns = [
    {
      key: 'grnNumber',
      label: 'GRN No.',
      render: (row: GRN) => (
        <span className="fw-bold text-danger">{row.grnNumber}</span>
      ),
    },
    {
      key: 'vendorName',
      label: 'Vendor',
    },
    {
      key: 'receivedQuantity',
      label: 'Received Qty',
      render: (row: GRN) => (
        <span className="text-danger fw-bold">{row.receivedQuantity}</span>
      ),
    },
    {
      key: 'comments',
      label: 'Remarks',
      render: (row: GRN) => (
        <span className="text-truncate d-inline-block" style={{ maxWidth: '150px' }} title={row.comments || ''}>
          {row.comments || '-'}
        </span>
      ),
    },
    {
      key: 'receiptDate',
      label: 'Date',
      render: (row: GRN) => new Date(row.receiptDate).toLocaleDateString(),
    },
    {
      key: 'statusName',
      label: 'Status',
      render: (row: GRN) => (
        <Badge bg="danger">
          {row.statusName}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: GRN) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            onClick={() => navigate(`/grn/${row.id}`)}
            title="View Details"
          >
            <FaEye />
          </Button>
        </div>
      ),
    },
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading rejected items: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div className="qc-rejected-list">
      <PageHeader
        title="QC Rejected Items"
        subtitle="Track and manage quality-rejected materials for vendor returns"
        breadcrumbs={[
          { label: 'Quality Control', path: '/quality-control' },
          { label: 'Rejected Items', path: '' },
        ]}
        actions={
          <Button variant="outline-success" onClick={handleExport}>
            <FaFileExport className="me-1" /> Export CSV
          </Button>
        }
      />

      {/* Summary Cards */}
      <Row className="mb-3">
        <Col md={4}>
          <Card className="bg-danger bg-opacity-10 border-danger">
            <Card.Body className="py-3">
              <Row className="align-items-center">
                <Col xs={8}>
                  <h4 className="mb-1 text-danger">₹45,230</h4>
                  <div className="text-muted small">Total Rejected Value (MTD)</div>
                </Col>
                <Col xs={4} className="text-end">
                  <FaTimesCircle className="text-danger" size={32} />
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="bg-warning bg-opacity-10 border-warning">
            <Card.Body className="py-3">
              <Row className="align-items-center">
                <Col xs={8}>
                  <h4 className="mb-1 text-warning">8</h4>
                  <div className="text-muted small">Pending Returns</div>
                </Col>
                <Col xs={4} className="text-end">
                  <FaUndo className="text-warning" size={32} />
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="bg-info bg-opacity-10 border-info">
            <Card.Body className="py-3">
              <Row className="align-items-center">
                <Col xs={8}>
                  <h4 className="mb-1 text-info">15</h4>
                  <div className="text-muted small">Returned this Month</div>
                </Col>
                <Col xs={4} className="text-end">
                  <FaTruck className="text-info" size={32} />
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Filters */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={3}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  placeholder="Search by GRN, PO, vendor..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </Col>
            <Col md={2}>
              <Form.Select
                value={returnStatusFilter}
                onChange={(e) => setReturnStatusFilter(e.target.value)}
              >
                <option value="">All Return Status</option>
                <option value="PENDING_RETURN">Pending Return</option>
                <option value="RETURNED">Returned</option>
                <option value="CREDITED">Credit Received</option>
              </Form.Select>
            </Col>
            <Col md={2}>
              <Form.Control
                type="date"
                placeholder="From Date"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
              />
            </Col>
            <Col md={2}>
              <Form.Control
                type="date"
                placeholder="To Date"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
              />
            </Col>
            <Col md={2}>
              <Button
                variant="outline-secondary"
                className="w-100"
                onClick={() => {
                  setSearchTerm('');
                  setReturnStatusFilter('');
                  setDateFrom('');
                  setDateTo('');
                }}
              >
                <FaFilter className="me-1" /> Clear
              </Button>
            </Col>
            <Col md={1}>
              <Button
                variant="outline-primary"
                className="w-100"
                onClick={() => navigate('/quality-control')}
              >
                Back
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <LoadingSpinner text="Loading rejected items..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={(rejectedData?.content || []) as unknown as Record<string, unknown>[]}
          keyField="id"
          totalItems={rejectedData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}
    </div>
  );
};

export default QCRejectedListPage;
