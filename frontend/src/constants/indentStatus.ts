// Single source of truth for indent display status labels and colors.
// Labels derived from displayStatus field on the backend (both regular and plant indents).
// Do not add status maps anywhere else in the frontend.
//
// Every status has a UNIQUE, visually distinguishable color. Values are either
// Bootstrap variants (primary, secondary, success, danger, warning, info, light, dark)
// or custom class suffixes (purple, orange, teal, cyan, dark-green, maroon, indigo,
// olive, pink, brown) defined as `.badge.bg-*` rules in styles/main.scss.
// Both kinds work unchanged through `<Badge bg={color}>` (renders class `bg-{color}`).

export const INDENT_STATUS_COLORS: Record<string, string> = {
  // Regular indent statuses (three-column compound logic)
  'Pending':              'warning',      // amber
  'RM Approved':          'info',         // light blue
  'RM Rejected':          'danger',       // red
  'Dept. Head Approved':  'primary',      // blue
  'Dept. Head Rejected':  'maroon',       // dark red — distinct from RM Rejected
  'In Progress':          'light',        // pale grey (dark text) — fallback, should rarely appear

  // Plant indent statuses (indent_approved_status direct mapping)
  'DEO Approved':         'indigo',       // deep violet-blue
  'Final Approved':       'olive',        // yellow-green
  'Rejected':             'danger',       // red (plant-only label, never co-occurs with RM Rejected)
  'On Hold':              'brown',        // warm brown — distinct from shared Hold
  'Inactive':             'dark',         // near-black

  // Shared statuses (used by both regular and plant indents)
  'Quotations Collected': 'secondary',    // grey
  'Negotiation Done':     'purple',       // violet
  'PO Released':          'success',      // green
  'Stores Rejected':      'pink',         // magenta — distinct from red rejections
  'Hold':                 'orange',       // orange — distinct from warning amber
  'Cash Buy':             'teal',         // blue-green
  'Goods Receipt':        'cyan',         // dark cyan — distinct from info light blue
  'Goods Issued':         'dark-green',   // deep green — distinct from success
  'Completed':            'success',      // green (never appears alongside PO Released)
};
