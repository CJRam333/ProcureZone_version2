package com.nslindia.procurezone.report.service;

import com.nslindia.procurezone.report.dto.POReportDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating Purchase Order reports
 * Provides summary, vendor performance, and detailed reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class POReportService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get PO summary report
     */
    public List<POReportDTO.Summary> getPOSummary(
            LocalDate startDate,
            LocalDate endDate,
            Integer vendorId,
            Integer statusId) {

        log.info("Generating PO summary report from {} to {}", startDate, endDate);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    po.po_number as poNumber,
                    po.po_date as poDate,
                    v.vendor_name as vendorName,
                    v.vendor_code as vendorCode,
                    CASE po.po_status
                        WHEN 1 THEN 'Created'
                        WHEN 2 THEN 'Approved'
                        WHEN 3 THEN 'Sent to Vendor'
                        WHEN 4 THEN 'Partially Received'
                        WHEN 5 THEN 'Fully Received'
                        WHEN 6 THEN 'Closed'
                        WHEN 7 THEN 'Cancelled'
                        ELSE 'Unknown'
                    END as status,
                    po.total_amount as totalAmount,
                    po.delivery_date as deliveryDate,
                    po.payment_terms as paymentTerms,
                    COUNT(DISTINCT pod.id) as itemCount,
                    SUM(pod.quantity) as totalQuantity
                FROM tbl_purchase_orders po
                LEFT JOIN tbl_vendors v ON po.vendor_id = v.id
                LEFT JOIN tbl_purchase_order_details pod ON po.id = pod.po_id
                WHERE po.po_date BETWEEN :startDate AND :endDate
                """);

        if (vendorId != null) {
            sql.append(" AND po.vendor_id = :vendorId");
        }
        if (statusId != null) {
            sql.append(" AND po.po_status = :statusId");
        }

        sql.append("""
                GROUP BY po.id, po.po_number, po.po_date, v.vendor_name, v.vendor_code,
                         po.po_status, po.total_amount, po.delivery_date, po.payment_terms
                ORDER BY po.po_date DESC
                """);

        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        if (vendorId != null)
            query.setParameter("vendorId", vendorId);
        if (statusId != null)
            query.setParameter("statusId", statusId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<POReportDTO.Summary> summaries = new ArrayList<>();
        for (Object[] row : results) {
            summaries.add(POReportDTO.Summary.builder()
                    .poNumber((String) row[0])
                    .poDate(row[1] != null ? ((java.sql.Date) row[1]).toLocalDate() : null)
                    .vendorName((String) row[2])
                    .vendorCode((String) row[3])
                    .status((String) row[4])
                    .totalAmount(row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO)
                    .deliveryDate(row[6] != null ? ((java.sql.Date) row[6]).toLocalDate() : null)
                    .paymentTerms((String) row[7])
                    .itemCount(row[8] != null ? ((Number) row[8]).longValue() : 0L)
                    .totalQuantity(row[9] != null ? ((Number) row[9]).doubleValue() : 0.0)
                    .build());
        }

        log.info("Generated PO summary report with {} records", summaries.size());
        return summaries;
    }

    /**
     * Get vendor performance report
     */
    public List<POReportDTO.VendorPerformance> getVendorPerformance(
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Generating vendor performance report from {} to {}", startDate, endDate);

        String sql = """
                SELECT
                    v.vendor_name as vendorName,
                    v.vendor_code as vendorCode,
                    COUNT(DISTINCT po.id) as totalPOs,
                    SUM(po.total_amount) as totalValue,
                    SUM(CASE WHEN po.po_status = 5 THEN 1 ELSE 0 END) as completedPOs,
                    SUM(CASE WHEN po.delivery_date < CURDATE() AND po.po_status NOT IN (5, 6, 7) THEN 1 ELSE 0 END) as delayedPOs,
                    AVG(CASE 
                        WHEN po.po_status = 5 AND grn.goods_receipt_date IS NOT NULL 
                        THEN DATEDIFF(grn.goods_receipt_date, po.po_date)
                        ELSE NULL 
                    END) as avgDeliveryDays,
                    SUM(CASE WHEN po.po_status = 7 THEN 1 ELSE 0 END) as cancelledPOs
                FROM tbl_vendors v
                LEFT JOIN tbl_purchase_orders po ON v.id = po.vendor_id 
                    AND po.po_date BETWEEN :startDate AND :endDate
                LEFT JOIN tbl_goods_receipt grn ON po.id = grn.goods_receipt_po_id
                GROUP BY v.id, v.vendor_name, v.vendor_code
                HAVING totalPOs > 0
                ORDER BY totalValue DESC
                """;

        var query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<POReportDTO.VendorPerformance> performance = new ArrayList<>();
        for (Object[] row : results) {
            Long totalPOs = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long completedPOs = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            
            Double onTimeDeliveryRate = totalPOs > 0 
                ? (completedPOs.doubleValue() / totalPOs.doubleValue() * 100) 
                : 0.0;

            performance.add(POReportDTO.VendorPerformance.builder()
                    .vendorName((String) row[0])
                    .vendorCode((String) row[1])
                    .totalPOs(totalPOs)
                    .totalValue(row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO)
                    .completedPOs(completedPOs)
                    .delayedPOs(row[5] != null ? ((Number) row[5]).longValue() : 0L)
                    .avgDeliveryDays(row[6] != null ? ((Number) row[6]).doubleValue() : 0.0)
                    .cancelledPOs(row[7] != null ? ((Number) row[7]).longValue() : 0L)
                    .onTimeDeliveryRate(onTimeDeliveryRate)
                    .build());
        }

        log.info("Generated vendor performance report for {} vendors", performance.size());
        return performance;
    }

    /**
     * Get PO statistics by status
     */
    public List<Map<String, Object>> getPOStatsByStatus(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    CASE po.po_status
                        WHEN 1 THEN 'Created'
                        WHEN 2 THEN 'Approved'
                        WHEN 3 THEN 'Sent to Vendor'
                        WHEN 4 THEN 'Partially Received'
                        WHEN 5 THEN 'Fully Received'
                        WHEN 6 THEN 'Closed'
                        WHEN 7 THEN 'Cancelled'
                        ELSE 'Unknown'
                    END as status,
                    COUNT(po.id) as count,
                    SUM(po.total_amount) as totalValue
                FROM tbl_purchase_orders po
                WHERE po.po_date BETWEEN :startDate AND :endDate
                GROUP BY po.po_status
                ORDER BY po.po_status
                """;

        var query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> stats = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", row[0]);
            map.put("count", ((Number) row[1]).longValue());
            map.put("totalValue", row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO);
            stats.add(map);
        }

        return stats;
    }
}
