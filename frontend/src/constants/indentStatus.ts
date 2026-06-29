// Single source of truth for indent display status labels and colors.
// Labels derived from displayStatus field on the backend (both regular and plant indents).
// Do not add status maps anywhere else in the frontend.

export const INDENT_STATUS_COLORS: Record<string, string> = {
  // Regular indent statuses (three-column compound logic)
  'Pending':              'warning',
  'RM Approved':          'info',
  'RM Rejected':          'danger',
  'Dept. Head Approved':  'primary',
  'Dept. Head Rejected':  'danger',
  'In Progress':          'info',

  // Plant indent statuses (indent_approved_status direct mapping)
  'DEO Approved':         'primary',
  'Final Approved':       'success',
  'Rejected':             'danger',
  'On Hold':              'secondary',
  'Inactive':             'secondary',

  // Shared statuses (used by both regular and plant indents)
  'Quotations Collected': 'info',
  'Negotiation Done':     'info',
  'PO Released':          'success',
  'Stores Rejected':      'danger',
  'Hold':                 'secondary',
  'Cash Buy':             'secondary',
  'Goods Receipt':        'success',
  'Goods Issued':         'success',
  'Completed':            'success',
};
