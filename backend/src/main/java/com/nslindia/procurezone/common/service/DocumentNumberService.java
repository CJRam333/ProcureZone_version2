package com.nslindia.procurezone.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

/**
 * Service for generating document numbers with financial year support
 * 
 * Matches legacy ProcureZone format:
 * - Indent: {CompanyCode}{FinancialYear}{5-digit-sequence} e.g., NSL202500001
 * - PO: {CompanyCode}PO{FinancialYear}{5-digit-sequence} e.g., NSLPO202500001
 * - GRN: {CompanyCode}GRN{FinancialYear}{5-digit-sequence} e.g.,
 * NSLGRN202500001
 * - Issue Note: {CompanyCode}ISS{FinancialYear}{5-digit-sequence} e.g.,
 * NSLISS202500001
 * 
 * Financial Year: April to March (Indian FY)
 * - April 2024 to March 2025 = FY 2024-25 (coded as 2425)
 * - April 2025 to March 2026 = FY 2025-26 (coded as 2526)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentNumberService {

    @PersistenceContext
    private EntityManager entityManager;

    public enum DocumentType {
        INDENT("", "tbl_indent_master", "indent_no", "indent_year"),
        PURCHASE_ORDER("PO", "tbl_purchase_orders", "po_number", "po_year"),
        GOODS_RECEIPT("GRN", "tbl_goods_receipt", "goods_receipt_no", "goods_receipt_date"),
        ISSUE_NOTE("ISS", "tbl_issue_note", "issue_note_no", "issue_note_date"),
        MATERIAL_RECEIPT("MR", "tbl_material_receipts", "mr_number", "mr_year");

        private final String prefix;
        private final String tableName;
        private final String numberColumn;
        private final String yearColumn;

        DocumentType(String prefix, String tableName, String numberColumn, String yearColumn) {
            this.prefix = prefix;
            this.tableName = tableName;
            this.numberColumn = numberColumn;
            this.yearColumn = yearColumn;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getTableName() {
            return tableName;
        }

        public String getNumberColumn() {
            return numberColumn;
        }

        public String getYearColumn() {
            return yearColumn;
        }
    }

    /**
     * Generate document number with company code and financial year
     * Format: {CompanyCode}{Prefix}{FinancialYear}{5-digit-sequence}
     * 
     * @param documentType Type of document (INDENT, PO, GRN, etc.)
     * @param companyCode  Company code (e.g., "NSL", "ABC")
     * @return Generated document number
     */
    @Transactional
    public String generateDocumentNumber(DocumentType documentType, String companyCode) {
        String financialYear = getCurrentFinancialYearCode();
        int nextSequence = getNextSequence(documentType, financialYear);

        String documentNumber = String.format("%s%s%s%05d",
                companyCode.toUpperCase(),
                documentType.getPrefix(),
                financialYear,
                nextSequence);

        log.debug("Generated {} number: {} (FY: {}, Seq: {})",
                documentType.name(), documentNumber, financialYear, nextSequence);

        return documentNumber;
    }

    /**
     * Generate document number with default format (for compatibility)
     * Format: {TYPE}/{YYYY}/{5-digit-sequence}
     * 
     * @param documentType Type of document
     * @return Generated document number
     */
    @Transactional
    public String generateSimpleDocumentNumber(DocumentType documentType) {
        String currentYear = String.valueOf(Year.now().getValue());
        int nextSequence = getNextSequenceByCalendarYear(documentType, currentYear);

        String prefix = switch (documentType) {
            case INDENT -> "IND";
            case PURCHASE_ORDER -> "PO";
            case GOODS_RECEIPT -> "GRN";
            case ISSUE_NOTE -> "ISS";
            case MATERIAL_RECEIPT -> "MR";
        };

        return String.format("%s/%s/%05d", prefix, currentYear, nextSequence);
    }

    /**
     * Get current financial year code
     * Indian Financial Year: April to March
     * 
     * @return Financial year code (e.g., "2425" for FY 2024-25)
     */
    public String getCurrentFinancialYearCode() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        // Financial year starts in April (month 4)
        // If we're in Jan-Mar, we're still in the previous year's FY
        int startYear = (month >= Month.APRIL.getValue()) ? year : year - 1;
        int endYear = startYear + 1;

        // Return as 4-digit code: last 2 digits of start year + last 2 digits of end
        // year
        return String.format("%02d%02d", startYear % 100, endYear % 100);
    }

    /**
     * Get current financial year in full format
     * 
     * @return Financial year string (e.g., "2024-25")
     */
    public String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        int startYear = (month >= Month.APRIL.getValue()) ? year : year - 1;
        int endYear = startYear + 1;

        return String.format("%d-%02d", startYear, endYear % 100);
    }

    /**
     * Get next sequence number for document type and financial year
     */
    private int getNextSequence(DocumentType documentType, String financialYear) {
        // Query to get max sequence for the financial year pattern
        // Pattern: ends with FY code followed by 5 digits
        String sql = String.format(
                "SELECT MAX(CAST(SUBSTRING(%s, -5) AS UNSIGNED)) " +
                        "FROM %s WHERE %s LIKE '%%%s_____'",
                documentType.getNumberColumn(),
                documentType.getTableName(),
                documentType.getNumberColumn(),
                financialYear);

        try {
            Object result = entityManager.createNativeQuery(sql).getSingleResult();
            int maxSequence = result != null ? ((Number) result).intValue() : 0;
            return maxSequence + 1;
        } catch (Exception e) {
            log.warn("Error getting sequence for {}: {}. Starting from 1.", documentType, e.getMessage());
            return 1;
        }
    }

    /**
     * Get next sequence number by calendar year (for simple format)
     */
    private int getNextSequenceByCalendarYear(DocumentType documentType, String year) {
        String sql = String.format(
                "SELECT MAX(CAST(SUBSTRING_INDEX(%s, '/', -1) AS UNSIGNED)) " +
                        "FROM %s WHERE %s IS NOT NULL",
                documentType.getNumberColumn(),
                documentType.getTableName(),
                documentType.getYearColumn());

        try {
            Object result = entityManager.createNativeQuery(sql).getSingleResult();
            int maxSequence = result != null ? ((Number) result).intValue() : 0;
            return maxSequence + 1;
        } catch (Exception e) {
            log.warn("Error getting sequence for {}: {}. Starting from 1.", documentType, e.getMessage());
            return 1;
        }
    }

    /**
     * Parse financial year from document number
     * 
     * @param documentNumber The document number to parse
     * @return Financial year code or null if not found
     */
    public String parseFinancialYear(String documentNumber) {
        if (documentNumber == null || documentNumber.length() < 9) {
            return null;
        }

        // Try to extract 4-digit FY code before the 5-digit sequence
        try {
            // Assuming format: {CompanyCode}{Prefix}{YYYY}{5-digit}
            // The FY code is 4 digits before the last 5 digits
            int length = documentNumber.length();
            return documentNumber.substring(length - 9, length - 5);
        } catch (Exception e) {
            log.debug("Could not parse financial year from: {}", documentNumber);
            return null;
        }
    }

    /**
     * Check if a document number belongs to current financial year
     */
    public boolean isCurrentFinancialYear(String documentNumber) {
        String fyCode = parseFinancialYear(documentNumber);
        return fyCode != null && fyCode.equals(getCurrentFinancialYearCode());
    }

    /**
     * Get financial year date range
     * 
     * @param fyCode Financial year code (e.g., "2425")
     * @return Array with [startDate, endDate]
     */
    public LocalDate[] getFinancialYearDateRange(String fyCode) {
        if (fyCode == null || fyCode.length() != 4) {
            return null;
        }

        try {
            int startYearShort = Integer.parseInt(fyCode.substring(0, 2));
            int endYearShort = Integer.parseInt(fyCode.substring(2, 4));

            // Determine century (assuming 2000s for now)
            int startYear = 2000 + startYearShort;
            int endYear = 2000 + endYearShort;

            LocalDate startDate = LocalDate.of(startYear, Month.APRIL, 1);
            LocalDate endDate = LocalDate.of(endYear, Month.MARCH, 31);

            return new LocalDate[] { startDate, endDate };
        } catch (Exception e) {
            log.error("Error parsing financial year code: {}", fyCode, e);
            return null;
        }
    }
}
