import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Form, InputGroup, Row, Col, Card } from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { FaPlus, FaSearch, FaFilter, FaEye, FaEdit, FaTimes, FaFileExport } from 'react-icons/fa';
import {
  PageHeader,
  DataTable,
  StatusBadge,
  ErrorAlert,
  Column,
} from '../../components/common';
import { useAuth } from '../../contexts/AuthContext';
import { indentsApi, companiesApi, departmentsApi, plantsApi, Indent, IndentStatus, IndentSearchParams } from '../../api';
import { INDENT_STATUS_COLORS } from '../../constants/indentStatus';

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

// Maps each display-status label to the three workflow-column IDs that produce it.
// Derived from IndentService.deriveDisplayStatus() compound matrix.
const DISPLAY_STATUS_FILTERS: Record<string, { approvedStatus?: number; finalStatus?: number; procurementStatus?: number }> = {
  'Pending RM Approval':  { approvedStatus: 1, finalStatus: 1, procurementStatus: 1 },
  'RM Rejected':          { approvedStatus: 2, finalStatus: 1 },
  'RM Approved':          { approvedStatus: 3, finalStatus: 1 },
  'Dept. Head Rejected':  { finalStatus: 2 },
  'Dept. Head Approved':  { approvedStatus: 3, finalStatus: 4, procurementStatus: 4 },
  'Quotations Collected': { approvedStatus: 3, finalStatus: 4, procurementStatus: 5 },
  'Negotiation Done':     { approvedStatus: 3, finalStatus: 4, procurementStatus: 6 },
  'PO Released':          { approvedStatus: 3, finalStatus: 4, procurementStatus: 7 },
  'Hold':                 { approvedStatus: 3, finalStatus: 4, procurementStatus: 8 },
  'Cash Buy':             { approvedStatus: 3, finalStatus: 4, procurementStatus: 9 },
};

const IndentsListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();

  const pageTitle = hasAnyRole(['ADMIN', 'SUPERADMIN', 'PROCUREMENT'])
    ? 'All Indents'
    : hasAnyRole(['DEPTHEAD', 'PLANTMANAGER'])
    ? 'Department Indents'
    : hasAnyRole(['SUPERVISOR'])
    ? 'My Team Indents'
    : 'My Indents';

  const [searchParams, setSearchParams] = useState<IndentSearchParams>({
    page: 0,
    size: 10,
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [selectedStatusLabel, setSelectedStatusLabel] = useState('');

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['indents', searchParams],
    queryFn: () => indentsApi.list(searchParams),
  });

  const { data: companiesData } = useQuery({
    queryKey: ['companies-active'],
    queryFn: () => companiesApi.getActive(0, 100),
  });
  const { data: departmentsData } = useQuery({
    queryKey: ['departments-active'],
    queryFn: () => departmentsApi.getActive(0, 100),
  });
  const { data: plantsData } = useQuery({
    queryKey: ['plants-active'],
    queryFn: () => plantsApi.getActive(0, 100),
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

  const handleStatusFilter = (label: string) => {
    setSelectedStatusLabel(label);
    const filters = label ? (DISPLAY_STATUS_FILTERS[label] ?? {}) : {};
    setSearchParams({
      ...searchParams,
      approvedStatus: filters.approvedStatus,
      finalStatus: filters.finalStatus,
      procurementStatus: filters.procurementStatus,
      page: 0,
    });
  };

  const handleExport = async (fmt: 'excel' | 'csv') => {
    try {
      const { page: _p, size: _s, ...filterParams } = searchParams;
      const blob = await indentsApi.export({ ...filterParams, format: fmt });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fmt === 'csv' ? 'indents_export.csv' : 'indents_export.xlsx';
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      alert('Export failed. You may not have permission.');
    }
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
        const ds: string | undefined = (item as any).displayStatus;
        const label = ds ?? (item as any).statusName ?? 'Unknown';
        const color = ds ? (INDENT_STATUS_COLORS[ds] ?? 'secondary') : 'secondary';
        return <span className={`badge bg-${color}`}>{label}</span>;
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
          {/* Edit only on a GENUINE draft: Spring status=1 AND no workflow progress in the
              three-column model. Legacy rows carry status=1 as an active flag even at terminal
              states (e.g. PO Released procurement=7 / Cash Buy=9), so the status=1 check alone
              wrongly showed Edit there — the three-column guard excludes those. */}
          {((item as any).statusId ?? (item as any).status) === 1 &&
            ((item as any).approvedStatusId ?? 1) === 1 &&
            ((item as any).finalStatusId ?? 1) === 1 &&
            ((item as any).procurementStatusId ?? 1) === 1 &&
            hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'DEPTHEAD', 'PROCUREMENT', 'SUPERVISOR']) && (
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
        title={pageTitle}
        subtitle="Manage purchase indent requests"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Indents' },
        ]}
        actions={
          <div className="d-flex gap-2">
            {hasAnyRole(['SUPERADMIN', 'ADMIN']) && (
              <>
                <Button variant="outline-success" size="sm" onClick={() => handleExport('excel')}>
                  <FaFileExport className="me-1" /> Excel
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => handleExport('csv')}>
                  <FaFileExport className="me-1" /> CSV
                </Button>
              </>
            )}
            {hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'DEPTHEAD', 'PROCUREMENT', 'SUPERVISOR']) && (
              <Button variant="primary" onClick={() => navigate('/indents/new')}>
                <FaPlus className="me-2" /> New Indent
              </Button>
            )}
          </div>
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
                value={selectedStatusLabel}
                onChange={(e) => handleStatusFilter(e.target.value)}
              >
                <option value="">All Statuses</option>
                <option value="Pending RM Approval">Pending RM Approval</option>
                <option value="RM Approved">RM Approved</option>
                <option value="RM Rejected">RM Rejected</option>
                <option value="Dept. Head Approved">Dept. Head Approved</option>
                <option value="Dept. Head Rejected">Dept. Head Rejected</option>
                <option value="Quotations Collected">Quotations Collected</option>
                <option value="Negotiation Done">Negotiation Done</option>
                <option value="PO Released">PO Released</option>
                <option value="Hold">Hold</option>
                <option value="Cash Buy">Cash Buy</option>
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
                  <Form.Label>Company</Form.Label>
                  <Form.Select
                    value={searchParams.companyId?.toString() || ''}
                    onChange={(e) => setSearchParams({ ...searchParams, companyId: e.target.value ? Number(e.target.value) : undefined, page: 0 })}
                  >
                    <option value="">All Companies</option>
                    {companiesData?.content?.map((c: any) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Department</Form.Label>
                  <Form.Select
                    value={searchParams.departmentId?.toString() || ''}
                    onChange={(e) => setSearchParams({ ...searchParams, departmentId: e.target.value ? Number(e.target.value) : undefined, page: 0 })}
                  >
                    <option value="">All Departments</option>
                    {departmentsData?.content?.map((d: any) => (
                      <option key={d.id} value={d.id}>{d.name}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Plant</Form.Label>
                  <Form.Select
                    value={searchParams.plantId?.toString() || ''}
                    onChange={(e) => setSearchParams({ ...searchParams, plantId: e.target.value ? Number(e.target.value) : undefined, page: 0 })}
                  >
                    <option value="">All Plants</option>
                    {plantsData?.content?.map((p: any) => (
                      <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
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
                    setSelectedStatusLabel('');
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
