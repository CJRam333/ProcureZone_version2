import React, { useState } from 'react';
import { Card, Row, Col, Form, Button, Table, Badge, Nav, Tab, Spinner, Alert, Modal } from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format, subDays, startOfMonth, endOfMonth, startOfYear } from 'date-fns';
import {
  FaFileAlt,
  FaDownload,
  FaChartBar,
  FaChartLine,
  FaWarehouse,
  FaShoppingCart,
  FaClipboardList,
  FaFilePdf,
  FaFileExcel,
  FaEye,
  FaSyncAlt,
} from 'react-icons/fa';
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { reportsApi, getErrorMessage } from '../../api';
import type { ReportParams, ReportSummary } from '../../api/reports';

// Chart Colors
const COLORS = ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#0dcaf0', '#6610f2', '#fd7e14', '#20c997'];

interface ReportCard {
  title: string;
  description: string;
  icon: React.ReactNode;
  reportType: string;
  apiFunction: (params: ReportParams) => Promise<any>;
  usesDates?: boolean;
}

const ReportsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('procurement');
  const [selectedReport, setSelectedReport] = useState<string | null>(null);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportData, setReportData] = useState<any>(null);
  const [reportLoading, setReportLoading] = useState(false);
  const [reportError, setReportError] = useState<string | null>(null);
  const [dateRange, setDateRange] = useState({
    fromDate: format(startOfMonth(new Date()), 'yyyy-MM-dd'),
    toDate: format(endOfMonth(new Date()), 'yyyy-MM-dd'),
  });

  // Report configurations
  const reportCategories: Record<string, ReportCard[]> = {
    procurement: [
      {
        title: 'Indent Summary Report',
        description: 'Summary of all indents by status, department, and priority',
        icon: <FaClipboardList className="text-primary" size={24} />,
        reportType: 'indent-summary',
        apiFunction: reportsApi.getIndentSummary,
      },
      {
        title: 'Indent Details Report',
        description: 'Detailed list of all indents with item breakdown',
        icon: <FaClipboardList className="text-info" size={24} />,
        reportType: 'indent-details',
        apiFunction: reportsApi.getIndentDetails,
      },
      // SCOPE-REDUCTION: "Purchase Order Report" and "Vendor Performance" hidden — not active in current production phase
    ],
    inventory: [
      {
        title: 'Stock Status Report',
        description: 'Current stock levels across all materials',
        icon: <FaWarehouse className="text-primary" size={24} />,
        reportType: 'inventory-summary',
        apiFunction: reportsApi.getInventorySummary,
      },
      {
        title: 'Inventory Details',
        description: 'Detailed inventory list with stock levels',
        icon: <FaWarehouse className="text-info" size={24} />,
        reportType: 'inventory-details',
        apiFunction: reportsApi.getInventoryDetails,
      },
      {
        title: 'Low Stock Alert',
        description: 'Materials below reorder level',
        icon: <FaWarehouse className="text-danger" size={24} />,
        reportType: 'low-stock',
        apiFunction: reportsApi.getLowStockItems,
      },
    ],
    financial: [
      {
        title: 'Purchase Value Report',
        description: 'Total purchase value by period, vendor, category',
        icon: <FaChartBar className="text-primary" size={24} />,
        reportType: 'po-summary',
        apiFunction: reportsApi.getPOSummary,
      },
      {
        title: 'Indent Value Analysis',
        description: 'Indent value analysis by department and period',
        icon: <FaChartLine className="text-warning" size={24} />,
        reportType: 'indent-summary',
        apiFunction: reportsApi.getIndentSummary,
      },
    ],
  };

  // Quick date range presets
  const applyPreset = (preset: string) => {
    const today = new Date();
    switch (preset) {
      case 'today':
        setDateRange({
          fromDate: format(today, 'yyyy-MM-dd'),
          toDate: format(today, 'yyyy-MM-dd'),
        });
        break;
      case 'week':
        setDateRange({
          fromDate: format(subDays(today, 7), 'yyyy-MM-dd'),
          toDate: format(today, 'yyyy-MM-dd'),
        });
        break;
      case 'month':
        setDateRange({
          fromDate: format(startOfMonth(today), 'yyyy-MM-dd'),
          toDate: format(endOfMonth(today), 'yyyy-MM-dd'),
        });
        break;
      case 'quarter':
        setDateRange({
          fromDate: format(subDays(today, 90), 'yyyy-MM-dd'),
          toDate: format(today, 'yyyy-MM-dd'),
        });
        break;
      case 'year':
        setDateRange({
          fromDate: format(startOfYear(today), 'yyyy-MM-dd'),
          toDate: format(today, 'yyyy-MM-dd'),
        });
        break;
    }
  };

  // Generate Report
  const handleGenerateReport = async (report: ReportCard) => {
    setSelectedReport(report.reportType);
    setReportLoading(true);
    setReportError(null);
    setShowReportModal(true);

    try {
      const params: ReportParams = {
        startDate: dateRange.fromDate,
        endDate: dateRange.toDate,
      };
      const data = await report.apiFunction(params);
      setReportData(data);
    } catch (err) {
      setReportError(getErrorMessage(err));
    } finally {
      setReportLoading(false);
    }
  };

  // Export Report - supports PDF, Excel, and CSV formats
  const handleExport = async (reportType: string, outputFormat: 'pdf' | 'excel') => {
    try {
      const params: ReportParams = {
        startDate: dateRange.fromDate,
        endDate: dateRange.toDate,
        format: outputFormat,
      };
      const blob = await reportsApi.exportReport(reportType, params);

      // Create download link with correct file extension
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      // Use correct extension based on format
      const extension = outputFormat === 'pdf' ? 'pdf' : 'xlsx';
      link.download = `${reportType}-${dateRange.fromDate}-${dateRange.toDate}.${extension}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert(`Export failed: ${getErrorMessage(err)}`);
    }
  };

  // Render Charts for Summary
  const renderCharts = (summary: ReportSummary) => {
    const statusData = summary.byStatus
      ? Object.entries(summary.byStatus).map(([name, value]) => ({ name, value }))
      : [];

    const deptData = summary.byDepartment
      ? Object.entries(summary.byDepartment).map(([name, value]) => ({ name, value }))
      : [];

    if (statusData.length === 0 && deptData.length === 0) return null;

    return (
      <Row className="mb-4">
        {statusData.length > 0 && (
          <Col md={6}>
            <Card className="h-100">
              <Card.Header>Status Distribution</Card.Header>
              <Card.Body>
                <div style={{ width: '100%', height: 300 }}>
                  <ResponsiveContainer>
                    <PieChart>
                      <Pie
                        data={statusData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        fill="#8884d8"
                        paddingAngle={5}
                        dataKey="value"
                        label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                      >
                        {statusData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend verticalAlign="bottom" height={36} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </Card.Body>
            </Card>
          </Col>
        )}

        {deptData.length > 0 && (
          <Col md={6}>
            <Card className="h-100">
              <Card.Header>Department Breakdown</Card.Header>
              <Card.Body>
                <div style={{ width: '100%', height: 300 }}>
                  <ResponsiveContainer>
                    <BarChart data={deptData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis allowDecimals={false} />
                      <Tooltip />
                      <Bar dataKey="value" fill="#0d6efd" name="Count" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </Card.Body>
            </Card>
          </Col>
        )}
      </Row>
    );
  };

  // Render report summary data
  const renderSummaryReport = (summary: ReportSummary) => (
    <div>
      <Row className="g-3 mb-4">
        <Col sm={6} md={3}>
          <Card className="bg-primary text-white">
            <Card.Body className="text-center">
              <div className="h2 mb-0">{summary.totalCount}</div>
              <small>Total Count</small>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} md={3}>
          <Card className="bg-success text-white">
            <Card.Body className="text-center">
              <div className="h2 mb-0">₹{(summary.totalValue || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}</div>
              <small>Total Value</small>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} md={3}>
          <Card className="bg-warning text-dark">
            <Card.Body className="text-center">
              <div className="h2 mb-0">{summary.pendingCount || 0}</div>
              <small>Pending</small>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} md={3}>
          <Card className="bg-info text-white">
            <Card.Body className="text-center">
              <div className="h2 mb-0">{summary.approvedCount || 0}</div>
              <small>Approved</small>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Render Visualizations */}
      {renderCharts(summary)}

      {summary.byStatus && Object.keys(summary.byStatus).length > 0 && (
        <Card className="mb-3">
          <Card.Header>Summary Details</Card.Header>
          <Card.Body className="p-0">
            <Table className="mb-0" hover>
              <thead className="table-light">
                <tr>
                  <th>Status / Category</th>
                  <th className="text-end">Count</th>
                  <th className="text-end">Share</th>
                </tr>
              </thead>
              <tbody>
                {Object.entries(summary.byStatus).map(([status, count]) => (
                  <tr key={status}>
                    <td>{status}</td>
                    <td className="text-end">{count}</td>
                    <td className="text-end">
                      {Math.round((count / summary.totalCount) * 100)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      )}
    </div>
  );

  // Render list report data
  const renderListReport = (data: any[]) => {
    if (!data || data.length === 0) {
      return <Alert variant="info">No data found for the selected period.</Alert>;
    }

    const columns = Object.keys(data[0]);
    return (
      <div className="table-responsive">
        <Table striped hover size="sm">
          <thead className="table-light">
            <tr>
              {columns.map((col) => (
                <th key={col}>{col.replace(/([A-Z])/g, ' $1').trim()}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.slice(0, 50).map((row, idx) => (
              <tr key={idx}>
                {columns.map((col) => (
                  <td key={col}>
                    {typeof row[col] === 'number'
                      ? row[col].toLocaleString('en-IN')
                      : row[col] || '-'}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </Table>
        {data.length > 50 && (
          <div className="text-center text-muted py-2">
            Showing first 50 of {data.length} records. Export for full data.
          </div>
        )}
      </div>
    );
  };

  return (
    <div>
      <PageHeader
        title="Reports"
        subtitle="Generate and export various procurement reports"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Reports' },
        ]}
      />

      {/* Date Range Filter */}
      <Card className="mb-4">
        <Card.Body>
          <Row className="align-items-end">
            <Col md={3}>
              <Form.Group>
                <Form.Label className="small text-muted">From Date</Form.Label>
                <Form.Control
                  type="date"
                  value={dateRange.fromDate}
                  onChange={(e) => setDateRange({ ...dateRange, fromDate: e.target.value })}
                />
              </Form.Group>
            </Col>
            <Col md={3}>
              <Form.Group>
                <Form.Label className="small text-muted">To Date</Form.Label>
                <Form.Control
                  type="date"
                  value={dateRange.toDate}
                  onChange={(e) => setDateRange({ ...dateRange, toDate: e.target.value })}
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <div className="d-flex gap-2 flex-wrap">
                <Button variant="outline-secondary" size="sm" onClick={() => applyPreset('today')}>
                  Today
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => applyPreset('week')}>
                  Last 7 Days
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => applyPreset('month')}>
                  This Month
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => applyPreset('quarter')}>
                  Last Quarter
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => applyPreset('year')}>
                  This Year
                </Button>
              </div>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Report Categories */}
      <Tab.Container activeKey={activeTab} onSelect={(k) => setActiveTab(k || 'procurement')}>
        <Card>
          <Card.Header>
            <Nav variant="tabs" className="card-header-tabs">
              <Nav.Item>
                <Nav.Link eventKey="procurement">
                  <FaShoppingCart className="me-2" />
                  Procurement
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link eventKey="inventory">
                  <FaWarehouse className="me-2" />
                  Inventory
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link eventKey="financial">
                  <FaChartBar className="me-2" />
                  Financial
                </Nav.Link>
              </Nav.Item>
            </Nav>
          </Card.Header>
          <Card.Body>
            <Tab.Content>
              {Object.entries(reportCategories).map(([category, reports]) => (
                <Tab.Pane key={category} eventKey={category}>
                  <Row>
                    {reports.map((report) => (
                      <Col lg={6} xl={3} className="mb-4" key={report.reportType + report.title}>
                        <Card className="h-100 report-card hover-lift">
                          <Card.Body className="d-flex flex-column">
                            <div className="mb-3">{report.icon}</div>
                            <h6 className="mb-2">{report.title}</h6>
                            <p className="text-muted small flex-grow-1">{report.description}</p>
                            <div className="d-flex gap-2 mt-3 flex-wrap">
                              <Button
                                variant="primary"
                                size="sm"
                                onClick={() => handleGenerateReport(report)}
                              >
                                <FaEye className="me-1" /> View
                              </Button>
                              <Button
                                variant="outline-success"
                                size="sm"
                                onClick={() => handleExport(report.reportType, 'excel')}
                                title="Export to Excel"
                              >
                                <FaFileExcel />
                              </Button>
                              <Button
                                variant="outline-danger"
                                size="sm"
                                onClick={() => handleExport(report.reportType, 'pdf')}
                                title="Export to PDF"
                              >
                                <FaFilePdf />
                              </Button>
                            </div>
                          </Card.Body>
                        </Card>
                      </Col>
                    ))}
                  </Row>
                </Tab.Pane>
              ))}
            </Tab.Content>
          </Card.Body>
        </Card>
      </Tab.Container>

      {/* Report Modal */}
      <Modal
        show={showReportModal}
        onHide={() => setShowReportModal(false)}
        size="xl"
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>
            {selectedReport?.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase())} Report
          </Modal.Title>
        </Modal.Header>
        <Modal.Body style={{ maxHeight: '70vh', overflowY: 'auto' }}>
          {reportLoading ? (
            <div className="text-center py-5">
              <Spinner animation="border" variant="primary" />
              <p className="mt-2">Generating report...</p>
            </div>
          ) : reportError ? (
            <Alert variant="danger">{reportError}</Alert>
          ) : reportData ? (
            Array.isArray(reportData)
              ? renderListReport(reportData)
              : renderSummaryReport(reportData as ReportSummary)
          ) : null}
        </Modal.Body>
        <Modal.Footer>
          <div className="d-flex gap-2">
            <Button variant="outline-success" onClick={() => handleExport(selectedReport!, 'excel')}>
              <FaFileExcel className="me-1" /> Export Excel
            </Button>
            <Button variant="outline-danger" onClick={() => handleExport(selectedReport!, 'pdf')}>
              <FaFilePdf className="me-1" /> Export PDF
            </Button>
          </div>
          <Button variant="secondary" onClick={() => setShowReportModal(false)}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>

      <style>{`
        .report-card {
          transition: transform 0.2s, box-shadow 0.2s;
          cursor: pointer;
        }
        .report-card:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        .hover-lift:hover {
          border-color: var(--bs-primary);
        }
      `}</style>
    </div>
  );
};

export default ReportsPage;
