import React, { useState } from 'react';
import { Card, Form, Row, Col, Badge, Button, InputGroup, Table } from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import {
  FaSearch, FaFilter, FaHistory, FaUser, FaCalendar, FaDatabase,
  FaFileExport, FaEye, FaEdit, FaTrash, FaPlus
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, Column } from '../../components/common';
import { getErrorMessage } from '../../api';
import apiClient from '../../api/client';

interface AuditLog {
  id: number;
  action: 'CREATE' | 'UPDATE' | 'DELETE' | 'LOGIN' | 'LOGOUT' | 'APPROVE' | 'REJECT';
  entityType: string;
  entityId: number;
  entityName?: string;
  performedBy: string;
  performedAt: string;
  ipAddress?: string;
  oldValue?: string;
  newValue?: string;
  remarks?: string;
}

const actionColors: Record<string, string> = {
  CREATE: 'success',
  UPDATE: 'info',
  DELETE: 'danger',
  LOGIN: 'primary',
  LOGOUT: 'secondary',
  APPROVE: 'success',
  REJECT: 'warning',
};

const actionIcons: Record<string, React.ReactNode> = {
  CREATE: <FaPlus />,
  UPDATE: <FaEdit />,
  DELETE: <FaTrash />,
  LOGIN: <FaUser />,
  LOGOUT: <FaUser />,
  APPROVE: <FaEdit />,
  REJECT: <FaEdit />,
};

// API function for audit logs
const auditLogsApi = {
  list: async (params: {
    search?: string;
    action?: string;
    entityType?: string;
    userId?: number;
    fromDate?: string;
    toDate?: string;
    page?: number;
    size?: number;
  }) => {
    const response = await apiClient.get('/audit-logs', { params });
    return response.data;
  },
};

const AuditLogViewerPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [actionFilter, setActionFilter] = useState<string>('');
  const [entityTypeFilter, setEntityTypeFilter] = useState<string>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 20;

  // Fetch audit logs
  const { data: auditData, isLoading, error } = useQuery({
    queryKey: ['audit-logs', searchTerm, actionFilter, entityTypeFilter, dateFrom, dateTo, currentPage],
    queryFn: () => auditLogsApi.list({
      search: searchTerm || undefined,
      action: actionFilter || undefined,
      entityType: entityTypeFilter || undefined,
      fromDate: dateFrom || undefined,
      toDate: dateTo || undefined,
      page: currentPage,
      size: pageSize,
    }),
  });

  const handleExport = () => {
    const data = auditData?.content || [];
    const headers = ['ID', 'Action', 'Entity Type', 'Entity ID', 'Performed By', 'Date/Time', 'IP Address', 'Remarks'];
    const rows = data.map((log: AuditLog) => [
      log.id,
      log.action,
      log.entityType,
      log.entityId,
      log.performedBy,
      new Date(log.performedAt).toLocaleString(),
      log.ipAddress || '',
      log.remarks || '',
    ]);

    const csv = [headers.join(','), ...rows.map((r: (string | number)[]) => r.map(c => `"${c}"`).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-log-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
  };

  const columns = [
    {
      key: 'id',
      label: 'ID',
      render: (row: AuditLog) => <span className="text-muted">#{row.id}</span>,
    },
    {
      key: 'action',
      label: 'Action',
      render: (row: AuditLog) => (
        <Badge bg={actionColors[row.action] || 'secondary'} className="d-flex align-items-center gap-1" style={{ width: 'fit-content' }}>
          {actionIcons[row.action]} {row.action}
        </Badge>
      ),
    },
    {
      key: 'entityType',
      label: 'Entity',
      render: (row: AuditLog) => (
        <div>
          <strong>{row.entityType}</strong>
          <div className="text-muted small">ID: {row.entityId}</div>
          {row.entityName && <div className="text-muted small">{row.entityName}</div>}
        </div>
      ),
    },
    {
      key: 'performedBy',
      label: 'Performed By',
      render: (row: AuditLog) => (
        <div>
          <FaUser className="me-1 text-muted" />
          {row.performedBy}
        </div>
      ),
    },
    {
      key: 'performedAt',
      label: 'Date/Time',
      render: (row: AuditLog) => (
        <div>
          <div>{new Date(row.performedAt).toLocaleDateString()}</div>
          <div className="text-muted small">{new Date(row.performedAt).toLocaleTimeString()}</div>
        </div>
      ),
    },
    {
      key: 'ipAddress',
      label: 'IP Address',
      render: (row: AuditLog) => row.ipAddress || '-',
    },
    {
      key: 'changes',
      label: 'Changes',
      render: (row: AuditLog) => (
        <div style={{ maxWidth: '200px' }}>
          {row.oldValue && (
            <div className="text-danger small text-truncate" title={row.oldValue}>
              - {row.oldValue.substring(0, 50)}...
            </div>
          )}
          {row.newValue && (
            <div className="text-success small text-truncate" title={row.newValue}>
              + {row.newValue.substring(0, 50)}...
            </div>
          )}
          {!row.oldValue && !row.newValue && <span className="text-muted">-</span>}
        </div>
      ),
    },
  ];

  // Entity types from database schema
  const entityTypes = [
    'INDENT', 'PURCHASE_ORDER', 'GRN', 'ISSUE_NOTE', 'MATERIAL', 'VENDOR',
    'EMPLOYEE', 'EMPLOYEE', 'COMPANY', 'PLANT', 'DEPARTMENT', 'INVENTORY'
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading audit logs: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div className="audit-log-viewer">
      <PageHeader
        title="Audit Log"
        subtitle="View system activity and change history"
        actions={
          <Button variant="outline-success" onClick={handleExport}>
            <FaFileExport className="me-1" /> Export CSV
          </Button>
        }
      />

      {/* Stats */}
      <Row className="mb-3">
        <Col md={3}>
          <Card className="bg-primary bg-opacity-10 border-primary">
            <Card.Body className="text-center py-3">
              <h4 className="mb-1 text-primary">1,659</h4>
              <div className="text-muted small">Total Logs</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-success bg-opacity-10 border-success">
            <Card.Body className="text-center py-3">
              <h4 className="mb-1 text-success">245</h4>
              <div className="text-muted small">Today's Activities</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-info bg-opacity-10 border-info">
            <Card.Body className="text-center py-3">
              <h4 className="mb-1 text-info">18</h4>
              <div className="text-muted small">Active Users Today</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-warning bg-opacity-10 border-warning">
            <Card.Body className="text-center py-3">
              <h4 className="mb-1 text-warning">12</h4>
              <div className="text-muted small">Failed Logins</div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Filters */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={2}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  placeholder="Search..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </Col>
            <Col md={2}>
              <Form.Select
                value={actionFilter}
                onChange={(e) => setActionFilter(e.target.value)}
              >
                <option value="">All Actions</option>
                <option value="CREATE">Create</option>
                <option value="UPDATE">Update</option>
                <option value="DELETE">Delete</option>
                <option value="LOGIN">Login</option>
                <option value="LOGOUT">Logout</option>
                <option value="APPROVE">Approve</option>
                <option value="REJECT">Reject</option>
              </Form.Select>
            </Col>
            <Col md={2}>
              <Form.Select
                value={entityTypeFilter}
                onChange={(e) => setEntityTypeFilter(e.target.value)}
              >
                <option value="">All Entities</option>
                {entityTypes.map(type => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </Form.Select>
            </Col>
            <Col md={2}>
              <Form.Control
                type="date"
                placeholder="From"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
              />
            </Col>
            <Col md={2}>
              <Form.Control
                type="date"
                placeholder="To"
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
                  setActionFilter('');
                  setEntityTypeFilter('');
                  setDateFrom('');
                  setDateTo('');
                }}
              >
                <FaFilter className="me-1" /> Clear
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <LoadingSpinner text="Loading audit logs..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={auditData?.content || []}
          keyField="id"
          totalItems={auditData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}
    </div>
  );
};

export default AuditLogViewerPage;
