import React from 'react';
import { OverlayTrigger, Popover } from 'react-bootstrap';
import { FaListUl } from 'react-icons/fa';

/**
 * Compact line-item shape shared by the indent & issue-note list endpoints
 * (IndentListResponse.ItemSummary / IssueNoteSummaryResponse.ItemSummary).
 */
export interface ItemsPreviewLine {
  materialCode?: string;
  materialName?: string;
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
 * listing every material on the document (code — name, quantity + UOM per line). Used in the
 * Indent and Issue Note list pages' "Items" column.
 */
const ItemsPreview: React.FC<ItemsPreviewProps> = ({ items, count, idKey }) => {
  const lines = items ?? [];
  const total = count ?? lines.length;

  if (lines.length === 0) {
    return <span className="text-muted">{total || '-'}</span>;
  }

  const popover = (
    <Popover id={`items-preview-${idKey}`} style={{ maxWidth: 360 }}>
      <Popover.Header as="h6" className="py-2">Items ({lines.length})</Popover.Header>
      <Popover.Body className="p-2">
        <div className="d-flex flex-column gap-1" style={{ maxHeight: 260, overflowY: 'auto' }}>
          {lines.map((it, idx) => (
            <div
              key={idx}
              className="small d-flex justify-content-between gap-3 border-bottom pb-1"
            >
              <span className="text-truncate" style={{ maxWidth: 220 }}>
                {it.materialCode && <span className="fw-medium">{it.materialCode}</span>}
                {it.materialCode && it.materialName ? ' — ' : ''}
                {it.materialName ?? ''}
              </span>
              <span className="text-nowrap text-muted">
                {it.quantity ?? '-'}
                {it.uomCode ? ` ${it.uomCode}` : ''}
              </span>
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
