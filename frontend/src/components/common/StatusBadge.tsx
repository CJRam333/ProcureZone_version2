import React from 'react';
import { Badge } from 'react-bootstrap';
import { INDENT_STATUS_COLORS } from '../../constants/indentStatus';

type BadgeVariant = 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';

interface StatusBadgeProps {
  status: string | number;
  statusMap?: Record<string | number, { label: string; className: string; variant?: BadgeVariant }>;
  className?: string;
  variant?: BadgeVariant; // Direct variant prop for Bootstrap badge
  label?: string; // Optional label override
}

// Default status mappings with Bootstrap variants
const defaultStatusMap: Record<string | number, { label: string; className: string; variant: BadgeVariant }> = {
  // Generic statuses (lowercase)
  draft: { label: 'Draft', className: 'draft', variant: 'secondary' },
  pending: { label: 'Pending', className: 'pending', variant: 'warning' },
  pending_approval: { label: 'Pending Approval', className: 'pending', variant: 'warning' },
  approved: { label: 'Approved', className: 'approved', variant: 'success' },
  rejected: { label: 'Rejected', className: 'rejected', variant: 'danger' },
  completed: { label: 'Completed', className: 'completed', variant: 'success' },
  cancelled: { label: 'Cancelled', className: 'rejected', variant: 'dark' },
  active: { label: 'Active', className: 'approved', variant: 'success' },
  inactive: { label: 'Inactive', className: 'draft', variant: 'secondary' },
  closed: { label: 'Closed', className: 'completed', variant: 'secondary' },

  // Uppercase versions (for API responses)
  DRAFT: { label: 'Draft', className: 'draft', variant: 'secondary' },
  PENDING: { label: 'Pending', className: 'pending', variant: 'warning' },
  PENDING_APPROVAL: { label: 'Pending Approval', className: 'pending', variant: 'warning' },
  APPROVED: { label: 'Approved', className: 'approved', variant: 'success' },
  REJECTED: { label: 'Rejected', className: 'rejected', variant: 'danger' },
  COMPLETED: { label: 'Completed', className: 'completed', variant: 'success' },
  CANCELLED: { label: 'Cancelled', className: 'rejected', variant: 'dark' },
  CLOSED: { label: 'Closed', className: 'completed', variant: 'secondary' },

  // PO statuses
  SENT_TO_VENDOR: { label: 'Sent to Vendor', className: 'pending', variant: 'info' },
  ACKNOWLEDGED: { label: 'Acknowledged', className: 'approved', variant: 'info' },
  PARTIALLY_RECEIVED: { label: 'Partially Received', className: 'pending', variant: 'warning' },
  FULLY_RECEIVED: { label: 'Fully Received', className: 'completed', variant: 'success' },

  // GRN statuses
  PENDING_QC: { label: 'Pending QC', className: 'pending', variant: 'warning' },
  QC_APPROVED: { label: 'QC Approved', className: 'approved', variant: 'info' },
  QC_REJECTED: { label: 'QC Rejected', className: 'rejected', variant: 'danger' },

  // Issue Note statuses
  ISSUED: { label: 'Issued', className: 'completed', variant: 'success' },
  PARTIALLY_ISSUED: { label: 'Partially Issued', className: 'pending', variant: 'info' },
  RETURNED: { label: 'Returned', className: 'pending', variant: 'info' },

};

const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  statusMap = defaultStatusMap,
  className = '',
  variant,
  label,
}) => {
  // Look up status config
  const statusConfig = statusMap[status] || statusMap[String(status).toLowerCase()] || statusMap[String(status).toUpperCase()] || {
    label: String(status).replace(/_/g, ' '),
    className: 'draft',
    variant: 'secondary' as BadgeVariant,
  };

  // Use provided variant/label or fall back to INDENT_STATUS_COLORS (for displayStatus strings) then statusConfig
  const indentColor = typeof status === 'string' ? INDENT_STATUS_COLORS[status] : undefined;
  const badgeVariant = variant || (indentColor as BadgeVariant | undefined) || statusConfig.variant || 'secondary';
  const badgeLabel = label || statusConfig.label || String(status);

  return (
    <Badge
      bg={badgeVariant}
      className={`status-badge ${statusConfig.className} ${className}`}
    >
      {badgeLabel}
    </Badge>
  );
};

export default StatusBadge;
