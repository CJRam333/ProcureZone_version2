import React, { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Form,
  Button,
  Badge,
  Alert,
  Spinner,
  ListGroup,
  Nav,
  Tab,
  Table,
} from 'react-bootstrap';
import {
  FaCog,
  FaBell,
  FaPalette,
  FaGlobe,
  FaShieldAlt,
  FaSave,
  FaMoon,
  FaSun,
  FaEnvelope,
  FaMobile,
  FaDesktop,
} from 'react-icons/fa';
import { PageHeader } from '../../components/common';
import { useAuth } from '../../contexts/AuthContext';

interface NotificationSetting {
  id: string;
  title: string;
  description: string;
  email: boolean;
  push: boolean;
  inApp: boolean;
}

const SettingsPage: React.FC = () => {
  const { user } = useAuth();
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);

  // Theme settings
  const [theme, setTheme] = useState<'light' | 'dark' | 'system'>('light');
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [compactMode, setCompactMode] = useState(false);

  // Regional settings
  const [language, setLanguage] = useState('en');
  const [timezone, setTimezone] = useState('Asia/Kolkata');
  const [dateFormat, setDateFormat] = useState('DD/MM/YYYY');
  const [currency, setCurrency] = useState('INR');

  // Notification settings
  const [notifications, setNotifications] = useState<NotificationSetting[]>([
    {
      id: 'indent-approval',
      title: 'Indent Approvals',
      description: 'When an indent is submitted for your approval',
      email: true,
      push: true,
      inApp: true,
    },
    {
      id: 'po-created',
      title: 'PO Created',
      description: 'When a purchase order is generated from your indent',
      email: true,
      push: false,
      inApp: true,
    },
    {
      id: 'grn-received',
      title: 'GRN Received',
      description: 'When goods are received against your PO',
      email: true,
      push: true,
      inApp: true,
    },
    {
      id: 'low-stock',
      title: 'Low Stock Alerts',
      description: 'When inventory falls below reorder level',
      email: true,
      push: true,
      inApp: true,
    },
    {
      id: 'system-updates',
      title: 'System Updates',
      description: 'Important system announcements and updates',
      email: false,
      push: false,
      inApp: true,
    },
  ]);

  // Security settings
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(false);
  const [sessionTimeout, setSessionTimeout] = useState(30);

  const handleSave = async () => {
    setSaving(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    setSaving(false);
    setSuccess('Settings saved successfully!');
    setTimeout(() => setSuccess(null), 3000);
  };

  const updateNotification = (id: string, field: 'email' | 'push' | 'inApp', value: boolean) => {
    setNotifications(prev =>
      prev.map(n => (n.id === id ? { ...n, [field]: value } : n))
    );
  };

  return (
    <div>
      <PageHeader
        title="Settings"
        subtitle="Manage your application preferences"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Settings' },
        ]}
        actions={
          <Button variant="primary" onClick={handleSave} disabled={saving}>
            {saving ? (
              <>
                <Spinner as="span" animation="border" size="sm" className="me-2" />
                Saving...
              </>
            ) : (
              <>
                <FaSave className="me-2" /> Save Changes
              </>
            )}
          </Button>
        }
      />

      {success && (
        <Alert variant="success" dismissible onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      <Tab.Container defaultActiveKey="appearance">
        <Row>
          <Col lg={3} className="mb-4">
            <Card>
              <Card.Body className="p-0">
                <Nav variant="pills" className="flex-column">
                  <Nav.Item>
                    <Nav.Link eventKey="appearance" className="d-flex align-items-center py-3 px-4">
                      <FaPalette className="me-3" /> Appearance
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="regional" className="d-flex align-items-center py-3 px-4">
                      <FaGlobe className="me-3" /> Regional
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="notifications" className="d-flex align-items-center py-3 px-4">
                      <FaBell className="me-3" /> Notifications
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link eventKey="security" className="d-flex align-items-center py-3 px-4">
                      <FaShieldAlt className="me-3" /> Security
                    </Nav.Link>
                  </Nav.Item>
                </Nav>
              </Card.Body>
            </Card>
          </Col>

          <Col lg={9}>
            <Tab.Content>
              {/* Appearance Tab */}
              <Tab.Pane eventKey="appearance">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0">
                      <FaPalette className="me-2" /> Appearance Settings
                    </h5>
                  </Card.Header>
                  <Card.Body>
                    <h6 className="mb-3">Theme</h6>
                    <Row className="mb-4">
                      <Col sm={4}>
                        <Card
                          className={`text-center cursor-pointer ${theme === 'light' ? 'border-primary' : ''}`}
                          onClick={() => setTheme('light')}
                          role="button"
                        >
                          <Card.Body>
                            <FaSun size={32} className="text-warning mb-2" />
                            <div className="fw-medium">Light</div>
                          </Card.Body>
                        </Card>
                      </Col>
                      <Col sm={4}>
                        <Card
                          className={`text-center cursor-pointer ${theme === 'dark' ? 'border-primary' : ''}`}
                          onClick={() => setTheme('dark')}
                          role="button"
                        >
                          <Card.Body>
                            <FaMoon size={32} className="text-primary mb-2" />
                            <div className="fw-medium">Dark</div>
                          </Card.Body>
                        </Card>
                      </Col>
                      <Col sm={4}>
                        <Card
                          className={`text-center cursor-pointer ${theme === 'system' ? 'border-primary' : ''}`}
                          onClick={() => setTheme('system')}
                          role="button"
                        >
                          <Card.Body>
                            <FaDesktop size={32} className="text-secondary mb-2" />
                            <div className="fw-medium">System</div>
                          </Card.Body>
                        </Card>
                      </Col>
                    </Row>

                    <hr />

                    <h6 className="mb-3">Layout</h6>
                    <Form.Check
                      type="switch"
                      id="sidebarCollapsed"
                      label="Collapse sidebar by default"
                      checked={sidebarCollapsed}
                      onChange={(e) => setSidebarCollapsed(e.target.checked)}
                      className="mb-2"
                    />
                    <Form.Check
                      type="switch"
                      id="compactMode"
                      label="Compact mode (reduced spacing)"
                      checked={compactMode}
                      onChange={(e) => setCompactMode(e.target.checked)}
                    />
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* Regional Tab */}
              <Tab.Pane eventKey="regional">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0">
                      <FaGlobe className="me-2" /> Regional Settings
                    </h5>
                  </Card.Header>
                  <Card.Body>
                    <Row>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Language</Form.Label>
                          <Form.Select
                            value={language}
                            onChange={(e) => setLanguage(e.target.value)}
                          >
                            <option value="en">English</option>
                            <option value="hi">Hindi</option>
                            <option value="ta">Tamil</option>
                            <option value="te">Telugu</option>
                            <option value="mr">Marathi</option>
                          </Form.Select>
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Timezone</Form.Label>
                          <Form.Select
                            value={timezone}
                            onChange={(e) => setTimezone(e.target.value)}
                          >
                            <option value="Asia/Kolkata">India Standard Time (IST)</option>
                            <option value="UTC">UTC</option>
                            <option value="America/New_York">Eastern Time (US)</option>
                            <option value="Europe/London">London (GMT)</option>
                          </Form.Select>
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Date Format</Form.Label>
                          <Form.Select
                            value={dateFormat}
                            onChange={(e) => setDateFormat(e.target.value)}
                          >
                            <option value="DD/MM/YYYY">DD/MM/YYYY (31/12/2024)</option>
                            <option value="MM/DD/YYYY">MM/DD/YYYY (12/31/2024)</option>
                            <option value="YYYY-MM-DD">YYYY-MM-DD (2024-12-31)</option>
                            <option value="DD-MMM-YYYY">DD-MMM-YYYY (31-Dec-2024)</option>
                          </Form.Select>
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Currency</Form.Label>
                          <Form.Select
                            value={currency}
                            onChange={(e) => setCurrency(e.target.value)}
                          >
                            <option value="INR">Indian Rupee (₹)</option>
                            <option value="USD">US Dollar ($)</option>
                            <option value="EUR">Euro (€)</option>
                            <option value="GBP">British Pound (£)</option>
                          </Form.Select>
                        </Form.Group>
                      </Col>
                    </Row>
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* Notifications Tab */}
              <Tab.Pane eventKey="notifications">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0">
                      <FaBell className="me-2" /> Notification Preferences
                    </h5>
                  </Card.Header>
                  <Card.Body>
                    <div className="table-responsive">
                      <Table className="align-middle">
                        <thead>
                          <tr>
                            <th>Notification Type</th>
                            <th className="text-center">
                              <FaEnvelope className="me-1" /> Email
                            </th>
                            <th className="text-center">
                              <FaMobile className="me-1" /> Push
                            </th>
                            <th className="text-center">
                              <FaDesktop className="me-1" /> In-App
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          {notifications.map((notification) => (
                            <tr key={notification.id}>
                              <td>
                                <div className="fw-medium">{notification.title}</div>
                                <small className="text-muted">{notification.description}</small>
                              </td>
                              <td className="text-center">
                                <Form.Check
                                  type="switch"
                                  checked={notification.email}
                                  onChange={(e) =>
                                    updateNotification(notification.id, 'email', e.target.checked)
                                  }
                                />
                              </td>
                              <td className="text-center">
                                <Form.Check
                                  type="switch"
                                  checked={notification.push}
                                  onChange={(e) =>
                                    updateNotification(notification.id, 'push', e.target.checked)
                                  }
                                />
                              </td>
                              <td className="text-center">
                                <Form.Check
                                  type="switch"
                                  checked={notification.inApp}
                                  onChange={(e) =>
                                    updateNotification(notification.id, 'inApp', e.target.checked)
                                  }
                                />
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </Table>
                    </div>
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* Security Tab */}
              <Tab.Pane eventKey="security">
                <Card className="mb-4">
                  <Card.Header>
                    <h5 className="mb-0">
                      <FaShieldAlt className="me-2" /> Security Settings
                    </h5>
                  </Card.Header>
                  <Card.Body>
                    <div className="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom">
                      <div>
                        <h6 className="mb-1">Two-Factor Authentication</h6>
                        <small className="text-muted">
                          Add an extra layer of security to your account
                        </small>
                      </div>
                      <div className="d-flex align-items-center gap-2">
                        <Badge bg={twoFactorEnabled ? 'success' : 'secondary'}>
                          {twoFactorEnabled ? 'Enabled' : 'Disabled'}
                        </Badge>
                        <Form.Check
                          type="switch"
                          checked={twoFactorEnabled}
                          onChange={(e) => setTwoFactorEnabled(e.target.checked)}
                        />
                      </div>
                    </div>

                    <Form.Group className="mb-3">
                      <Form.Label>Session Timeout (minutes)</Form.Label>
                      <Form.Select
                        value={sessionTimeout}
                        onChange={(e) => setSessionTimeout(Number(e.target.value))}
                      >
                        <option value={15}>15 minutes</option>
                        <option value={30}>30 minutes</option>
                        <option value={60}>1 hour</option>
                        <option value={120}>2 hours</option>
                        <option value={480}>8 hours</option>
                      </Form.Select>
                      <Form.Text className="text-muted">
                        Automatically log out after this period of inactivity
                      </Form.Text>
                    </Form.Group>
                  </Card.Body>
                </Card>

                <Card>
                  <Card.Header>
                    <h5 className="mb-0">Active Sessions</h5>
                  </Card.Header>
                  <Card.Body>
                    <ListGroup variant="flush">
                      <ListGroup.Item className="d-flex justify-content-between align-items-center">
                        <div>
                          <div className="d-flex align-items-center">
                            <FaDesktop className="me-3 text-muted" size={20} />
                            <div>
                              <div className="fw-medium">Windows PC - Chrome</div>
                              <small className="text-muted">Mumbai, India • Current session</small>
                            </div>
                          </div>
                        </div>
                        <Badge bg="success">Active</Badge>
                      </ListGroup.Item>
                      <ListGroup.Item className="d-flex justify-content-between align-items-center">
                        <div>
                          <div className="d-flex align-items-center">
                            <FaMobile className="me-3 text-muted" size={20} />
                            <div>
                              <div className="fw-medium">iPhone - Safari</div>
                              <small className="text-muted">Mumbai, India • 2 hours ago</small>
                            </div>
                          </div>
                        </div>
                        <Button variant="outline-danger" size="sm">
                          Revoke
                        </Button>
                      </ListGroup.Item>
                    </ListGroup>
                  </Card.Body>
                </Card>
              </Tab.Pane>
            </Tab.Content>
          </Col>
        </Row>
      </Tab.Container>
    </div>
  );
};

export default SettingsPage;
