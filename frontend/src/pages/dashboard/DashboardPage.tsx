import React from 'react';
import { Row, Col, Card, Badge, Table, ProgressBar } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  FaFileAlt,
  FaClipboardList,
  FaBoxes,
  FaExclamationTriangle,
  FaArrowRight,
  FaArrowUp,
  FaArrowDown,
  FaCheckCircle,
  FaClock,
} from 'react-icons/fa';
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { PageHeader, LoadingSpinner, DashboardSummary } from '../../components/common';
import { useAuth } from '../../contexts/AuthContext';
import { reportsApi, grnApi } from '../../api';

// Chart colors
const COLORS = ['#0d6efd', '#198754', '#dc3545', '#ffc107', '#0dcaf0', '#6f42c1'];

interface StatCardProps {
  title: string;
  value: number | string;
  icon: React.ReactNode;
  iconBg: string;
  change?: number;
  linkTo?: string;
  linkText?: string;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  iconBg,
  change,
  linkTo,
  linkText,
}) => (
  <Card className="h-100 border-0 shadow-sm">
    <Card.Body>
      <div className="d-flex align-items-start justify-content-between">
        <div>
          <div className="text-muted small text-uppercase fw-semibold mb-1">{title}</div>
          <div className="h2 mb-0 fw-bold">{value}</div>
          {change !== undefined && (
            <div className={`small mt-1 ${change >= 0 ? 'text-success' : 'text-danger'}`}>
              {change >= 0 ? <FaArrowUp className="me-1" /> : <FaArrowDown className="me-1" />}
              {Math.abs(change)}% from last month
            </div>
          )}
        </div>
        <div className={`${iconBg} rounded-circle p-3`} style={{ opacity: 0.9 }}>
          {icon}
        </div>
      </div>
      {linkTo && (
        <Link
          to={linkTo}
          className="d-flex align-items-center gap-1 mt-3 text-decoration-none small"
        >
          {linkText || 'View all'} <FaArrowRight size={10} />
        </Link>
      )}
    </Card.Body>
  </Card>
);

// Sample trend data (in production, this would come from API)
const trendData = [
  { name: 'Jan', indents: 45 },
  { name: 'Feb', indents: 52 },
  { name: 'Mar', indents: 48 },
  { name: 'Apr', indents: 61 },
  { name: 'May', indents: 55 },
  { name: 'Jun', indents: 67 },
];



const DashboardPage: React.FC = () => {
  const { user, hasAnyRole } = useAuth();

  // Fetch dashboard stats
  // Fetch dashboard stats
  const { data: stats, isLoading: loadingStats } = useQuery({
    queryKey: ['dashboard', 'stats'],
    queryFn: () => reportsApi.getDashboardStats(),
    enabled: !!user,
  });

  const { data: recentGRNs } = useQuery({
    queryKey: ['dashboard', 'recentGRNs'],
    queryFn: () => grnApi.list({ page: 0, size: 5 }),
    enabled: hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'FLOORINCHARGE', 'GOODSINCHARGE']),
  });

  const { data: lowStock } = useQuery({
    queryKey: ['dashboard', 'lowStock'],
    queryFn: () => reportsApi.getLowStockItems(),
    enabled: hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'FLOORINCHARGE', 'GOODSINCHARGE']),
  });

  const isLoading = loadingStats;

  // Calculate totals for pie chart
  const statusDistribution = React.useMemo(() => [
    { name: 'Pending', value: (stats?.indentStats?.pending || 0) + (stats?.poStats?.pending || 0), color: '#ffc107' },
    { name: 'Approved', value: (stats?.indentStats?.approved || 0), color: '#198754' },
    { name: 'Rejected', value: (stats?.indentStats?.rejected || 0), color: '#dc3545' },
    { name: 'Delivered', value: (stats?.poStats?.delivered || 0), color: '#0d6efd' },
  ], [stats]);

  const totalStatusItems = statusDistribution.reduce((acc, item) => acc + item.value, 0);

  return (
    <div>
      <PageHeader
        title={`Welcome back, ${user?.displayName || user?.employeeName || user?.username || 'User'}!`}
        subtitle="Here's an overview of your procurement activities"
      />

      {isLoading ? (
        <LoadingSpinner />
      ) : (
        <>
          {/* Stats Row */}
          <Row className="g-4 mb-4">
            {hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'DEPTHEAD', 'SUPERVISOR']) && (
              <Col sm={6} xl={3}>
                <StatCard
                  title="Pending Approvals"
                  value={stats?.indentStats?.pending || 0}
                  icon={<FaFileAlt className="text-white" size={24} />}
                  iconBg="bg-primary"
                  linkTo="/indents/approval"
                  linkText="View pending"
                />
              </Col>
            )}

            {/* SCOPE-REDUCTION: "Active Purchase Orders" and "Pending GRN" stat cards hidden */}

            {hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'FLOORINCHARGE', 'GOODSINCHARGE']) && (
              <Col sm={6} xl={3}>
                <StatCard
                  title="Low Stock Alerts"
                  value={stats?.inventoryStats?.lowStock || 0}
                  icon={<FaBoxes className="text-white" size={24} />}
                  iconBg="bg-warning"
                  linkTo="/inventory?lowStock=true"
                  linkText="View inventory"
                />
              </Col>
            )}
          </Row>

          {/* Role-Based Workflow Widgets */}
          {user?.roles && user.roles.length > 0 && (
            <DashboardSummary stats={stats} userRoles={user.roles} />
          )}

          {/* Charts Row */}
          <Row className="g-4 mb-4">
            {/* Procurement Trends Chart */}
            <Col lg={8}>
              <Card className="h-100 border-0 shadow-sm">
                <Card.Header className="bg-transparent border-0 pb-0">
                  <h5 className="mb-0">Procurement Trends</h5>
                  <small className="text-muted">Monthly overview of procurement activities</small>
                </Card.Header>
                <Card.Body>
                  <ResponsiveContainer width="100%" height={300}>
                    <AreaChart data={trendData}>
                      <defs>
                        <linearGradient id="colorIndents" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#0d6efd" stopOpacity={0.3} />
                          <stop offset="95%" stopColor="#0d6efd" stopOpacity={0} />
                        </linearGradient>
                        {/* SCOPE-REDUCTION: colorPOs and colorGRNs gradients removed */}
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e9ecef" />
                      <XAxis dataKey="name" stroke="#6c757d" fontSize={12} />
                      <YAxis stroke="#6c757d" fontSize={12} />
                      <Tooltip
                        contentStyle={{
                          backgroundColor: '#fff',
                          border: '1px solid #e9ecef',
                          borderRadius: '8px',
                          boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                        }}
                      />
                      <Legend />
                      <Area
                        type="monotone"
                        dataKey="indents"
                        name="Indents"
                        stroke="#0d6efd"
                        fillOpacity={1}
                        fill="url(#colorIndents)"
                        strokeWidth={2}
                      />
                      {/* SCOPE-REDUCTION: Purchase Orders and GRNs Area series removed */}
                    </AreaChart>
                  </ResponsiveContainer>
                </Card.Body>
              </Card>
            </Col>

            {/* Status Distribution Pie Chart */}
            <Col lg={4}>
              <Card className="h-100 border-0 shadow-sm">
                <Card.Header className="bg-transparent border-0 pb-0">
                  <h5 className="mb-0">Indent Status</h5>
                  <small className="text-muted">Distribution by status</small>
                </Card.Header>
                <Card.Body className="d-flex flex-column align-items-center">
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie
                        data={statusDistribution}
                        cx="50%"
                        cy="50%"
                        innerRadius={50}
                        outerRadius={80}
                        paddingAngle={3}
                        dataKey="value"
                      >
                        {statusDistribution.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value: number) => [`${value} (${((value / totalStatusItems) * 100).toFixed(0)}%)`, '']}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="d-flex flex-wrap justify-content-center gap-3 mt-2">
                    {statusDistribution.map((item) => (
                      <div key={item.name} className="d-flex align-items-center gap-2">
                        <div
                          className="rounded-circle"
                          style={{ width: 10, height: 10, backgroundColor: item.color }}
                        />
                        <small className="text-muted">{item.name}: {item.value}</small>
                      </div>
                    ))}
                  </div>
                </Card.Body>
              </Card>
            </Col>
          </Row>

          {/* Quick Actions & Alerts Row */}
          <Row className="g-4 mb-4">
            <Col lg={6}>
              <Card className="h-100 border-0 shadow-sm">
                <Card.Header className="bg-transparent border-0">
                  <h5 className="mb-0">Quick Actions</h5>
                </Card.Header>
                <Card.Body>
                  <div className="d-flex flex-wrap gap-2">
                    {hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'SUPERVISOR']) && (
                      <Link to="/indents/new" className="btn btn-primary">
                        <FaFileAlt className="me-2" /> Create Indent
                      </Link>
                    )}
                    {/* SCOPE-REDUCTION: "Create PO" and "Create GRN" quick actions removed */}
                    {hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'ISSUECONFIRM', 'SUPERVISOR']) && (
                      <Link to="/issue-notes/new" className="btn btn-secondary">
                        <FaClipboardList className="me-2" /> Create Issue Note
                      </Link>
                    )}
                  </div>
                </Card.Body>
              </Card>
            </Col>

            {/* Low Stock Alerts */}
            {lowStock && lowStock.length > 0 && (
              <Col lg={6}>
                <Card className="h-100 border-0 shadow-sm border-start border-4 border-warning">
                  <Card.Header className="bg-transparent border-0">
                    <h5 className="mb-0 d-flex align-items-center">
                      <FaExclamationTriangle className="me-2 text-warning" />
                      Low Stock Alerts
                    </h5>
                  </Card.Header>
                  <Card.Body className="pt-0">
                    <ul className="list-group list-group-flush">
                      {(lowStock as any[]).slice(0, 4).map((item: any) => (
                        <li
                          key={`${item.materialId}`}
                          className="list-group-item px-0 d-flex justify-content-between align-items-center"
                        >
                          <div>
                            <div className="fw-medium">{item.materialName}</div>
                            <small className="text-muted">
                              Reorder at {item.reorderLevel}
                            </small>
                          </div>
                          <Badge bg="warning" text="dark" className="fs-6">
                            {item.currentStock} {item.uomName}
                          </Badge>
                        </li>
                      ))}
                    </ul>
                    {lowStock.length > 4 && (
                      <Link to="/inventory?lowStock=true" className="btn btn-link p-0 mt-2">
                        View all {lowStock.length} items →
                      </Link>
                    )}
                  </Card.Body>
                </Card>
              </Col>
            )}
          </Row>

          {/* Recent GRNs Table */}
          {recentGRNs?.content && recentGRNs.content.length > 0 && (
            <Row className="g-4">
              <Col>
                <Card className="border-0 shadow-sm">
                  <Card.Header className="bg-transparent border-0 d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">Recent GRN Activity</h5>
                    <Link to="/grn" className="btn btn-sm btn-outline-primary">
                      View All
                    </Link>
                  </Card.Header>
                  <Card.Body className="p-0">
                    <div className="table-responsive">
                      <Table className="mb-0" hover>
                        <thead className="table-light">
                          <tr>
                            <th>GRN Number</th>
                            <th>PO Number</th>
                            <th>Vendor</th>
                            <th>Date</th>
                            <th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {recentGRNs.content.slice(0, 5).map((grn: any) => (
                            <tr key={grn.id}>
                              <td>
                                <Link to={`/grn/${grn.id}`} className="fw-medium text-decoration-none">
                                  {grn.grnNumber}
                                </Link>
                              </td>
                              <td>{grn.poNumber}</td>
                              <td>{grn.vendorName}</td>
                              <td>{new Date(grn.grnDate).toLocaleDateString()}</td>
                              <td>
                                <Badge
                                  bg={
                                    grn.statusName === 'Posted' ? 'success' :
                                      grn.statusName === 'QC Approved' ? 'info' :
                                        grn.statusName === 'Pending QC' ? 'warning' : 'secondary'
                                  }
                                >
                                  {grn.statusName || 'Draft'}
                                </Badge>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </Table>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          )}
        </>
      )}
    </div>
  );
};

export default DashboardPage;
