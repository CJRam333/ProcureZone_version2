import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Row, Col, Badge, Button, InputGroup } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaPlus, FaEye, FaEdit, FaTrash, FaSearch, FaFilter, FaLeaf, FaIndustry } from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, Column, ExportButtons } from '../../components/common';
import { plantIndentsApi, plantsApi, getErrorMessage } from '../../api';
import type { PlantIndent } from '../../api/plantIndents';
import { INDENT_STATUS_COLORS } from '../../constants/indentStatus';

const PlantIndentListPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [plantFilter, setPlantFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [employeeSearch, setEmployeeSearch] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;
  const queryClient = useQueryClient();

  // Fetch plants for filter
  const { data: plantsData } = useQuery({
    queryKey: ['plants-list'],
    queryFn: () => plantsApi.getAll(0, 100),
  });

  // Fetch plant indents — all active filters are combined in one request
  const { data: indentsData, isLoading, error } = useQuery({
    queryKey: ['plant-indents', searchTerm, plantFilter, statusFilter, employeeSearch, fromDate, toDate, currentPage],
    queryFn: () => plantIndentsApi.list({
      page: currentPage,
      size: pageSize,
      search: searchTerm || undefined,
      plantId: plantFilter ? Number(plantFilter) : undefined,
      status: statusFilter !== '' ? Number(statusFilter) : undefined,
      empSearch: employeeSearch || undefined,
      fromDate: fromDate || undefined,
      toDate: toDate || undefined,
    }),
  });

  const columns = [
    {
      key: 'indentNumber',
      label: 'Indent No.',
      render: (row: PlantIndent) => (
        <span className="fw-bold text-primary">{row.indentNumber || row.indentCode}</span>
      ),
    },
    {
      key: 'plantName',
      label: 'Plant',
      render: (row: PlantIndent) => (
        <div>
          <FaIndustry className="me-1 text-muted" />
          {row.plantName || 'Not Assigned'}
        </div>
      ),
    },
    {
      key: 'cropTypeName',
      label: 'Crop Type',
      render: (row: PlantIndent) => (
        <div>
          {(row.outputMaterial || row.cropTypeName) && (
            <div className="small">
              <FaLeaf className="me-1 text-success" />
              {row.outputMaterial || row.cropTypeName}
            </div>
          )}
          {row.packProcess && (
            <div className="text-muted small">{row.packProcess}</div>
          )}
          {!row.outputMaterial && !row.cropTypeName && !row.packProcess && '—'}
        </div>
      ),
    },
    {
      key: 'employeeNumber',
      label: 'Employee',
      render: (row: PlantIndent) => row.employeeName || row.employeeNumber || '—',
    },
    {
      key: 'batchNumber',
      label: 'Batch No.',
      render: (row: PlantIndent) => row.batchNumber || '—',
    },
    {
      key: 'masterUom',
      label: 'UOM',
      render: (row: PlantIndent) => row.masterUom || '—',
    },
    {
      key: 'detailCount',
      label: 'Items',
      render: (row: PlantIndent) => row.detailCount || 0,
    },
    {
      key: 'status',
      label: 'Status',
      render: (row: PlantIndent) => (
        <Badge bg={INDENT_STATUS_COLORS[row.statusDescription ?? ''] || 'secondary'}>
          {row.statusDescription || 'Unknown'}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: PlantIndent) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            title="View"
            onClick={() => navigate(`/plant-indent/${row.id}`)}
          >
            <FaEye />
          </Button>
          {(row.status === 1 || row.status === 2) && (
            <Button
              variant="outline-warning"
              size="sm"
              title="Edit"
              onClick={() => navigate(`/plant-indent/${row.id}/edit`)}
            >
              <FaEdit />
            </Button>
          )}
          {row.status === 1 && (
            <Button
              variant="outline-danger"
              size="sm"
              title="Delete"
              onClick={() => {
                if (window.confirm(`Delete indent ${row.indentNumber || row.id}?`)) {
                  plantIndentsApi.delete(row.id).then(() => {
                    queryClient.invalidateQueries({ queryKey: ['plant-indents'] });
                  });
                }
              }}
            >
              <FaTrash />
            </Button>
          )}
        </div>
      ),
    },
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading plant indents: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div className="plant-indent-list">
      <PageHeader
        title="Plant Indents"
        subtitle="Manage plant-specific material requisitions (R&D / Production)"
        actions={
          <Button variant="primary" onClick={() => navigate('/plant-indent/new')}>
            <FaPlus className="me-1" /> New Plant Indent
          </Button>
        }
      />

      {/* Filters */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={4}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  placeholder="Search by indent number, crop, batch..."
                  value={searchTerm}
                  onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(0); }}
                />
              </InputGroup>
            </Col>
            <Col md={3}>
              <Form.Control
                placeholder="Employee name or ID..."
                value={employeeSearch}
                onChange={(e) => { setEmployeeSearch(e.target.value); setCurrentPage(0); }}
              />
            </Col>
            <Col md={3}>
              <Form.Select
                value={plantFilter}
                onChange={(e) => { setPlantFilter(e.target.value); setCurrentPage(0); }}
              >
                <option value="">All Plants</option>
                {plantsData?.content?.map((plant: { id: number; name: string }) => (
                  <option key={plant.id} value={plant.id}>{plant.name}</option>
                ))}
              </Form.Select>
            </Col>
            <Col md={2}>
              <Form.Select
                value={statusFilter}
                onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(0); }}
              >
                <option value="">All Status</option>
                <option value="1">Pending</option>
                <option value="2">Rejected</option>
                <option value="3">DEO Approved</option>
                <option value="4">Final Approved</option>
                <option value="5">Quotations Collected</option>
                <option value="6">Negotiation Done</option>
                <option value="7">PO Released</option>
                <option value="8">On Hold</option>
                <option value="9">Cash Buy</option>
                <option value="10">Goods Receipt</option>
                <option value="11">Goods Issued</option>
                <option value="20">Completed</option>
              </Form.Select>
            </Col>
          </Row>
          <Row className="g-3 align-items-end mt-1">
            <Col md={3}>
              <Form.Control
                type="date"
                value={fromDate}
                onChange={(e) => { setFromDate(e.target.value); setCurrentPage(0); }}
                placeholder="From date"
              />
            </Col>
            <Col md={3}>
              <Form.Control
                type="date"
                value={toDate}
                onChange={(e) => { setToDate(e.target.value); setCurrentPage(0); }}
                placeholder="To date"
              />
            </Col>
            <Col md="auto" className="d-flex gap-2">
              {(searchTerm || plantFilter || statusFilter || employeeSearch || fromDate || toDate) && (
                <Button
                  variant="outline-danger"
                  size="sm"
                  onClick={() => {
                    setSearchTerm('');
                    setPlantFilter('');
                    setStatusFilter('');
                    setEmployeeSearch('');
                    setFromDate('');
                    setToDate('');
                    setCurrentPage(0);
                  }}
                >
                  <FaFilter className="me-1" /> Clear
                </Button>
              )}
              <ExportButtons
                filenameBase="plant_indents_export"
                onExport={(fmt) =>
                  plantIndentsApi.export({
                    format: fmt,
                    search: searchTerm || undefined,
                    plantId: plantFilter ? Number(plantFilter) : undefined,
                    status: statusFilter !== '' ? Number(statusFilter) : undefined,
                    empSearch: employeeSearch || undefined,
                    fromDate: fromDate || undefined,
                    toDate: toDate || undefined,
                  })
                }
              />
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <LoadingSpinner text="Loading plant indents..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={(indentsData?.content || []) as unknown as Record<string, unknown>[]}
          keyField="id"
          totalItems={indentsData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}
    </div>
  );
};

export default PlantIndentListPage;
