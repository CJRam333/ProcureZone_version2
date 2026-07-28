import React from 'react';
import { OverlayTrigger, Popover } from 'react-bootstrap';
import { FaListUl } from 'react-icons/fa';

/**
 * Compact line-item shape shared by the indent & issue-note list endpoints
 * (IndentListResponse.ItemSummary / IssueNoteSummaryResponse.ItemSummary).
 */
export interface ItemsPreviewLine {
  materialName?: string;
  materialDescription?: string;
  companies?: string; // company selected at creation (falls back to all companies for legacy rows)
  uomCode?: string;
  quantity?: number;
}

interface ItemsPreviewProps {
  /** Compact per-line summary supplied by the list endpoint (no per-row detail fetch). */
  items?: ItemsPreviewLine[];
  /** Total item count (falls back to items.length). */
  count?: number;
  /** Unique suffix for the popover id (usually the row id). */
  idKey: string | number;
}

/**
 * Renders the item count followed by a preview icon. Hovering/focusing the icon shows a popup
 * listing every material on the document — material NAME (full, wraps; never truncated), the
 * company/companies stocking it, and quantity + UOM together — one row per line item. Used in the
 * Indent and Issue Note list pages' "Items" column.
 */
const ItemsPreview: React.FC<ItemsPreviewProps> = ({ items, count, idKey }) => {
  const lines = items ?? [];
  const total = count ?? lines.length;

  if (lines.length === 0) {
    return <span className="text-muted">{total || '-'}</span>;
  }

  const popover = (
    // Widened well beyond Bootstrap's 276px default; names WRAP (wordBreak) instead of truncating,
    // so a material name of any length is shown in full.
    <Popover id={`items-preview-${idKey}`} style={{ maxWidth: 440 }}>
      <Popover.Header as="h6" className="py-2">Items ({lines.length})</Popover.Header>
      <Popover.Body className="p-2">
        <div
          className="d-flex flex-column gap-2"
          style={{ maxHeight: 300, overflowY: 'auto', minWidth: 280 }}
        >
          {lines.map((it, idx) => (
            <div key={idx} className="small border-bottom pb-1">
              <div className="d-flex justify-content-between gap-3">
                <span className="fw-medium" style={{ wordBreak: 'break-word' }}>
                  {it.materialName ?? '—'}
                </span>
                <span className="text-nowrap text-muted">
                  {it.quantity ?? '-'}
                  {it.uomCode ? ` ${it.uomCode}` : ''}
                </span>
              </div>
              {it.materialDescription && (
                <div className="text-muted fst-italic" style={{ wordBreak: 'break-word' }}>
                  {it.materialDescription}
                </div>
              )}
              {it.companies && (
                <div className="text-muted" style={{ wordBreak: 'break-word' }}>
                  {it.companies}
                </div>
              )}
            </div>
          ))}
        </div>
      </Popover.Body>
    </Popover>
  );

  return (
    <div className="d-flex align-items-center gap-2">
      <span>{total}</span>
      <OverlayTrigger trigger={['hover', 'focus']} placement="left" overlay={popover}>
        <span
          role="button"
          tabIndex={0}
          className="text-primary d-inline-flex"
          style={{ cursor: 'pointer' }}
          onClick={(e) => e.stopPropagation()}
          aria-label="Preview items"
        >
          <FaListUl />
        </span>
      </OverlayTrigger>
    </div>
  );
};

export default ItemsPreview;
