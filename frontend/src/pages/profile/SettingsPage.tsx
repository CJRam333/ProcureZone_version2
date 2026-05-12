import React, { useState } from 'react';
import {
  Card, Row, Col, Form, Button, Badge, Alert, Spinner,
  ListGroup, Nav, Tab, Table,
} from 'react-bootstrap';
import {
  FaBell, FaPalette, FaGlobe, FaShieldAlt, FaSave,
  FaMoon, FaSun, FaEnvelope, FaMobile, FaDesktop,
} from 'react-icons/fa';
import { PageHeader } from '../../components/common';
import { useSettings, NotificationSetting } from '../../contexts/SettingsContext';
import { toast } from 'react-toastify';

const SettingsPage: React.FC = () => {
  const { settings, saveAllSettings } = useSettings();
  const [saving, setSaving] = useState(false);

  // Local draft state — only committed to context when Save is clicked
  const [theme, setTheme] = useState(settings.theme);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(settings.sidebarCollapsed);
  const [compactMode, setCompactMode] = useState(settings.compactMode);
  const [language, setLanguage] = useState(settings.language);
  const [timezone, setTimezone] = useState(settings.timezone);
  const [dateFormat, setDateFormat] = useState(settings.dateFormat);
  const [currency, setCurrency] = useState(settings.currency);
  const [notifications, setNotifications] = useState<NotificationSetting[]>(settings.notifications);
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(settings.twoFactorEnabled);
  const [sessionTimeout, setSessionTimeout] = useState(settings.sessionTimeout);

  const handleSave = async () => {
    setSaving(true);
    try {
      await saveAllSettings({
        theme,
        sidebarCollapsed,
        compactMode,
        language,
        timezone,
        dateFormat,
        currency,
        notifications,
        twoFactorEnabled,
        sessionTimeout,
      });
      toast.success('Settings saved successfully!');
    } finally {
      setSaving(false);
    }
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
              <><Spinner as="span" animation="border" size="sm" className="me-2" />Saving...</>
            ) : (
              <><FaSave className="me-2" />Save Changes</>
            )}
          </Button>
        }
      />

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
              {/* ── Appearance ── */}
              <Tab.Pane eventKey="appearance">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0"><FaPalette className="me-2" />Appearance Settings</h5>
                  </Card.Header>
                  <Card.Body>
                    <h6 className="mb-3">Theme</h6>
                    <Row className="mb-4">
                      {(['light', 'dark', 'system'] as const).map((t) => (
                        <Col sm={4} key={t}>
                          <Card
                            className={`text-center h-100 ${theme === t ? 'border-primary border-2' : ''}`}
                            onClick={() => setTheme(t)}
                            role="button"
                            style={{ cursor: 'pointer', transition: 'border-color 0.2s' }}
                          >
                            <Card.Body className="py-3">
                              {t === 'light' && <FaSun size={32} className="text-warning mb-2" />}
                              {t === 'dark' && <FaMoon size={32} className="text-primary mb-2" />}
                              {t === 'system' && <FaDesktop size={32} className="text-secondary mb-2" />}
                              <div className="fw-medium text-capitalize">{t}</div>
                              {theme === t && (
                                <Badge bg="primary" className="mt-1">Selected</Badge>
                              )}
                            </Card.Body>
                          </Card>
                        </Col>
                      ))}
                    </Row>

                    <hr />

                    <h6 className="mb-3">Layout</h6>
                    <Form.Check
                      type="switch"
                      id="sidebarCollapsed"
                      label="Collapse sidebar by default"
                      checked={sidebarCollapsed}
                      onChange={(e) => setSidebarCollapsed(e.target.checked)}
                      className="mb-3"
                    />
                    <Form.Check
                      type="switch"
                      id="compactMode"
                      label="Compact mode (reduced spacing)"
                      checked={compactMode}
                      onChange={(e) => setCompactMode(e.target.checked)}
                    />
                    <Form.Text className="text-muted d-block mt-2">
                      Changes take effect immediately after saving.
                    </Form.Text>
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* ── Regional ── */}
              <Tab.Pane eventKey="regional">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0"><FaGlobe className="me-2" />Regional Settings</h5>
                  </Card.Header>
                  <Card.Body>
                    <Row>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Language</Form.Label>
                          <Form.Select value={language} onChange={(e) => setLanguage(e.target.value)}>
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
                          <Form.Select value={timezone} onChange={(e) => setTimezone(e.target.value)}>
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
                          <Form.Select value={dateFormat} onChange={(e) => setDateFormat(e.target.value)}>
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
                          <Form.Select value={currency} onChange={(e) => setCurrency(e.target.value)}>
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

              {/* ── Notifications ── */}
              <Tab.Pane eventKey="notifications">
                <Card>
                  <Card.Header>
                    <h5 className="mb-0"><FaBell className="me-2" />Notification Preferences</h5>
                  </Card.Header>
                  <Card.Body>
                    <div className="table-responsive">
                      <Table className="align-middle">
                        <thead>
                          <tr>
                            <th>Notification Type</th>
                            <th className="text-center"><FaEnvelope className="me-1" />Email</th>
                            <th className="text-center"><FaMobile className="me-1" />Push</th>
                            <th className="text-center"><FaDesktop className="me-1" />In-App</th>
                          </tr>
                        </thead>
                        <tbody>
                          {notifications.map((n) => (
                            <tr key={n.id}>
                              <td>
                                <div className="fw-medium">{n.title}</div>
                                <small className="text-muted">{n.description}</small>
                              </td>
                              <td className="text-center">
                                <Form.Check type="switch" checked={n.email}
                                  onChange={(e) => updateNotification(n.id, 'email', e.target.checked)} />
                              </td>
                              <td className="text-center">
                                <Form.Check type="switch" checked={n.push}
                                  onChange={(e) => updateNotification(n.id, 'push', e.target.checked)} />
                              </td>
                              <td className="text-center">
                                <Form.Check type="switch" checked={n.inApp}
                                  onChange={(e) => updateNotification(n.id, 'inApp', e.target.checked)} />
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </Table>
                    </div>
                  </Card.Body>
                </Card>
              </Tab.Pane>

              {/* ── Security ── */}
              <Tab.Pane eventKey="security">
                <Card className="mb-4">
                  <Card.Header>
                    <h5 className="mb-0"><FaShieldAlt className="me-2" />Security Settings</h5>
                  </Card.Header>
                  <Card.Body>
                    <div className="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom">
                      <div>
                        <h6 className="mb-1">Two-Factor Authentication</h6>
                        <small className="text-muted">Add an extra layer of security</small>
                      </div>
                      <div className="d-flex align-items-center gap-2">
                        <Badge bg={twoFactorEnabled ? 'success' : 'secondary'}>
                          {twoFactorEnabled ? 'Enabled' : 'Disabled'}
                        </Badge>
                        <Form.Check type="switch" checked={twoFactorEnabled}
                          onChange={(e) => setTwoFactorEnabled(e.target.checked)} />
                      </div>
                    </div>

                    <Form.Group className="mb-3">
                      <Form.Label>Session Timeout (minutes)</Form.Label>
                      <Form.Select value={sessionTimeout}
                        onChange={(e) => setSessionTimeout(Number(e.target.value))}>
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
                  <Card.Header><h5 className="mb-0">Active Sessions</h5></Card.Header>
                  <Card.Body>
                    <ListGroup variant="flush">
                      <ListGroup.Item className="d-flex justify-content-between align-items-center">
                        <div className="d-flex align-items-center">
                          <FaDesktop className="me-3 text-muted" size={20} />
                          <div>
                            <div className="fw-medium">Windows PC - Chrome</div>
                            <small className="text-muted">Current session</small>
                          </div>
                        </div>
                        <Badge bg="success">Active</Badge>
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
