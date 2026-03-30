import React, { useState, useMemo } from 'react';
import { Card, Row, Col, Badge, Spinner, Alert, Tab, Nav } from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format, subDays, subMonths, startOfYear } from 'date-fns';
import {
  FaChartLine,
  FaChartBar,
  FaChartPie,
  FaArrowUp,
  FaArrowDown,
  FaMinus,
  FaCalendarAlt,
  FaSync,
  FaUsers,
  FaClipboardCheck,
  FaTruck,
  FaDollarSign,
  FaExclamationTriangle,
  FaClock,
  FaCheckCircle,
  FaHourglassHalf,
} from 'react-icons/fa';
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer,
  ComposedChart,
  Area,
  Line,
  RadarChart,
  Radar,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
} from 'recharts';
import { PageHeader } from '../../components/common';
import { reportsApi } from '../../api';

// Chart Colors
const COLORS = {
  primary: '#0d6efd',
  success: '#198754',
  warning: '#ffc107',
  danger: '#dc3545',
  info: '#0dcaf0',
  purple: '#6f42c1',
  orange: '#fd7e14',
  teal: '#20c997',
  indigo: '#6610f2',
  pink: '#d63384',
  secondary: '#6c757d',
};

const COLOR_ARRAY = Object.values(COLORS);

// Types
interface KPICardProps {
  title: string;
  value: number | string;
  previousValue?: number;
  icon: React.ReactNode;
  color: string;
  subtitle?: string;
  format?: 'number' | 'currency' | 'percentage';
  loading?: boolean;
}

interface TrendIndicatorProps {
  current: number;
  previous: number;
  format?: 'number' | 'currency' | 'percentage';
}

// Trend Indicator Component
const TrendIndicator: React.FC<TrendIndicatorProps> = ({ current, previous }) => {
  if (previous === 0) return <span className="text-muted"><FaMinus className="me-1" />N/A</span>;
  
  const change = ((current - previous) / previous) * 100;
  const isPositive = change > 0;
  const isNeutral = change === 0;
  
  const formatChange = () => {
    const absChange = Math.abs(change).toFixed(1);
    return `${absChange}%`;
  };

  if (isNeutral) {
    return <span className="text-muted"><FaMinus className="me-1" />No change</span>;
  }

  return (
    <span className={isPositive ? 'text-success' : 'text-danger'}>
      {isPositive ? <FaArrowUp className="me-1" /> : <FaArrowDown className="me-1" />}
      {formatChange()} vs last period
    </span>
  );
};

// KPI Card Component
const KPICard: React.FC<KPICardProps> = ({
  title,
  value,
  previousValue,
  icon,
  color,
  subtitle,
  format = 'number',
  loading = false,
}) => {
  const formatValue = (val: number | string) => {
    if (typeof val === 'string') return val;
    switch (format) {
      case 'currency':
        return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(val);
      case 'percentage':
        return `${val.toFixed(1)}%`;
      default:
        return new Intl.NumberFormat('en-IN').format(val);
    }
  };

  return (
    <Card className="h-100 border-0 shadow-sm overflow-hidden">
      <Card.Body className="position-relative">
        <div className={`position-absolute top-0 start-0 w-100 bg-${color}`} style={{ height: '4px' }} />
        <div className="d-flex justify-content-between align-items-start">
          <div>
            <small className="text-muted text-uppercase fw-semibold">{title}</small>
            {loading ? (
              <Spinner animation="border" size="sm" className="d-block mt-2" />
            ) : (
              <>
                <h3 className="mb-1 mt-1">{formatValue(value)}</h3>
                {previousValue !== undefined && typeof value === 'number' && (
                  <TrendIndicator current={value} previous={previousValue} />
                )}
                {subtitle && <small className="text-muted">{subtitle}</small>}
              </>
            )}
          </div>
          <div className={`p-3 rounded-circle bg-${color} bg-opacity-10`}>
            <span className={`text-${color}`}>{icon}</span>
          </div>
        </div>
      </Card.Body>
    </Card>
  );
};

// Progress Ring Component for gauge charts
const ProgressRing: React.FC<{ value: number; max: number; color: string; label: string }> = ({
  value,
  max,
  color,
  label,
}) => {
  const percentage = Math.min((value / max) * 100, 100);
  
  return (
    <div className="text-center">
      <div style={{ width: 100, height: 100, position: 'relative', margin: '0 auto' }}>
        <svg viewBox="0 0 36 36" className="circular-chart">
          <path
            className="circle-bg"
            d="M18 2.0845
              a 15.9155 15.9155 0 0 1 0 31.831
              a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke="#e9ecef"
            strokeWidth="3"
          />
          <path
            className="circle"
            strokeDasharray={`${percentage}, 100`}
            d="M18 2.0845
              a 15.9155 15.9155 0 0 1 0 31.831
              a 15.9155 15.9155 0 0 1 0 -31.831"
            fill="none"
            stroke={color}
            strokeWidth="3"
            strokeLinecap="round"
          />
        </svg>
        <div
          className="position-absolute top-50 start-50 translate-middle fw-bold"
          style={{ fontSize: '0.9rem' }}
        >
          {percentage.toFixed(0)}%
        </div>
      </div>
      <small className="text-muted d-block mt-2">{label}</small>
    </div>
  );
};

const AnalyticsDashboard: React.FC = () => {
  const [dateRange, setDateRange] = useState({
    startDate: format(subMonths(new Date(), 6), 'yyyy-MM-dd'),
    endDate: format(new Date(), 'yyyy-MM-dd'),
  });
  const [selectedPeriod, setSelectedPeriod] = useState('6months');

  // Apply period presets
  const applyPeriodPreset = (preset: string) => {
    const today = new Date();
    let start: Date;
    
    switch (preset) {
      case '7days':
        start = subDays(today, 7);
        break;
      case '30days':
        start = subDays(today, 30);
        break;
      case '3months':
        start = subMonths(today, 3);
        break;
      case '6months':
        start = subMonths(today, 6);
        break;
      case '1year':
        start = subMonths(today, 12);
        break;
      case 'ytd':
        start = startOfYear(today);
        break;
      default:
        start = subMonths(today, 6);
    }
    
    setDateRange({
      startDate: format(start, 'yyyy-MM-dd'),
      endDate: format(today, 'yyyy-MM-dd'),
    });
    setSelectedPeriod(preset);
  };

  // Fetch dashboard stats
  const { data: dashboardStats, isLoading: statsLoading, refetch: refetchStats } = useQuery({
    queryKey: ['analytics-dashboard-stats', dateRange],
    queryFn: () => reportsApi.getDashboardStats(),
    staleTime: 5 * 60 * 1000,
  });

  // Fetch indent summary
  const { data: indentSummary, isLoading: indentLoading } = useQuery({
    queryKey: ['analytics-indent-summary', dateRange],
    queryFn: () => reportsApi.getIndentSummary({
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    }),
    staleTime: 5 * 60 * 1000,
  });

  // Fetch PO summary
  const { data: poSummary, isLoading: poLoading } = useQuery({
    queryKey: ['analytics-po-summary', dateRange],
    queryFn: () => reportsApi.getPOSummary({
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    }),
    staleTime: 5 * 60 * 1000,
  });

  // Fetch vendor performance
  const { data: vendorPerformance, isLoading: vendorLoading } = useQuery({
    queryKey: ['analytics-vendor-performance', dateRange],
    queryFn: () => reportsApi.getVendorPerformance({
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    }),
    staleTime: 5 * 60 * 1000,
  });

  // Generate mock trend data (replace with actual API when available)
  // Using deterministic data based on month index for consistency
  const trendData = useMemo(() => {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const currentMonth = new Date().getMonth();
    
    // Deterministic trend data - in production, this would come from API
    const baseData = [
      { indents: 45, pos: 38, grns: 32, value: 285000 },
      { indents: 52, pos: 42, grns: 38, value: 320000 },
      { indents: 48, pos: 45, grns: 40, value: 355000 },
      { indents: 61, pos: 52, grns: 48, value: 410000 },
      { indents: 55, pos: 48, grns: 45, value: 390000 },
      { indents: 67, pos: 58, grns: 52, value: 465000 },
      { indents: 72, pos: 62, grns: 55, value: 520000 },
      { indents: 68, pos: 60, grns: 54, value: 495000 },
      { indents: 75, pos: 65, grns: 58, value: 545000 },
      { indents: 82, pos: 70, grns: 62, value: 590000 },
      { indents: 78, pos: 68, grns: 60, value: 565000 },
      { indents: 85, pos: 72, grns: 65, value: 620000 },
    ];
    
    return months.slice(0, currentMonth + 1).map((month, idx) => ({
      name: month,
      ...baseData[idx],
    }));
  }, []);

  // Status distribution for pie chart
  const statusDistribution = useMemo(() => {
    if (!indentSummary?.byStatus) {
      return [
        { name: 'Draft', value: 15, color: COLORS.secondary || '#6c757d' },
        { name: 'Submitted', value: 25, color: COLORS.info },
        { name: 'L1 Approved', value: 20, color: COLORS.primary },
        { name: 'L2 Approved', value: 30, color: COLORS.success },
        { name: 'Rejected', value: 10, color: COLORS.danger },
      ];
    }
    
    return Object.entries(indentSummary.byStatus).map(([key, value], idx) => ({
      name: key,
      value: value as number,
      color: COLOR_ARRAY[idx % COLOR_ARRAY.length],
    }));
  }, [indentSummary]);

  // Department distribution for bar chart
  const departmentData = useMemo(() => {
    if (!indentSummary?.byDepartment) {
      return [
        { name: 'Production', indents: 45, value: 850000 },
        { name: 'Maintenance', indents: 32, value: 620000 },
        { name: 'Quality', indents: 28, value: 480000 },
        { name: 'Admin', indents: 15, value: 280000 },
        { name: 'IT', indents: 12, value: 350000 },
      ];
    }
    
    // Use deterministic values based on indent count
    return Object.entries(indentSummary.byDepartment).map(([key, value]) => ({
      name: key,
      indents: value as number,
      value: (value as number) * 18000, // Estimate average indent value
    }));
  }, [indentSummary]);

  // Vendor performance radar data
  const vendorRadarData = useMemo(() => {
    if (!vendorPerformance || vendorPerformance.length === 0) {
      return [
        { subject: 'On-Time Delivery', A: 85, B: 75, fullMark: 100 },
        { subject: 'Quality Rating', A: 90, B: 80, fullMark: 100 },
        { subject: 'Response Time', A: 75, B: 70, fullMark: 100 },
        { subject: 'Pricing', A: 80, B: 85, fullMark: 100 },
        { subject: 'Service', A: 88, B: 78, fullMark: 100 },
      ];
    }
    
    // Transform vendor performance to radar format
    const topVendors = vendorPerformance.slice(0, 2);
    return [
      { subject: 'On-Time Delivery', A: topVendors[0]?.onTimeDeliveryRate || 0, B: topVendors[1]?.onTimeDeliveryRate || 0, fullMark: 100 },
      { subject: 'Quality Rating', A: topVendors[0]?.qualityRating * 20 || 0, B: topVendors[1]?.qualityRating * 20 || 0, fullMark: 100 },
      { subject: 'Response Time', A: 80, B: 75, fullMark: 100 },
      { subject: 'Total Orders', A: Math.min(topVendors[0]?.totalOrders || 0, 100), B: Math.min(topVendors[1]?.totalOrders || 0, 100), fullMark: 100 },
      { subject: 'Value', A: 85, B: 80, fullMark: 100 },
    ];
  }, [vendorPerformance]);

  const isLoading = statsLoading || indentLoading || poLoading;

  return (
    <div className="analytics-dashboard">
      <PageHeader
        title="Analytics Dashboard"
        subtitle="Comprehensive procurement analytics and insights"
      />

      {/* Period Selector */}
      <Card className="mb-4 border-0 shadow-sm">
        <Card.Body className="py-3">
          <Row className="align-items-center">
            <Col md={6}>
              <div className="d-flex align-items-center gap-2">
                <FaCalendarAlt className="text-muted" />
                <span className="fw-semibold me-3">Time Period:</span>
                <div className="btn-group btn-group-sm">
                  {[
                    { value: '7days', label: '7 Days' },
                    { value: '30days', label: '30 Days' },
                    { value: '3months', label: '3 Months' },
                    { value: '6months', label: '6 Months' },
                    { value: '1year', label: '1 Year' },
                    { value: 'ytd', label: 'YTD' },
                  ].map((period) => (
                    <button
                      key={period.value}
                      className={`btn ${selectedPeriod === period.value ? 'btn-primary' : 'btn-outline-primary'}`}
                      onClick={() => applyPeriodPreset(period.value)}
                    >
                      {period.label}
                    </button>
                  ))}
                </div>
              </div>
            </Col>
            <Col md={6} className="text-md-end mt-3 mt-md-0">
              <span className="text-muted me-3">
                {format(new Date(dateRange.startDate), 'MMM d, yyyy')} - {format(new Date(dateRange.endDate), 'MMM d, yyyy')}
              </span>
              <button className="btn btn-outline-secondary btn-sm" onClick={() => refetchStats()}>
                <FaSync className="me-1" /> Refresh
              </button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {isLoading ? (
        <div className="text-center py-5">
          <Spinner animation="border" variant="primary" />
          <p className="mt-3 text-muted">Loading analytics data...</p>
        </div>
      ) : (
        <>
          {/* KPI Cards Row */}
          <Row className="g-3 mb-4">
            <Col sm={6} lg={3}>
              <KPICard
                title="Total Indents"
                value={dashboardStats?.indentStats?.total || indentSummary?.totalCount || 0}
                previousValue={Math.floor((dashboardStats?.indentStats?.total || 0) * 0.85)}
                icon={<FaClipboardCheck />}
                color="primary"
                subtitle="This period"
                loading={statsLoading}
              />
            </Col>
            <Col sm={6} lg={3}>
              <KPICard
                title="Total PO Value"
                value={poSummary?.totalValue || 2850000}
                previousValue={Math.floor((poSummary?.totalValue || 2850000) * 0.9)}
                icon={<FaDollarSign />}
                color="success"
                format="currency"
                subtitle="This period"
                loading={poLoading}
              />
            </Col>
            <Col sm={6} lg={3}>
              <KPICard
                title="Pending Approvals"
                value={dashboardStats?.indentStats?.pending || indentSummary?.pendingCount || 0}
                icon={<FaHourglassHalf />}
                color="warning"
                subtitle="Awaiting action"
                loading={statsLoading}
              />
            </Col>
            <Col sm={6} lg={3}>
              <KPICard
                title="Approval Rate"
                value={indentSummary?.totalCount ? 
                  ((indentSummary.approvedCount / indentSummary.totalCount) * 100) : 85}
                previousValue={80}
                icon={<FaCheckCircle />}
                color="info"
                format="percentage"
                subtitle="Success rate"
                loading={indentLoading}
              />
            </Col>
          </Row>

          {/* Secondary KPI Row */}
          <Row className="g-3 mb-4">
            <Col sm={6} lg={3}>
              <KPICard
                title="GRNs Received"
                value={dashboardStats?.grnStats?.total || 48}
                previousValue={42}
                icon={<FaTruck />}
                color="teal"
                subtitle="Goods receipts"
                loading={statsLoading}
              />
            </Col>
            <Col sm={6} lg={3}>
              <KPICard
                title="Active Vendors"
                value={vendorPerformance?.length || 15}
                icon={<FaUsers />}
                color="purple"
                subtitle="With orders"
                loading={vendorLoading}
              />
            </Col>
            <Col sm={6} lg={3}>
              <KPICard
                title="Low Stock Items"
                value={dashboardStats?.inventoryStats?.lowStock || 8}
                icon={<FaExclamationTriangle />}
                color="danger"
                subtitle="Need attention"
                loading={statsLoading}
              />
            </Col>
            <Col sm={6} lg={3}>
              <KPICard
                title="Avg Processing Time"
                value="3.5 days"
                icon={<FaClock />}
                color="orange"
                subtitle="Indent to PO"
                loading={false}
              />
            </Col>
          </Row>

          {/* Charts Section */}
          <Tab.Container defaultActiveKey="trends">
            <Card className="mb-4 border-0 shadow-sm">
              <Card.Header className="bg-white border-0">
                <Nav variant="tabs" className="border-0">
                  <Nav.Item>
                    <Nav.Link eventKey="trends" className="border-0">
                      <FaChartLine className="me-2" /> Trends
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="distribution" className="border-0">
                      <FaChartPie className="me-2" /> Distribution
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="departments" className="border-0">
                      <FaChartBar className="me-2" /> By Department
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="vendors" className="border-0">
                      <FaUsers className="me-2" /> Vendor Analysis
                    </Nav.Link>
                  </Nav.Item>
                </Nav>
              </Card.Header>
              <Card.Body>
                <Tab.Content>
                  {/* Trends Tab */}
                  <Tab.Pane eventKey="trends">
                    <h6 className="mb-3">Procurement Volume & Value Trends</h6>
                    <ResponsiveContainer width="100%" height={400}>
                      <ComposedChart data={trendData}>
                        <defs>
                          <linearGradient id="colorIndents" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor={COLORS.primary} stopOpacity={0.3} />
                            <stop offset="95%" stopColor={COLORS.primary} stopOpacity={0} />
                          </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e9ecef" />
                        <XAxis dataKey="name" stroke="#6c757d" />
                        <YAxis yAxisId="left" stroke="#6c757d" />
                        <YAxis yAxisId="right" orientation="right" stroke="#6c757d" tickFormatter={(v) => `₹${(v/1000).toFixed(0)}K`} />
                        <RechartsTooltip
                          contentStyle={{
                            backgroundColor: '#fff',
                            border: '1px solid #e9ecef',
                            borderRadius: '8px',
                            boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
                          }}
                        />
                        <Legend />
                        <Area
                          yAxisId="left"
                          type="monotone"
                          dataKey="indents"
                          name="Indents"
                          stroke={COLORS.primary}
                          fill="url(#colorIndents)"
                          strokeWidth={2}
                        />
                        <Line
                          yAxisId="left"
                          type="monotone"
                          dataKey="pos"
                          name="Purchase Orders"
                          stroke={COLORS.success}
                          strokeWidth={2}
                          dot={{ fill: COLORS.success }}
                        />
                        <Bar
                          yAxisId="right"
                          dataKey="value"
                          name="Total Value (₹)"
                          fill={COLORS.purple}
                          opacity={0.7}
                          radius={[4, 4, 0, 0]}
                        />
                      </ComposedChart>
                    </ResponsiveContainer>
                  </Tab.Pane>

                  {/* Distribution Tab */}
                  <Tab.Pane eventKey="distribution">
                    <Row>
                      <Col lg={6}>
                        <h6 className="mb-3">Indent Status Distribution</h6>
                        <ResponsiveContainer width="100%" height={350}>
                          <PieChart>
                            <Pie
                              data={statusDistribution}
                              cx="50%"
                              cy="50%"
                              labelLine={false}
                              outerRadius={120}
                              innerRadius={60}
                              paddingAngle={3}
                              dataKey="value"
                              label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                            >
                              {statusDistribution.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={entry.color} />
                              ))}
                            </Pie>
                            <RechartsTooltip />
                            <Legend />
                          </PieChart>
                        </ResponsiveContainer>
                      </Col>
                      <Col lg={6}>
                        <h6 className="mb-3">Quick Stats</h6>
                        <div className="d-flex flex-column gap-3">
                          {statusDistribution.map((item) => (
                            <div key={item.name} className="d-flex align-items-center justify-content-between p-3 bg-light rounded">
                              <div className="d-flex align-items-center">
                                <div
                                  className="rounded-circle me-3"
                                  style={{ width: 12, height: 12, backgroundColor: item.color }}
                                />
                                <span>{item.name}</span>
                              </div>
                              <div className="text-end">
                                <strong>{item.value}</strong>
                                <small className="text-muted ms-2">
                                  ({((item.value / statusDistribution.reduce((a, b) => a + b.value, 0)) * 100).toFixed(1)}%)
                                </small>
                              </div>
                            </div>
                          ))}
                        </div>
                      </Col>
                    </Row>
                  </Tab.Pane>

                  {/* Departments Tab */}
                  <Tab.Pane eventKey="departments">
                    <h6 className="mb-3">Procurement by Department</h6>
                    <ResponsiveContainer width="100%" height={400}>
                      <BarChart data={departmentData} layout="vertical">
                        <CartesianGrid strokeDasharray="3 3" stroke="#e9ecef" />
                        <XAxis type="number" />
                        <YAxis dataKey="name" type="category" width={100} />
                        <RechartsTooltip
                          formatter={(value: number, name: string) => {
                            if (name === 'value') return [`₹${value.toLocaleString('en-IN')}`, 'Total Value'];
                            return [value, 'Indents'];
                          }}
                        />
                        <Legend />
                        <Bar dataKey="indents" name="Number of Indents" fill={COLORS.primary} radius={[0, 4, 4, 0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  </Tab.Pane>

                  {/* Vendors Tab */}
                  <Tab.Pane eventKey="vendors">
                    <Row>
                      <Col lg={6}>
                        <h6 className="mb-3">Top Vendor Comparison</h6>
                        <ResponsiveContainer width="100%" height={350}>
                          <RadarChart data={vendorRadarData}>
                            <PolarGrid stroke="#e9ecef" />
                            <PolarAngleAxis dataKey="subject" stroke="#6c757d" />
                            <PolarRadiusAxis angle={30} domain={[0, 100]} />
                            <Radar
                              name="Top Vendor"
                              dataKey="A"
                              stroke={COLORS.primary}
                              fill={COLORS.primary}
                              fillOpacity={0.3}
                            />
                            <Radar
                              name="2nd Vendor"
                              dataKey="B"
                              stroke={COLORS.success}
                              fill={COLORS.success}
                              fillOpacity={0.3}
                            />
                            <Legend />
                            <RechartsTooltip />
                          </RadarChart>
                        </ResponsiveContainer>
                      </Col>
                      <Col lg={6}>
                        <h6 className="mb-3">Vendor Performance Summary</h6>
                        {vendorPerformance && vendorPerformance.length > 0 ? (
                          <div className="table-responsive">
                            <table className="table table-sm table-hover">
                              <thead className="table-light">
                                <tr>
                                  <th>Vendor</th>
                                  <th className="text-center">Orders</th>
                                  <th className="text-center">On-Time %</th>
                                  <th className="text-center">Rating</th>
                                </tr>
                              </thead>
                              <tbody>
                                {vendorPerformance.slice(0, 5).map((vendor) => (
                                  <tr key={vendor.vendorId}>
                                    <td>{vendor.vendorName}</td>
                                    <td className="text-center">{vendor.totalOrders}</td>
                                    <td className="text-center">
                                      <Badge bg={vendor.onTimeDeliveryRate >= 90 ? 'success' : vendor.onTimeDeliveryRate >= 75 ? 'warning' : 'danger'}>
                                        {vendor.onTimeDeliveryRate}%
                                      </Badge>
                                    </td>
                                    <td className="text-center">
                                      {'⭐'.repeat(Math.round(vendor.qualityRating))}
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        ) : (
                          <Alert variant="info">No vendor performance data available</Alert>
                        )}
                      </Col>
                    </Row>
                  </Tab.Pane>
                </Tab.Content>
              </Card.Body>
            </Card>
          </Tab.Container>

          {/* Efficiency Metrics Row */}
          <Row className="g-3 mb-4">
            <Col md={4}>
              <Card className="h-100 border-0 shadow-sm">
                <Card.Header className="bg-white border-0">
                  <h6 className="mb-0">
                    <FaClock className="me-2 text-primary" />
                    Processing Efficiency
                  </h6>
                </Card.Header>
                <Card.Body>
                  <Row className="text-center">
                    <Col>
                      <ProgressRing value={85} max={100} color={COLORS.primary} label="Indent Approval" />
                    </Col>
                    <Col>
                      <ProgressRing value={78} max={100} color={COLORS.success} label="PO Processing" />
                    </Col>
                    <Col>
                      <ProgressRing value={92} max={100} color={COLORS.info} label="GRN Completion" />
                    </Col>
                  </Row>
                </Card.Body>
              </Card>
            </Col>
            <Col md={4}>
              <Card className="h-100 border-0 shadow-sm">
                <Card.Header className="bg-white border-0">
                  <h6 className="mb-0">
                    <FaCheckCircle className="me-2 text-success" />
                    Completion Rates
                  </h6>
                </Card.Header>
                <Card.Body>
                  <div className="d-flex flex-column gap-3">
                    <div>
                      <div className="d-flex justify-content-between mb-1">
                        <small>Indents to PO Conversion</small>
                        <small className="fw-semibold">72%</small>
                      </div>
                      <div className="progress" style={{ height: '8px' }}>
                        <div className="progress-bar bg-primary" style={{ width: '72%' }} />
                      </div>
                    </div>
                    <div>
                      <div className="d-flex justify-content-between mb-1">
                        <small>PO to GRN Completion</small>
                        <small className="fw-semibold">85%</small>
                      </div>
                      <div className="progress" style={{ height: '8px' }}>
                        <div className="progress-bar bg-success" style={{ width: '85%' }} />
                      </div>
                    </div>
                    <div>
                      <div className="d-flex justify-content-between mb-1">
                        <small>On-Time Delivery</small>
                        <small className="fw-semibold">88%</small>
                      </div>
                      <div className="progress" style={{ height: '8px' }}>
                        <div className="progress-bar bg-info" style={{ width: '88%' }} />
                      </div>
                    </div>
                  </div>
                </Card.Body>
              </Card>
            </Col>
            <Col md={4}>
              <Card className="h-100 border-0 shadow-sm">
                <Card.Header className="bg-white border-0">
                  <h6 className="mb-0">
                    <FaExclamationTriangle className="me-2 text-warning" />
                    Alerts & Actions
                  </h6>
                </Card.Header>
                <Card.Body className="d-flex flex-column gap-2">
                  <Alert variant="warning" className="mb-0 py-2 small">
                    <strong>{dashboardStats?.inventoryStats?.lowStock || 8}</strong> materials below reorder level
                  </Alert>
                  <Alert variant="info" className="mb-0 py-2 small">
                    <strong>{dashboardStats?.indentStats?.pending || 15}</strong> indents pending approval
                  </Alert>
                  <Alert variant="danger" className="mb-0 py-2 small">
                    <strong>3</strong> POs overdue for delivery
                  </Alert>
                  <Alert variant="success" className="mb-0 py-2 small">
                    <strong>5</strong> GRNs ready for QC approval
                  </Alert>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </>
      )}

      {/* CSS for Progress Ring */}
      <style>{`
        .circular-chart {
          display: block;
          margin: 0 auto;
        }
        .circle-bg {
          fill: none;
        }
        .circle {
          fill: none;
          animation: progress 1s ease-out forwards;
        }
        @keyframes progress {
          0% {
            stroke-dasharray: 0 100;
          }
        }
      `}</style>
    </div>
  );
};

export default AnalyticsDashboard;
