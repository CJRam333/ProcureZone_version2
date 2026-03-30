import React from 'react';
import { Card, Badge, Row, Col, ListGroup } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FaInbox, FaArrowRight } from 'react-icons/fa';
import { getWidgetsForRoles, DashboardStats } from './roleWidgetConfig';

interface WorkflowWidgetProps {
  title: string;
  count: number;
  icon: React.ReactNode;
  color: string;
  description?: string;
  linkTo: string;
  linkText?: string;
  items?: Array<{
    id: number | string;
    title: string;
    subtitle?: string;
    status?: string;
    date?: string;
  }>;
}

export const WorkflowWidget: React.FC<WorkflowWidgetProps> = ({
  title,
  count,
  icon,
  color,
  description,
  linkTo,
  linkText = 'View All',
  items = [],
}) => (
  <Card className="h-100 border-0 shadow-sm">
    <Card.Header className={`bg-${color} text-white border-0 py-3`}>
      <div className="d-flex align-items-center justify-content-between">
        <div className="d-flex align-items-center">
          <span className="me-2">{icon}</span>
          <span className="fw-semibold">{title}</span>
        </div>
        <Badge bg="light" text="dark" className="rounded-pill px-3 py-2">
          {count}
        </Badge>
      </div>
      {description && <small className="opacity-75 d-block mt-1">{description}</small>}
    </Card.Header>
    <Card.Body className="p-0">
      {items.length > 0 ? (
        <ListGroup variant="flush">
          {items.slice(0, 4).map((item) => (
            <ListGroup.Item
              key={item.id}
              className="d-flex justify-content-between align-items-center py-2 px-3"
            >
              <div className="text-truncate me-2">
                <div className="fw-medium text-truncate">{item.title}</div>
                {item.subtitle && (
                  <small className="text-muted">{item.subtitle}</small>
                )}
              </div>
              {item.status && (
                <Badge bg="secondary" className="text-nowrap">
                  {item.status}
                </Badge>
              )}
            </ListGroup.Item>
          ))}
        </ListGroup>
      ) : (
        <div className="text-center text-muted py-4">
          <FaInbox size={24} className="mb-2 opacity-50" />
          <div className="small">No pending items</div>
        </div>
      )}
    </Card.Body>
    <Card.Footer className="bg-transparent border-0 text-center py-2">
      <Link
        to={linkTo}
        className="text-decoration-none small d-inline-flex align-items-center gap-1"
      >
        {linkText} <FaArrowRight size={10} />
      </Link>
    </Card.Footer>
  </Card>
);

// Compact Action Card for Quick Actions
interface ActionCardProps {
  title: string;
  count: number;
  icon: React.ReactNode;
  color: string;
  linkTo: string;
}

export const ActionCard: React.FC<ActionCardProps> = ({
  title,
  count,
  icon,
  color,
  linkTo,
}) => (
  <Link to={linkTo} className="text-decoration-none">
    <Card className={`border-0 shadow-sm h-100 border-start border-4 border-${color}`}>
      <Card.Body className="py-3">
        <div className="d-flex align-items-center">
          <div className={`text-${color} me-3`}>{icon}</div>
          <div>
            <div className="h4 mb-0 fw-bold">{count}</div>
            <small className="text-muted">{title}</small>
          </div>
        </div>
      </Card.Body>
    </Card>
  </Link>
);

// Dashboard Summary Row component
interface DashboardSummaryProps {
  stats: DashboardStats | null | undefined;
  userRoles: string[];
}

export const DashboardSummary: React.FC<DashboardSummaryProps> = ({
  stats,
  userRoles,
}) => {
  const widgets = getWidgetsForRoles(userRoles);

  if (widgets.length === 0) return null;

  return (
    <Row className="g-3 mb-4">
      {widgets.slice(0, 4).map((widget) => (
        <Col key={widget.id} sm={6} lg={3}>
          <WorkflowWidget
            title={widget.title}
            count={widget.getCount(stats)}
            icon={widget.icon}
            color={widget.color}
            description={widget.description}
            linkTo={widget.linkTo}
          />
        </Col>
      ))}
    </Row>
  );
};

// Re-export types only (configs are exported from roleWidgetConfig.ts)
export type { DashboardStats };

export default WorkflowWidget;
