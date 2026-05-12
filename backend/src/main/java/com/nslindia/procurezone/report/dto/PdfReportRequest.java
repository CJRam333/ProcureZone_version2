package com.nslindia.procurezone.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for PDF report generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfReportRequest {

    /** Report title displayed at top */
    private String title;

    /** Subtitle or description */
    private String subtitle;

    /** Company name for header */
    private String companyName;

    /** Report type for formatting decisions */
    private ReportType reportType;

    /** Column headers for table */
    private List<String> headers;

    /** Column widths (relative proportions) */
    private float[] columnWidths;

    /** Data rows - each map contains header -> value pairs */
    private List<Map<String, Object>> data;

    /** Summary section data (for totals, etc.) */
    private Map<String, Object> summary;

    /** Include page numbers */
    @Builder.Default
    private boolean includePageNumbers = true;

    /** Include generation timestamp */
    @Builder.Default
    private boolean includeTimestamp = true;

    /** Include company logo */
    @Builder.Default
    private boolean includeLogo = true;

    /** Page orientation */
    @Builder.Default
    private PageOrientation orientation = PageOrientation.PORTRAIT;

    /** Page size */
    @Builder.Default
    private PageSize pageSize = PageSize.A4;

    public enum ReportType {
        INDENT,
        PURCHASE_ORDER,
        GOODS_RECEIPT,
        ISSUE_NOTE,
        INVENTORY_STOCK,
        VENDOR_REPORT,
        MATERIAL_REPORT,
        CUSTOM
    }

    public enum PageOrientation {
        PORTRAIT,
        LANDSCAPE
    }

    public enum PageSize {
        A4,
        A3,
        LETTER,
        LEGAL
    }
}
