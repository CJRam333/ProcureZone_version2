package com.nslindia.procurezone.report.service;

import com.nslindia.procurezone.report.dto.InventoryReportDTO;
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
 * Service for generating Inventory reports
 * Provides stock status, material usage, and low stock alerts
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryReportService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get current stock status for all materials
     */
    public List<InventoryReportDTO.StockStatus> getStockStatus(
            Integer companyId,
            Integer plantId,
            Integer locationId) {

        log.info("Generating stock status report for company={}, plant={}, location={}",
                companyId, plantId, locationId);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.material_code as materialCode,
                    m.material_name as materialName,
                    m.material_group as materialGroup,
                    u.uom_name as uom,
                    ib.current_qty as currentQuantity,
                    ib.reserved_qty as reservedQuantity,
                    ib.available_qty as availableQuantity,
                    ib.min_stock_level as minStockLevel,
                    ib.max_stock_level as maxStockLevel,
                    ib.reorder_level as reorderLevel,
                    CASE
                        WHEN ib.available_qty <= ib.min_stock_level THEN 'Critical'
                        WHEN ib.available_qty <= ib.reorder_level THEN 'Low'
                        WHEN ib.available_qty >= ib.max_stock_level THEN 'Excess'
                        ELSE 'Normal'
                    END as stockStatus,
                    c.company_name as companyName,
                    p.plant_name as plantName,
                    l.location_name as locationName,
                    ib.last_transaction_date as lastTransactionDate
                FROM tbl_inventory_balance ib
                INNER JOIN tbl_material_master m ON ib.material_id = m.material_id
                LEFT JOIN tbl_umo_master u ON m.material_uom = u.uom_id
                LEFT JOIN tbl_company_master c ON ib.company_id = c.company_id
                LEFT JOIN tbl_plant_master p ON ib.plant_id = p.plant_id
                LEFT JOIN tbl_location_master l ON ib.location_id = l.location_id
                WHERE ib.status_id = 1
                """);

        if (companyId != null) {
            sql.append(" AND ib.company_id = :companyId");
        }
        if (plantId != null) {
            sql.append(" AND ib.plant_id = :plantId");
        }
        if (locationId != null) {
            sql.append(" AND ib.location_id = :locationId");
        }

        sql.append(" ORDER BY m.material_code");

        var query = entityManager.createNativeQuery(sql.toString());
        if (companyId != null)
            query.setParameter("companyId", companyId);
        if (plantId != null)
            query.setParameter("plantId", plantId);
        if (locationId != null)
            query.setParameter("locationId", locationId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<InventoryReportDTO.StockStatus> stockList = new ArrayList<>();
        for (Object[] row : results) {
            stockList.add(InventoryReportDTO.StockStatus.builder()
                    .materialCode((String) row[0])
                    .materialName((String) row[1])
                    .materialGroup((String) row[2])
                    .uom((String) row[3])
                    .currentQuantity(row[4] != null ? ((BigDecimal) row[4]).doubleValue() : 0.0)
                    .reservedQuantity(row[5] != null ? ((BigDecimal) row[5]).doubleValue() : 0.0)
                    .availableQuantity(row[6] != null ? ((BigDecimal) row[6]).doubleValue() : 0.0)
                    .minStockLevel(row[7] != null ? ((BigDecimal) row[7]).doubleValue() : 0.0)
                    .maxStockLevel(row[8] != null ? ((BigDecimal) row[8]).doubleValue() : 0.0)
                    .reorderLevel(row[9] != null ? ((BigDecimal) row[9]).doubleValue() : 0.0)
                    .stockStatus((String) row[10])
                    .companyName((String) row[11])
                    .plantName((String) row[12])
                    .locationName((String) row[13])
                    .lastTransactionDate(
                            row[14] != null ? ((java.sql.Timestamp) row[14]).toLocalDateTime().toLocalDate() : null)
                    .build());
        }

        log.info("Generated stock status report with {} records", stockList.size());
        return stockList;
    }

    /**
     * Get low stock alerts (materials below reorder level)
     */
    public List<InventoryReportDTO.LowStock> getLowStockAlerts(Integer companyId) {

        log.info("Generating low stock alerts for company={}", companyId);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.material_code as materialCode,
                    m.material_name as materialName,
                    u.uom_name as uom,
                    ib.available_qty as availableQuantity,
                    ib.reorder_level as reorderLevel,
                    ib.min_stock_level as minStockLevel,
                    (ib.reorder_level - ib.available_qty) as shortfall,
                    c.company_name as companyName,
                    p.plant_name as plantName,
                    l.location_name as locationName
                FROM tbl_inventory_balance ib
                INNER JOIN tbl_material_master m ON ib.material_id = m.material_id
                LEFT JOIN tbl_umo_master u ON m.material_uom = u.uom_id
                LEFT JOIN tbl_company_master c ON ib.company_id = c.company_id
                LEFT JOIN tbl_plant_master p ON ib.plant_id = p.plant_id
                LEFT JOIN tbl_location_master l ON ib.location_id = l.location_id
                WHERE ib.status_id = 1
                AND ib.available_qty <= ib.reorder_level
                """);

        if (companyId != null) {
            sql.append(" AND ib.company_id = :companyId");
        }

        sql.append(" ORDER BY (ib.reorder_level - ib.available_qty) DESC");

        var query = entityManager.createNativeQuery(sql.toString());
        if (companyId != null)
            query.setParameter("companyId", companyId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<InventoryReportDTO.LowStock> alerts = new ArrayList<>();
        for (Object[] row : results) {
            alerts.add(InventoryReportDTO.LowStock.builder()
                    .materialCode((String) row[0])
                    .materialName((String) row[1])
                    .uom((String) row[2])
                    .availableQuantity(row[3] != null ? ((BigDecimal) row[3]).doubleValue() : 0.0)
                    .reorderLevel(row[4] != null ? ((BigDecimal) row[4]).doubleValue() : 0.0)
                    .minStockLevel(row[5] != null ? ((BigDecimal) row[5]).doubleValue() : 0.0)
                    .shortfall(row[6] != null ? ((BigDecimal) row[6]).doubleValue() : 0.0)
                    .companyName((String) row[7])
                    .plantName((String) row[8])
                    .locationName((String) row[9])
                    .build());
        }

        log.info("Generated {} low stock alerts", alerts.size());
        return alerts;
    }

    /**
     * Get material usage report
     */
    public List<InventoryReportDTO.MaterialUsage> getMaterialUsage(
            LocalDate startDate,
            LocalDate endDate,
            Integer materialId) {

        log.info("Generating material usage report from {} to {}", startDate, endDate);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.material_code as materialCode,
                    m.material_name as materialName,
                    u.uom_name as uom,
                    it.transaction_type as transactionType,
                    SUM(CASE WHEN it.transaction_type = 'IN' THEN it.quantity ELSE 0 END) as totalIn,
                    SUM(CASE WHEN it.transaction_type = 'OUT' THEN it.quantity ELSE 0 END) as totalOut,
                    COUNT(*) as transactionCount,
                    c.company_name as companyName
                FROM tbl_inventory_transaction it
                INNER JOIN tbl_material_master m ON it.material_id = m.material_id
                LEFT JOIN tbl_umo_master u ON m.material_uom = u.uom_id
                LEFT JOIN tbl_company_master c ON it.company_id = c.company_id
                WHERE it.transaction_date BETWEEN :startDate AND :endDate
                """);

        if (materialId != null) {
            sql.append(" AND it.material_id = :materialId");
        }

        sql.append("""
                GROUP BY m.material_code, m.material_name, u.uom_name,
                         it.transaction_type, c.company_name
                ORDER BY m.material_code, it.transaction_type
                """);

        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (materialId != null)
            query.setParameter("materialId", materialId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<InventoryReportDTO.MaterialUsage> usageList = new ArrayList<>();
        for (Object[] row : results) {
            usageList.add(InventoryReportDTO.MaterialUsage.builder()
                    .materialCode((String) row[0])
                    .materialName((String) row[1])
                    .uom((String) row[2])
                    .transactionType((String) row[3])
                    .totalIn(row[4] != null ? ((BigDecimal) row[4]).doubleValue() : 0.0)
                    .totalOut(row[5] != null ? ((BigDecimal) row[5]).doubleValue() : 0.0)
                    .transactionCount(((Number) row[6]).intValue())
                    .companyName((String) row[7])
                    .build());
        }

        log.info("Generated material usage report with {} records", usageList.size());
        return usageList;
    }

    /**
     * Get inventory value report
     */
    public List<Map<String, Object>> getInventoryValue(Integer companyId) {

        log.info("Generating inventory value report for company={}", companyId);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.material_group as materialGroup,
                    COUNT(DISTINCT m.material_id) as materialCount,
                    SUM(ib.available_qty) as totalQuantity,
                    SUM(ib.available_qty * m.material_rate) as totalValue
                FROM tbl_inventory_balance ib
                INNER JOIN tbl_material_master m ON ib.material_id = m.material_id
                WHERE ib.status_id = 1
                """);

        if (companyId != null) {
            sql.append(" AND ib.company_id = :companyId");
        }

        sql.append(" GROUP BY m.material_group ORDER BY totalValue DESC");

        var query = entityManager.createNativeQuery(sql.toString());
        if (companyId != null)
            query.setParameter("companyId", companyId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> valueList = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("materialGroup", row[0]);
            map.put("materialCount", ((Number) row[1]).intValue());
            map.put("totalQuantity", row[2] != null ? ((BigDecimal) row[2]).doubleValue() : 0.0);
            map.put("totalValue", row[3] != null ? ((BigDecimal) row[3]).doubleValue() : 0.0);
            valueList.add(map);
        }

        return valueList;
    }
}
