import React, { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Form,
  Button,
  Table,
  Badge,
  Nav,
  Tab,
  Modal,
  InputGroup,
  Alert,
  Spinner,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import { toast } from 'react-toastify';
import {
  FaUsers,
  FaCog,
  FaKey,
  FaBuilding,
  FaSearch,
  FaPlus,
  FaEdit,
  FaTrash,
  FaUserShield,
  FaSync,
  FaDatabase,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner, DataTable } from '../../components/common';
import { adminApi, usersApi, rolesApi, getErrorMessage } from '../../api';

const AdminPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('users');
  const [showUserModal, setShowUserModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const queryClient = useQueryClient();

  // Fetch users from API
  const { data: usersData, isLoading: usersLoading, error: usersError } = useQuery({
    queryKey: ['users', searchTerm],
    queryFn: () => usersApi.getAll(0, 100, searchTerm || undefined),
  });

  // Fetch roles from API
  const { data: rolesData, isLoading: rolesLoading, error: rolesError } = useQuery({
    queryKey: ['roles'],
    queryFn: () => rolesApi.getAll(0, 50),
  });

  // Fetch system configs from API
  const { data: configsData, isLoading: configsLoading, error: configsError } = useQuery({
    queryKey: ['admin-configs'],
    queryFn: adminApi.getConfigs,
  });

  // Fetch system stats
  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: adminApi.getSystemStats,
  });

  const users = usersData?.content || [];
  const roles = rolesData?.content || [];
  const systemSettings = configsData || [];

  const getRoleVariant = (role: string): string => {
    const variants: Record<string, string> = {
      SUPERADMIN: 'danger',
      ADMIN: 'primary',
      MANAGER: 'info',
      PURCHASE: 'success',
      STORES: 'warning',
      QUALITY: 'secondary',
      AUDITOR: 'dark',
    };
    return variants[role] || 'secondary';
  };

  return (
    <div>
      <PageHeader
        title="Administration"
        subtitle="Manage users, roles, and system settings"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Admin' },
        ]}
      />

      <Tab.Container activeKey={activeTab} onSelect={(k) => setActiveTab(k || 'users')}>
        <Row>
          <Col lg={3}>
            <Card className="mb-4">
              <Card.Body className="p-0">
                <Nav variant="pills" className="flex-column admin-nav">
                  <Nav.Item>
                    <Nav.Link eventKey="users" className="d-flex align-items-center">
                      <FaUsers className="me-2" /> Users
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="roles" className="d-flex align-items-center">
                      <FaUserShield className="me-2" /> Roles & Permissions
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="settings" className="d-flex align-items-center">
                      <FaCog className="me-2" /> System Settings
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="company" className="d-flex align-items-center">
                      <FaBuilding className="me-2" /> Company Setup
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="security" className="d-flex align-items-center">
                      <FaKey className="me-2" /> Security
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="maintenance" className="d-flex align-items-center">
                      <FaDatabase className="me-2" /> Maintenance
                    </Nav.Link>
                  </Nav.Item>
                </Nav>
              </Card.Body>
            </Card>

            {/* Quick Stats */}
            <Card>
              <Card.Header>
                <h6 className="mb-0">Quick Stats</h6>
              </Card.Header>
              <Card.Body>
                {statsLoading ? (
                  <div className="text-center py-3">
                    <Spinner size="sm" />
                  </div>
                ) : (
                  <>
                    <div className="mb-3">
                      <small className="text-muted">Total Users</small>
                      <div className="h4 mb-0">{statsData?.totalUsers || users.length}</div>
                    </div>
                    <div className="mb-3">
                      <small className="text-muted">Active Users</small>
                      <div className="h4 mb-0 text-success">
                        {statsData?.activeUsers || users.filter(u => u.status === 1).length}
                      </div>
                    </div>
                    <div>
                      <small className="text-muted">Roles Defined</small>
                      <div className="h4 mb-0 text-primary">{roles.length}</div>
                    </div>
                  </>
                )}
              </Card.Body>
            </Card>
          </Col>

          <Col lg={9}>
            <Tab.Content>
              {/* Users Tab */}
              <Tab.Pane eventKey="users">
                <Card>
                  <Card.Header className="d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">User Management</h5>
                    <Button variant="primary" size="sm" onClick={() => setShowUserModal(true)}>
                      <FaPlus className="me-2" /> Add User
                    </Button>
                  </Card.Header>
                  <Card.Body>
                    <InputGroup className="mb-3">
                      <InputGroup.Text>
                        <FaSearch />
                      </InputGroup.Text>
                      <Form.Control
                        placeholder="Search users..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                      />
                    </InputGroup>

                    {usersLoading ? (
                      <div className="text-center py-5">
                        <Spinner animation="border" variant="primary" />
                        <p className="mt-2 text-muted">Loading users...</p>
                      </div>
                    ) : usersError ? (
                      <Alert variant="danger">
                        Error loading users: {getErrorMessage(usersError)}
                      </Alert>
                    ) : (
                      <Table responsive hover>
                        <thead className="table-light">
                          <tr>
                            <th>User</th>
                            <th>Username</th>
                            <th>Status</th>
                            <th>Last Login IP</th>
                            <th>Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          {users.length === 0 ? (
                            <tr>
                              <td colSpan={5} className="text-center py-4 text-muted">
                                No users found
                              </td>
                            </tr>
                          ) : users.map(user => (
                            <tr key={user.id}>
                              <td>
                                <div className="fw-medium">{user.employeeName || 'N/A'}</div>
                                <small className="text-muted">{user.employeeEmail || '-'}</small>
                              </td>
                              <td>{user.username}</td>
                              <td>
                                <Badge bg={user.status === 1 ? 'success' : 'secondary'}>
                                  {user.status === 1 ? 'Active' : 'Inactive'}
                                </Badge>
                              </td>
                              <td>
                                <small>{user.lastLoginIp || '-'}</small>
                              </td>
                              <td>
                                <Button variant="link" size="sm" className="p-0 me-2">
                                  <FaEdit />
                                </Button>
                                <Button variant="link" size="sm" className="p-0 text-danger">
                                  <FaTrash />
                                </Button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </Table>
                    )}
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* Roles Tab */}
              <Tab.Pane eventKey="roles">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0">Roles & Permissions</h5>
                  </Card.Header>
                  <Card.Body>
                    {rolesLoading ? (
                      <div className="text-center py-5">
                        <Spinner animation="border" variant="primary" />
                        <p className="mt-2 text-muted">Loading roles...</p>
                      </div>
                    ) : rolesError ? (
                      <Alert variant="danger">
                        Error loading roles: {getErrorMessage(rolesError)}
                      </Alert>
                    ) : (
                      <Row>
                        {roles.length === 0 ? (
                          <Col>
                            <p className="text-center text-muted py-4">No roles found</p>
                          </Col>
                        ) : roles.map(role => (
                          <Col md={6} lg={4} className="mb-3" key={role.id}>
                            <Card className="h-100">
                              <Card.Body>
                                <div className="d-flex justify-content-between align-items-start">
                                  <div>
                                    <Badge bg={getRoleVariant(role.code)} className="mb-2">
                                      {role.name}
                                    </Badge>
                                    <p className="text-muted small mb-2">Code: {role.code}</p>
                                    <div className="small">
                                      {role.canView && <Badge bg="light" text="dark" className="me-1">View</Badge>}
                                      {role.canAdd && <Badge bg="light" text="dark" className="me-1">Add</Badge>}
                                      {role.canEdit && <Badge bg="light" text="dark" className="me-1">Edit</Badge>}
                                      {role.canDelete && <Badge bg="light" text="dark" className="me-1">Delete</Badge>}
                                    </div>
                                  </div>
                                  <Button variant="link" size="sm">
                                    <FaEdit />
                                  </Button>
                                </div>
                              </Card.Body>
                            </Card>
                          </Col>
                        ))}
                      </Row>
                    )}
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* Settings Tab */}
              <Tab.Pane eventKey="settings">
                <Card>
                  <Card.Header className="d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">System Settings</h5>
                    <Button variant="primary" size="sm">
                      Save Changes
                    </Button>
                  </Card.Header>
                  <Card.Body>
                    {configsLoading ? (
                      <div className="text-center py-5">
                        <Spinner animation="border" variant="primary" />
                        <p className="mt-2 text-muted">Loading settings...</p>
                      </div>
                    ) : configsError ? (
                      <Alert variant="danger">
                        Error loading settings: {getErrorMessage(configsError)}
                      </Alert>
                    ) : systemSettings.length === 0 ? (
                      <Alert variant="info">
                        No system settings found. Configure default settings in the database.
                      </Alert>
                    ) : (
                      <Row>
                        {systemSettings.map(setting => (
                          <Col md={6} className="mb-3" key={setting.id || setting.configKey}>
                            <Form.Group>
                              <Form.Label>{setting.description || setting.configKey}</Form.Label>
                              <Form.Control
                                type="text"
                                defaultValue={setting.configValue}
                                disabled={!setting.isEditable}
                              />
                              {!setting.isEditable && (
                                <Form.Text className="text-muted">This setting is read-only</Form.Text>
                              )}
                            </Form.Group>
                          </Col>
                        ))}
                      </Row>
                    )}
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* Company Setup Tab */}
              <Tab.Pane eventKey="company">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0">Company Setup</h5>
                  </Card.Header>
                  <Card.Body>
                    <Row>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Company Name</Form.Label>
                          <Form.Control type="text" defaultValue="ProcureZone Industries" />
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>GSTIN</Form.Label>
                          <Form.Control type="text" placeholder="Enter GSTIN" />
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>PAN</Form.Label>
                          <Form.Control type="text" placeholder="Enter PAN" />
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Contact Email</Form.Label>
                          <Form.Control type="email" defaultValue="info@procurezone.com" />
                        </Form.Group>
                      </Col>
                      <Col xs={12}>
                        <Form.Group className="mb-3">
                          <Form.Label>Address</Form.Label>
                          <Form.Control as="textarea" rows={3} placeholder="Enter company address" />
                        </Form.Group>
                      </Col>
                    </Row>
                    <Button variant="primary">Save Company Details</Button>
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* Security Tab */}
              <Tab.Pane eventKey="security">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0">Security Settings</h5>
                  </Card.Header>
                  <Card.Body>
                    <Row>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Password Minimum Length</Form.Label>
                          <Form.Control type="number" defaultValue={8} />
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Session Timeout (minutes)</Form.Label>
                          <Form.Control type="number" defaultValue={30} />
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Check
                          type="switch"
                          id="require-uppercase"
                          label="Require uppercase in password"
                          defaultChecked
                          className="mb-3"
                        />
                      </Col>
                      <Col md={6}>
                        <Form.Check
                          type="switch"
                          id="require-numbers"
                          label="Require numbers in password"
                          defaultChecked
                          className="mb-3"
                        />
                      </Col>
                      <Col md={6}>
                        <Form.Check
                          type="switch"
                          id="two-factor"
                          label="Enable Two-Factor Authentication"
                          className="mb-3"
                        />
                      </Col>
                      <Col md={6}>
                        <Form.Check
                          type="switch"
                          id="login-audit"
                          label="Enable Login Audit Log"
                          defaultChecked
                          className="mb-3"
                        />
                      </Col>
                    </Row>
                    <Button variant="primary">Save Security Settings</Button>
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* Maintenance Tab */}
              <Tab.Pane eventKey="maintenance">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0">System Maintenance</h5>
                  </Card.Header>
                  <Card.Body>
                    <Row>
                      <Col md={6} className="mb-4">
                        <Card className="h-100 bg-light">
                          <Card.Body>
                            <h6><FaSync className="me-2" />Clear Cache</h6>
                            <p className="text-muted small">
                              Clear application cache to refresh data
                            </p>
                            <Button variant="outline-primary" size="sm">
                              Clear Cache
                            </Button>
                          </Card.Body>
                        </Card>
                      </Col>
                      <Col md={6} className="mb-4">
                        <Card className="h-100 bg-light">
                          <Card.Body>
                            <h6><FaDatabase className="me-2" />Database Backup</h6>
                            <p className="text-muted small">
                              Create a backup of the database
                            </p>
                            <Button variant="outline-primary" size="sm">
                              Create Backup
                            </Button>
                          </Card.Body>
                        </Card>
                      </Col>
                    </Row>
                  </Card.Body>
                </Card>
              </Tab.Pane>
            </Tab.Content>
          </Col>
        </Row>
      </Tab.Container>

      {/* Add User Modal */}
      <Modal show={showUserModal} onHide={() => setShowUserModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Add New User</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>Username</Form.Label>
              <Form.Control type="text" placeholder="Enter username" />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Full Name</Form.Label>
              <Form.Control type="text" placeholder="Enter full name" />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Email</Form.Label>
              <Form.Control type="email" placeholder="Enter email" />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Role</Form.Label>
              <Form.Select>
                <option value="">Select Role</option>
                {roles.map(role => (
                  <option key={role.id} value={role.id}>{role.name}</option>
                ))}
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Initial Password</Form.Label>
              <Form.Control type="password" placeholder="Enter password" />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowUserModal(false)}>
            Cancel
          </Button>
          <Button variant="primary" onClick={() => setShowUserModal(false)}>
            Create User
          </Button>
        </Modal.Footer>
      </Modal>

      <style>{`
        .admin-nav .nav-link {
          border-radius: 0;
          padding: 0.75rem 1rem;
          color: var(--bs-body-color);
        }
        .admin-nav .nav-link:hover {
          background-color: var(--bs-gray-100);
        }
        .admin-nav .nav-link.active {
          background-color: var(--bs-primary);
          color: white;
        }
      `}</style>
    </div>
  );
};

export default AdminPage;
