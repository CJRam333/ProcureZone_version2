// Single source of truth for indent display status labels and colors.
// Labels derived from three-column compound logic on the backend (displayStatus field).
// Do not add status maps anywhere else in the frontend.

export const INDENT_STATUS_COLORS: Record<string, string> = {
  'Pending':              'warning',
  'RM Approved':          'info',
  'RM Rejected':          'danger',
  'Dept. Head Approved':  'primary',
  'Dept. Head Rejected':  'danger',
  'Quotations Collected': 'info',
  'Negotiation Done':     'info',
  'PO Released':          'success',
  'Hold':                 'secondary',
  'Cash Buy':             'secondary',
  'Goods Receipt':        'success',
  'Goods Issued':         'success',
  'In Progress':          'info',
};
