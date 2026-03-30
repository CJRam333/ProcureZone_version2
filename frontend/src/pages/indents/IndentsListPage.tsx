import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Form, InputGroup, Row, Col, Card } from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { FaPlus, FaSearch, FaFilter, FaEye, FaEdit, FaTimes } from 'react-icons/fa';
import {
  PageHeader,
  DataTable,
  StatusBadge,
  ErrorAlert,
  Column,
} from '../../components/common';
import { useAuth } from '../../contexts/AuthContext';
import { indentsApi, Indent, IndentStatus, IndentSearchParams } from '../../api';

// Safe date formatter
const formatDate = (dateStr: string | null | undefined): string => {
  if (!dateStr) return 'N/A';
  try {
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return 'N/A';
    return format(date, 'dd MMM yyyy');
  } catch {
    return 'N/A';
  }
};

const statusMap: Record<number, { label: string; className: string }> = {
  1: { label: 'Draft', className: 'draft' },
  2: { label: 'Submitted', className: 'pending' },
  3: { label: 'Dept Head Approved', className: 'approved' },
  4: { label: 'Finance Approved', className: 'approved' },
  5: { label: 'Procurement Approved', className: 'completed' },
  6: { label: 'Rejected', className: 'rejected' },
  7: { label: 'On Hold', className: 'pending' },
  8: { label: 'Completed', className: 'completed' },
};

const IndentsListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();
  
  const [searchParams, setSearchParams] = useState<IndentSearchParams>({
    page: 0,
    size: 10,
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['indents', searchParams],
    queryFn: () => indentsApi.list(searchParams),
  });

  const handleSearch = () => {
    setSearchParams({ ...searchParams, search: searchTerm, page: 0 });
  };

  const handlePageChange = (page: number) => {
    setSearchParams({ ...searchParams, page });
  };

  const handlePageSizeChange = (size: number) => {
    setSearchParams({ ...searchParams, size, page: 0 });
  };

  const handleStatusFilter = (status: string) => {
    setSearchParams({
      ...searchParams,
      status: status ? (Number(status) as IndentStatus) : undefined,
      page: 0,
    });
  };

  const columns: Column<Indent>[] = [
    {
      key: 'indentNumber',
      label: 'Indent No.',
      sortable: true,
      render: (item) => (
        <span className="fw-medium text-primary">{item.indentNumber}</span>
      ),
    },
    {
      key: 'indentDate',
      label: 'Date',
      sortable: true,
      render: (item: any) => formatDate((item as any).indentDate),
    },
    {
      key: 'employeeName' as any,
      label: 'Requested By',
      render: (item: any) => (item as any).employeeName || (item as any).requestedByName || 'N/A',
    },
    {
      key: 'departmentName',
      label: 'Department',
    },
    {
      key: 'itemCount' as any,
      label: 'Items',
      render: (item: any) => (item as any).itemCount ?? (item as any).items?.length ?? '-',
    },
    {
      key: 'status',
      label: 'Status',
      render: (item: any) => {
        const sid = (item as any).statusId ?? (item as any).status;
        const mapped = statusMap[sid];
        if (mapped) {
          const bgClass = mapped.className === 'draft' ? 'secondary' : mapped.className === 'pending' ? 'warning' : mapped.className === 'approved' ? 'info' : mapped.className === 'completed' ? 'success' : mapped.className === 'rejected' ? 'danger' : 'secondary';
          return <span className={`badge bg-${bgClass}`}>{mapped.label}</span>;
        }
        const sname = (item as any).statusName;
        if (sname) return <span className="badge bg-secondary">{sname}</span>;
        return <span className="badge bg-secondary">Unknown</span>;
      },
    },
    {
      key: 'actions',
      label: 'Actions',
      width: '120px',
      render: (item) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            title="View"
            onClick={(e) => {
              e.stopPropagation();
              navigate(`/indents/${item.id}`);
            }}
          >
            <FaEye />
          </Button>
          {((item as any).statusId ?? (item as any).status) === 1 &&
            hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'EMPLOYEE', 'DEPTHEAD', 'PROCUREMENT']) && (
              <Button
                variant="outline-secondary"
                size="sm"
                title="Edit"
                onClick={(e) => {
                  e.stopPropagation();
                  navigate(`/indents/${item.id}/edit`);
                }}
              >
                <FaEdit />
              </Button>
            )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Indents"
        subtitle="Manage purchase indent requests"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Indents' },
        ]}
        actions={
          hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'EMPLOYEE', 'DEPTHEAD', 'PROCUREMENT']) && (
            <Button variant="primary" onClick={() => navigate('/indents/new')}>
              <FaPlus className="me-2" /> New Indent
            </Button>
          )
        }
      />

      {/* Search and Filters */}
      <Card className="mb-4">
        <Card.Body>
          <Row className="g-3">
            <Col md={6} lg={4}>
              <InputGroup>
                <Form.Control
                  placeholder="Search by indent number, purpose..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                />
                <Button variant="outline-secondary" onClick={handleSearch}>
                  <FaSearch />
                </Button>
              </InputGroup>
            </Col>
            <Col md={3} lg={2}>
              <Form.Select
                value={searchParams.status?.toString() || ''}
                onChange={(e) => handleStatusFilter(e.target.value)}
              >
                <option value="">All Statuses</option>
                <option value="1">Draft</option>
                <option value="2">Submitted</option>
                <option value="3">Dept Head Approved</option>
                <option value="5">Procurement Approved</option>
                <option value="6">Rejected</option>
                <option value="8">Completed</option>
              </Form.Select>
            </Col>
            <Col md="auto">
              <Button
                variant="outline-secondary"
                onClick={() => setShowFilters(!showFilters)}
              >
                <FaFilter className="me-2" />
                {showFilters ? 'Hide' : 'More'} Filters
              </Button>
            </Col>
          </Row>

          {showFilters && (
            <Row className="g-3 mt-2">
              <Col md={3}>
                <Form.Group>
                  <Form.Label>From Date</Form.Label>
                  <Form.Control
                    type="date"
                    value={searchParams.fromDate || ''}
                    onChange={(e) =>
                      setSearchParams({ ...searchParams, fromDate: e.target.value || undefined, page: 0 })
                    }
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>To Date</Form.Label>
                  <Form.Control
                    type="date"
                    value={searchParams.toDate || ''}
                    onChange={(e) =>
                      setSearchParams({ ...searchParams, toDate: e.target.value || undefined, page: 0 })
                    }
                  />
                </Form.Group>
              </Col>
              <Col md={3} className="d-flex align-items-end">
                <Button
                  variant="outline-danger"
                  onClick={() => {
                    setSearchParams({ page: 0, size: 10 });
                    setSearchTerm('');
                  }}
                >
                  <FaTimes className="me-2" /> Clear Filters
                </Button>
              </Col>
            </Row>
          )}
        </Card.Body>
      </Card>

      {/* Data Table */}
      {error ? (
        <ErrorAlert
          message="Failed to load indents. Please try again."
          onRetry={() => refetch()}
        />
      ) : (
        <DataTable<Indent>
          columns={columns}
          data={data?.content || []}
          keyField="id"
          loading={isLoading}
          emptyMessage="No indents found"
          totalItems={data?.totalElements || 0}
          currentPage={data?.number || 0}
          pageSize={data?.size || 10}
          onPageChange={handlePageChange}
          onPageSizeChange={handlePageSizeChange}
          onRowClick={(item) => navigate(`/indents/${item.id}`)}
        />
      )}
    </div>
  );
};

export default IndentsListPage;
