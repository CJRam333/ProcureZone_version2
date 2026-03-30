import React from 'react';
import { Link } from 'react-router-dom';
import { Breadcrumb as BSBreadcrumb } from 'react-bootstrap';

export interface BreadcrumbItem {
  label: string;
  path?: string;
}

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  breadcrumbs?: BreadcrumbItem[];
  actions?: React.ReactNode;
}

const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  subtitle,
  breadcrumbs,
  actions,
}) => {
  return (
    <div className="page-header d-flex flex-wrap align-items-start justify-content-between gap-3">
      <div>
        {breadcrumbs && breadcrumbs.length > 0 && (
          <BSBreadcrumb className="mb-2">
            {breadcrumbs.map((item, index) => (
              <BSBreadcrumb.Item
                key={index}
                linkAs={item.path ? Link : 'span'}
                linkProps={item.path ? { to: item.path } : undefined}
                active={index === breadcrumbs.length - 1}
              >
                {item.label}
              </BSBreadcrumb.Item>
            ))}
          </BSBreadcrumb>
        )}
        <h1>{title}</h1>
        {subtitle && <p className="text-muted mb-0">{subtitle}</p>}
      </div>
      {actions && <div className="d-flex gap-2">{actions}</div>}
    </div>
  );
};

export default PageHeader;
