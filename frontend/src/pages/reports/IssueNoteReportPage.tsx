import React, { useState } from 'react';
import { Card, Row, Col, Form, Button, Table, Badge, Spinner, Alert } from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format, subDays } from 'date-fns';
import { FaFileExcel, FaFileCsv, FaSearch, FaSync } from 'react-icons/fa';
import { PageHeader } from '../../components/common';
import { departmentsApi, getErrorMessage } from '../../api';
import apiClient from '../../api/client';
import { useAuth } from '../../contexts/AuthContext';

const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: '1', label: 'Draft' },
  { value: '2', label: 'Pending RM Approval' },
  { value: '3', label: 'Awaiting Stores' },
  { value: '5', label: 'Rejected by RM' },
  { value: '8', label: 'Goods Issued' },
  { value: '9', label: 'Rejected by Stores' },
  { value: '10', label: 'Returned' },
];

const IssueNoteReportPage: React.FC = () => {
  const { hasAnyRole } = useAuth();
  const canExport = hasAnyRole(['SUPERADMIN', 'ADMIN']);

  const today = format(new Date(), 'yyyy-MM-dd');
  const thirtyDaysAgo = format(subDays(new Date(), 30), 'yyyy-MM-dd');

  const [fromDate, setFromDate] = useState(thirtyDaysAgo);
  const [toDate, setToDate] = useState(today);
  const [departmentId, setDepartmentId] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);

  const { data: departments } = useQuery({
    queryKey: ['departments'],
    queryFn: () => departmentsApi.getAll(0, 200),
  });

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['issue-note-report', fromDate, toDate, departmentId, status, page],
    queryFn: async () => {
      const params = new URLSearchParams({ page: String(page), size: '20' });
      if (departmentId) params.set('departmentId', departmentId);
      if (status) params.set('status', status);
      const res = await apiClient.get(`/issue-notes?${params}`);
      return res.data;
    },
  });

  const handleExport = (fmt: 'excel' | 'csv') => {
    const params = new URLSearchParams({ format: fmt });
    if (fromDate) params.set('fromDate', fromDate);
    if (toDate) params.set('toDate', toDate);
    if (departmentId) params.set('departmentId', departmentId);
    if (status) params.set('status', status);
    const url = `/api/v1/issue-notes/export?${params}`;
    window.open(url, '_blank');
  };

  const notes: any[] = data?.content || [];
  const totalPages: number = data?.totalPages || 0;
  const totalItems: number = data?.totalItems || 0;

  const statusBadgeColor = (s: number) => {
    if (s === 8) return 'success';
    if (s === 5 || s === 9) return 'danger';
    if (s === 3) return 'warning';
    if (s === 2) return 'info';
    return 'secondary';
  };

  return (
    <div>
      <PageHeader
        title="Issue Note Report"
        subtitle="Filter and export issue note records"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Reports', path: '/reports' },
          { label: 'Issue Notes' },
        ]}
      />

      <Card className="mb-4">
        <Card.Header><h6 className="mb-0">Filters</h6></Card.Header>
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={3}>
              <Form.Label className="small">From Date</Form.Label>
              <Form.Control type="date" value={fromDate} onChange={e => { setFromDate(e.target.value); setPage(0); }} />
            </Col>
            <Col md={3}>
              <Form.Label className="small">To Date</Form.Label>
              <Form.Control type="date" value={toDate} onChange={e => { setToDate(e.target.value); setPage(0); }} />
            </Col>
            <Col md={2}>
              <Form.Label className="small">Department</Form.Label>
              <Form.Select value={departmentId} onChange={e => { setDepartmentId(e.target.value); setPage(0); }}>
                <option value="">All Departments</option>
                {departments?.content?.map((d: any) => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </Form.Select>
            </Col>
            <Col md={2}>
              <Form.Label className="small">Status</Form.Label>
              <Form.Select value={status} onChange={e => { setStatus(e.target.value); setPage(0); }}>
                {STATUS_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
              </Form.Select>
            </Col>
            <Col md={2} className="d-flex gap-2">
              <Button variant="primary" className="flex-grow-1" onClick={() => refetch()}>
                <FaSearch className="me-1" /> Search
              </Button>
              <Button variant="outline-secondary" onClick={() => { setFromDate(thirtyDaysAgo); setToDate(today); setDepartmentId(''); setStatus(''); setPage(0); }}>
                <FaSync />
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {error && <Alert variant="danger">{getErrorMessage(error)}</Alert>}

      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <span>
            Results <Badge bg="secondary">{totalItems}</Badge>
          </span>
          {canExport && (
            <div className="d-flex gap-2">
              <Button size="sm" variant="outline-success" onClick={() => handleExport('excel')}>
                <FaFileExcel className="me-1" /> Excel
              </Button>
              <Button size="sm" variant="outline-secondary" onClick={() => handleExport('csv')}>
                <FaFileCsv className="me-1" /> CSV
              </Button>
            </div>
          )}
        </Card.Header>
        <Card.Body className="p-0">
          {isLoading ? (
            <div className="text-center py-5"><Spinner animation="border" /></div>
          ) : (
            <div className="table-responsive">
              <Table hover className="mb-0">
                <thead className="bg-light">
                  <tr>
                    <th>Issue Note No.</th>
                    <th>Date</th>
                    <th>Department</th>
                    <th>Issued To</th>
                    <th>Status</th>
                    <th className="text-center">Items</th>
                    <th className="text-end">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {notes.length === 0 ? (
                    <tr><td colSpan={7} className="text-center text-muted py-4">No issue notes found for the selected filters</td></tr>
                  ) : (
                    notes.map((note: any) => (
                      <tr key={note.id}>
                        <td><a href={`/issue-notes/${note.id}`} className="text-decoration-none fw-medium">{note.issueNoteNumber}</a></td>
                        <td>{note.issueDate ? format(new Date(note.issueDate), 'dd MMM yyyy') : '-'}</td>
                        <td>{note.departmentId || '-'}</td>
                        <td>{note.issuedTo || '-'}</td>
                        <td><Badge bg={statusBadgeColor(note.status)} className="text-nowrap">{note.statusDescription || '-'}</Badge></td>
                        <td className="text-center">{note.detailCount ?? '-'}</td>
                        <td className="text-end">{note.totalAmount != null ? `₹${Number(note.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : '-'}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </Table>
            </div>
          )}
        </Card.Body>
        {totalPages > 1 && (
          <Card.Footer className="d-flex justify-content-between align-items-center">
            <small className="text-muted">Page {page + 1} of {totalPages}</small>
            <div className="d-flex gap-2">
              <Button size="sm" variant="outline-secondary" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Prev</Button>
              <Button size="sm" variant="outline-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</Button>
            </div>
          </Card.Footer>
        )}
      </Card>
    </div>
  );
};

export default IssueNoteReportPage;
