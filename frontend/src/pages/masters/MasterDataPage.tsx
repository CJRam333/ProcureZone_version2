import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Row, Col, Card } from 'react-bootstrap';
import { PageHeader } from '../../components/common';
import {
  FaBuilding,
  FaIndustry,
  FaMapMarkerAlt,
  FaSitemap,
  FaLayerGroup,
  FaUsers,
  FaUserCog,
  FaUserTag,
  FaCubes,
  FaHandshake,
  FaRuler,
  FaLeaf,
  FaLink,
  FaProjectDiagram,
} from 'react-icons/fa';

interface MasterCard {
  title: string;
  description: string;
  icon: React.ReactNode;
  path: string;
  color: string;
}

interface MasterGroup {
  title: string;
  description: string;
  cards: MasterCard[];
}

const masterGroups: MasterGroup[] = [
  {
    title: 'Organization',
    description: 'Manage your organizational structure',
    cards: [
      {
        title: 'Companies',
        description: 'Company profiles, codes, and status management',
        icon: <FaBuilding />,
        path: '/masters/companies',
        color: '#2563eb',
      },
      {
        title: 'Plants',
        description: 'Manufacturing and processing plant records',
        icon: <FaIndustry />,
        path: '/masters/plants',
        color: '#7c3aed',
      },
      {
        title: 'Locations',
        description: 'Warehouses, offices, and storage locations',
        icon: <FaMapMarkerAlt />,
        path: '/masters/locations',
        color: '#0891b2',
      },
      {
        title: 'Departments',
        description: 'Department hierarchy and configuration',
        icon: <FaSitemap />,
        path: '/masters/departments',
        color: '#0d9488',
      },
      {
        title: 'Sections',
        description: 'Sub-department sections and divisions',
        icon: <FaLayerGroup />,
        path: '/masters/sections',
        color: '#6366f1',
      },
    ],
  },
  {
    title: 'People',
    description: 'Manage employees, users, and access roles',
    cards: [
      {
        title: 'Employees',
        description: 'Employee records, departments, and designations',
        icon: <FaUsers />,
        path: '/masters/employees',
        color: '#ea580c',
      },
      {
        title: 'Users',
        description: 'Login accounts, passwords, and access control',
        icon: <FaUserCog />,
        path: '/masters/users',
        color: '#d946ef',
      },
      {
        title: 'Roles',
        description: 'Role definitions and permission management',
        icon: <FaUserTag />,
        path: '/masters/roles',
        color: '#e11d48',
      },
    ],
  },
  {
    title: 'Catalog',
    description: 'Materials, vendors, and measurement units',
    cards: [
      {
        title: 'Materials',
        description: 'Material catalog, categories, and specifications',
        icon: <FaCubes />,
        path: '/masters/materials',
        color: '#ca8a04',
      },
      {
        title: 'Vendors',
        description: 'Supplier profiles, contacts, and ratings',
        icon: <FaHandshake />,
        path: '/masters/vendors',
        color: '#16a34a',
      },
      {
        title: 'Unit of Measure',
        description: 'Measurement units for materials and inventory',
        icon: <FaRuler />,
        path: '/masters/uom',
        color: '#9333ea',
      },
      {
        title: 'Crop Types',
        description: 'Agricultural crop classification for plants',
        icon: <FaLeaf />,
        path: '/masters/crops',
        color: '#059669',
      },
    ],
  },
  {
    title: 'Relationships & Mappings',
    description: 'Configure relationships between organizational entities',
    cards: [
      {
        title: 'Company-Department',
        description: 'Assign departments to companies',
        icon: <FaSitemap />,
        path: '/masters/companies?tab=department-mapping',
        color: '#2563eb',
      },
      {
        title: 'Company-Location',
        description: 'Assign locations to companies',
        icon: <FaMapMarkerAlt />,
        path: '/masters/companies?tab=location-mapping',
        color: '#0891b2',
      },
      {
        title: 'Employee-Role',
        description: 'Assign roles to employees',
        icon: <FaUserTag />,
        path: '/masters/employees?tab=role-mapping',
        color: '#ea580c',
      },
      {
        title: 'Reporting Hierarchy',
        description: 'Configure employee reporting structure',
        icon: <FaProjectDiagram />,
        path: '/masters/employees?tab=reporting-hierarchy',
        color: '#d946ef',
      },
      {
        title: 'Location-Material',
        description: 'Materials available at locations',
        icon: <FaCubes />,
        path: '/masters/locations?tab=material-mapping',
        color: '#ca8a04',
      },
      {
        title: 'Plant-Material',
        description: 'Material stock levels per plant',
        icon: <FaLink />,
        path: '/masters/plants?tab=material-mapping',
        color: '#7c3aed',
      },
    ],
  },
];

const MasterDataPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div>
      <PageHeader
        title="Master Data"
        subtitle="Manage all master data and configuration"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Master Data' },
        ]}
      />

      {masterGroups.map((group, groupIdx) => (
        <div key={group.title} className={groupIdx > 0 ? 'mt-4' : ''}>
          <div className="mb-3">
            <h5 className="fw-semibold text-dark mb-1">{group.title}</h5>
            <p className="text-muted small mb-0">{group.description}</p>
          </div>

          <Row xs={1} sm={2} md={3} lg={4} className="g-3">
            {group.cards.map((card) => (
              <Col key={card.path}>
                <Card
                  className="h-100 border-0 shadow-sm master-card"
                  style={{ cursor: 'pointer', transition: 'all 0.2s ease' }}
                  onClick={() => navigate(card.path)}
                  onMouseEnter={(e) => {
                    const el = e.currentTarget;
                    el.style.transform = 'translateY(-4px)';
                    el.style.boxShadow = '0 8px 25px rgba(0,0,0,0.12)';
                  }}
                  onMouseLeave={(e) => {
                    const el = e.currentTarget;
                    el.style.transform = 'translateY(0)';
                    el.style.boxShadow = '0 1px 3px rgba(0,0,0,0.1)';
                  }}
                >
                  <Card.Body className="d-flex align-items-start gap-3 p-3">
                    <div
                      className="d-flex align-items-center justify-content-center rounded-3 flex-shrink-0"
                      style={{
                        width: '48px',
                        height: '48px',
                        backgroundColor: `${card.color}15`,
                        color: card.color,
                        fontSize: '1.25rem',
                      }}
                    >
                      {card.icon}
                    </div>
                    <div className="min-w-0">
                      <h6 className="fw-semibold mb-1" style={{ fontSize: '0.95rem' }}>
                        {card.title}
                      </h6>
                      <p className="text-muted mb-0" style={{ fontSize: '0.8rem', lineHeight: 1.4 }}>
                        {card.description}
                      </p>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </div>
      ))}
    </div>
  );
};

export default MasterDataPage;
