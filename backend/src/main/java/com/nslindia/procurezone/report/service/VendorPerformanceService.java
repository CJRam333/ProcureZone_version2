package com.nslindia.procurezone.report.service;

import com.nslindia.procurezone.report.dto.VendorPerformanceDTO;
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
 * Service for generating Vendor Performance reports
 * Provides vendor statistics, delivery performance, and quality metrics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VendorPerformanceService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get vendor performance summary
     */
    public List<VendorPerformanceDTO.Summary> getVendorPerformance(
            LocalDate startDate,
            LocalDate endDate,
            Integer vendorId) {

        log.info("Generating vendor performance report from {} to {}", startDate, endDate);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    v.id as vendorId,
                    v.vendor_name as vendorName,
                    v.vendor_code as vendorCode,
                    v.vendor_type as vendorCategory,
                    COUNT(DISTINCT po.id) as totalPOs,
                    SUM(po.total_amount) as totalPurchaseValue,
                    COUNT(DISTINCT CASE WHEN po.po_status = 5 THEN po.id END) as fulfilledPOs,
                    COUNT(DISTINCT CASE WHEN po.po_status = 7 THEN po.id END) as cancelledPOs,
                    AVG(DATEDIFF(gr.goods_receipt_date, po.po_date)) as avgDeliveryDays,
                    COUNT(DISTINCT gr.goods_receipt_id) as totalGRNs,
                    SUM(CASE WHEN gr.goods_receipt_status = 2 THEN 1 ELSE 0 END) as acceptedGRNs,
                    SUM(CASE WHEN gr.goods_receipt_status = 3 THEN 1 ELSE 0 END) as rejectedGRNs
                FROM tbl_vendors v
                LEFT JOIN tbl_purchase_orders po ON v.id = po.vendor_id
                    AND po.po_date BETWEEN :startDate AND :endDate
                LEFT JOIN tbl_goods_receipt gr ON po.indent_id = gr.indent_id
                WHERE v.status = 1
                """);

        if (vendorId != null) {
            sql.append(" AND v.id = :vendorId");
        }

        sql.append("""
                GROUP BY v.id, v.vendor_name, v.vendor_code, v.vendor_type
                ORDER BY totalPurchaseValue DESC
                """);

        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (vendorId != null)
            query.setParameter("vendorId", vendorId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<VendorPerformanceDTO.Summary> summaries = new ArrayList<>();
        for (Object[] row : results) {
            Integer totalPOs = row[4] != null ? ((Number) row[4]).intValue() : 0;
            Integer fulfilledPOs = row[6] != null ? ((Number) row[6]).intValue() : 0;
            Integer totalGRNs = row[9] != null ? ((Number) row[9]).intValue() : 0;
            Integer acceptedGRNs = row[10] != null ? ((Number) row[10]).intValue() : 0;

            Double fulfillmentRate = totalPOs > 0 ? (fulfilledPOs * 100.0 / totalPOs) : 0.0;
            Double qualityRate = totalGRNs > 0 ? (acceptedGRNs * 100.0 / totalGRNs) : 0.0;

            summaries.add(VendorPerformanceDTO.Summary.builder()
                    .vendorId(((Number) row[0]).intValue())
                    .vendorName((String) row[1])
                    .vendorCode((String) row[2])
                    .vendorCategory((String) row[3])
                    .totalPOs(totalPOs)
                    .totalPurchaseValue(row[5] != null ? ((BigDecimal) row[5]).doubleValue() : 0.0)
                    .fulfilledPOs(fulfilledPOs)
                    .cancelledPOs(row[7] != null ? ((Number) row[7]).intValue() : 0)
                    .avgDeliveryDays(row[8] != null ? ((BigDecimal) row[8]).doubleValue() : 0.0)
                    .totalGRNs(totalGRNs)
                    .acceptedGRNs(acceptedGRNs)
                    .rejectedGRNs(row[11] != null ? ((Number) row[11]).intValue() : 0)
                    .fulfillmentRate(fulfillmentRate)
                    .qualityRate(qualityRate)
                    .build());
        }

        log.info("Generated vendor performance report with {} vendors", summaries.size());
        return summaries;
    }

    /**
     * Get vendor delivery performance
     */
    public List<VendorPerformanceDTO.DeliveryPerformance> getDeliveryPerformance(
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Generating vendor delivery performance from {} to {}", startDate, endDate);

        String sql = """
                SELECT
                    v.vendor_name as vendorName,
                    po.po_number as poNumber,
                    po.po_date as poDate,
                    po.delivery_date as expectedDate,
                    gr.receipt_date as actualDate,
                    DATEDIFF(gr.receipt_date, po.delivery_date) as delayDays,
                    CASE
                        WHEN DATEDIFF(gr.receipt_date, po.delivery_date) <= 0 THEN 'On Time'
                        WHEN DATEDIFF(gr.receipt_date, po.delivery_date) <= 7 THEN 'Minor Delay'
                        ELSE 'Major Delay'
                    END as deliveryStatus,
                    po.total_amount as poValue
                FROM tbl_goods_receipt gr
                INNER JOIN tbl_purchase_orders po ON gr.po_id = po.id
                INNER JOIN tbl_vendors v ON po.vendor_id = v.id
                WHERE po.po_date BETWEEN :startDate AND :endDate
                ORDER BY gr.receipt_date DESC
                """;

        var query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<VendorPerformanceDTO.DeliveryPerformance> deliveries = new ArrayList<>();
        for (Object[] row : results) {
            deliveries.add(VendorPerformanceDTO.DeliveryPerformance.builder()
                    .vendorName((String) row[0])
                    .poNumber((String) row[1])
                    .poDate(row[2] != null ? ((java.sql.Date) row[2]).toLocalDate() : null)
                    .expectedDate(row[3] != null ? ((java.sql.Date) row[3]).toLocalDate() : null)
                    .actualDate(row[4] != null ? ((java.sql.Date) row[4]).toLocalDate() : null)
                    .delayDays(row[5] != null ? ((Number) row[5]).intValue() : 0)
                    .deliveryStatus((String) row[6])
                    .poValue(row[7] != null ? ((BigDecimal) row[7]).doubleValue() : 0.0)
                    .build());
        }

        return deliveries;
    }

    /**
     * Get vendor ranking by various metrics
     */
    public List<Map<String, Object>> getVendorRanking(
            LocalDate startDate,
            LocalDate endDate,
            String rankBy) {

        log.info("Generating vendor ranking by {} from {} to {}", rankBy, startDate, endDate);

        String orderByClause = switch (rankBy != null ? rankBy.toLowerCase() : "value") {
            case "quality" -> "qualityRate DESC";
            case "delivery" -> "onTimeRate DESC";
            case "volume" -> "totalPOs DESC";
            default -> "totalValue DESC";
        };

        String sql = String.format("""
                SELECT
                    v.vendor_name as vendorName,
                    COUNT(DISTINCT po.id) as totalPOs,
                    SUM(po.total_amount) as totalValue,
                    AVG(CASE WHEN DATEDIFF(gr.receipt_date, po.delivery_date) <= 0
                        THEN 100 ELSE 0 END) as onTimeRate,
                    AVG(CASE WHEN gr.quality_status = 'ACCEPTED'
                        THEN 100 ELSE 0 END) as qualityRate
                FROM tbl_vendors v
                LEFT JOIN tbl_purchase_orders po ON v.id = po.vendor_id
                    AND po.po_date BETWEEN :startDate AND :endDate
                LEFT JOIN tbl_goods_receipt gr ON po.id = gr.po_id
                WHERE v.status_id = 1
                GROUP BY v.id, v.vendor_name
                HAVING COUNT(DISTINCT po.id) > 0
                ORDER BY %s
                """, orderByClause);

        var query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> rankings = new ArrayList<>();
        int rank = 1;
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("rank", rank++);
            map.put("vendorName", row[0]);
            map.put("totalPOs", row[1] != null ? ((Number) row[1]).intValue() : 0);
            map.put("totalValue", row[2] != null ? ((BigDecimal) row[2]).doubleValue() : 0.0);
            map.put("onTimeRate", row[3] != null ? ((BigDecimal) row[3]).doubleValue() : 0.0);
            map.put("qualityRate", row[4] != null ? ((BigDecimal) row[4]).doubleValue() : 0.0);
            rankings.add(map);
        }

        return rankings;
    }
}
