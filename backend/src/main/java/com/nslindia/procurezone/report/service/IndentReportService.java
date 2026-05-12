package com.nslindia.procurezone.report.service;

import com.nslindia.procurezone.report.dto.IndentReportDTO;
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
 * Service for generating Indent reports
 * Provides summary and detailed reports for indents
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IndentReportService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get indent summary report
     * 
     * @param startDate Start date filter
     * @param endDate   End date filter
     * @param statusId  Status filter (optional)
     * @param deptId    Department filter (optional)
     * @param companyId Company filter (optional)
     * @return List of indent summary data
     */
    public List<IndentReportDTO.Summary> getIndentSummary(
            LocalDate startDate,
            LocalDate endDate,
            Integer statusId,
            Integer deptId,
            Integer companyId) {

        log.info("Generating indent summary report from {} to {}", startDate, endDate);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    i.indent_no as indentNo,
                    i.indent_date as indentDate,
                    d.dept_name as departmentName,
                    c.company_name as companyName,
                    e.emp_name as createdBy,
                    CASE i.indent_status
                        WHEN 0 THEN 'Draft'
                        WHEN 1 THEN 'Submitted'
                        WHEN 2 THEN 'L1 Approved'
                        WHEN 3 THEN 'L1 Rejected'
                        WHEN 4 THEN 'L2 Approved'
                        WHEN 5 THEN 'L2 Rejected'
                        WHEN 6 THEN 'PO Created'
                        WHEN 7 THEN 'Closed'
                        ELSE 'Unknown'
                    END as status,
                    i.indent_priority as priority,
                    COUNT(DISTINCT id.indent_details_id) as itemCount,
                    SUM(id.indent_details_qty) as totalQuantity
                FROM tbl_indent_master i
                LEFT JOIN tbl_department_master d ON i.indent_dept = d.dept_id
                LEFT JOIN tbl_company_master c ON i.indent_company = c.company_id
                LEFT JOIN tbl_emp_master e ON i.indent_createdby = e.emp_number
                LEFT JOIN tbl_indent_details id ON i.indent_id = id.indent_id
                WHERE i.indent_date BETWEEN :startDate AND :endDate
                """);

        if (statusId != null) {
            sql.append(" AND i.indent_status = :statusId");
        }
        if (deptId != null) {
            sql.append(" AND i.indent_dept = :deptId");
        }
        if (companyId != null) {
            sql.append(" AND i.indent_company = :companyId");
        }

        sql.append("""
                GROUP BY i.indent_id, i.indent_no, i.indent_date, d.dept_name,
                         c.company_name, e.emp_name, i.indent_status, i.indent_priority
                ORDER BY i.indent_date DESC
                """);

        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        if (statusId != null)
            query.setParameter("statusId", statusId);
        if (deptId != null)
            query.setParameter("deptId", deptId);
        if (companyId != null)
            query.setParameter("companyId", companyId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<IndentReportDTO.Summary> summaries = new ArrayList<>();
        for (Object[] row : results) {
            summaries.add(IndentReportDTO.Summary.builder()
                    .indentNo((String) row[0])
                    .indentDate(row[1] != null ? ((java.sql.Date) row[1]).toLocalDate() : null)
                    .departmentName((String) row[2])
                    .companyName((String) row[3])
                    .createdBy((String) row[4])
                    .status((String) row[5])
                    .priority(row[6] != null ? ((Number) row[6]).intValue() : 0)
                    .itemCount(row[7] != null ? ((Number) row[7]).longValue() : 0L)
                    .totalQuantity(row[8] != null ? ((Number) row[8]).doubleValue() : 0.0)
                    .build());
        }

        log.info("Generated indent summary report with {} records", summaries.size());
        return summaries;
    }

    /**
     * Get detailed indent report with line items
     */
    public List<IndentReportDTO.Detailed> getIndentDetailed(
            LocalDate startDate,
            LocalDate endDate,
            Integer statusId,
            Integer deptId) {

        log.info("Generating detailed indent report from {} to {}", startDate, endDate);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    i.indent_no as indentNo,
                    i.indent_date as indentDate,
                    d.dept_name as departmentName,
                    m.material_name as materialName,
                    m.material_code as materialCode,
                    id.indent_details_qty as quantity,
                    u.umo_name as uom,
                    id.indent_details_rate as rate,
                    id.indent_details_reqdate as requiredDate,
                    e.emp_name as createdBy,
                    CASE i.indent_status
                        WHEN 0 THEN 'Draft'
                        WHEN 1 THEN 'Submitted'
                        WHEN 2 THEN 'L1 Approved'
                        WHEN 3 THEN 'L1 Rejected'
                        WHEN 4 THEN 'L2 Approved'
                        WHEN 5 THEN 'L2 Rejected'
                        WHEN 6 THEN 'PO Created'
                        WHEN 7 THEN 'Closed'
                        ELSE 'Unknown'
                    END as status,
                    id.indent_details_remarks as remarks
                FROM tbl_indent_master i
                INNER JOIN tbl_indent_details id ON i.indent_id = id.indent_id
                LEFT JOIN tbl_department_master d ON i.indent_dept = d.dept_id
                LEFT JOIN tbl_material_master m ON id.indent_details_material = m.material_id
                LEFT JOIN tbl_umo_master u ON m.material_umo = u.umo_id
                LEFT JOIN tbl_emp_master e ON i.indent_createdby = e.emp_number
                WHERE i.indent_date BETWEEN :startDate AND :endDate
                """);

        if (statusId != null) {
            sql.append(" AND i.indent_status = :statusId");
        }
        if (deptId != null) {
            sql.append(" AND i.indent_dept = :deptId");
        }

        sql.append(" ORDER BY i.indent_date DESC, i.indent_no, m.material_name");

        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        if (statusId != null)
            query.setParameter("statusId", statusId);
        if (deptId != null)
            query.setParameter("deptId", deptId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<IndentReportDTO.Detailed> details = new ArrayList<>();
        for (Object[] row : results) {
            details.add(IndentReportDTO.Detailed.builder()
                    .indentNo((String) row[0])
                    .indentDate(row[1] != null ? ((java.sql.Date) row[1]).toLocalDate() : null)
                    .departmentName((String) row[2])
                    .materialName((String) row[3])
                    .materialCode((String) row[4])
                    .quantity(row[5] != null ? ((Number) row[5]).doubleValue() : 0.0)
                    .uom((String) row[6])
                    .rate(row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO)
                    .requiredDate(row[8] != null ? ((java.sql.Date) row[8]).toLocalDate() : null)
                    .createdBy((String) row[9])
                    .status((String) row[10])
                    .remarks((String) row[11])
                    .build());
        }

        log.info("Generated detailed indent report with {} line items", details.size());
        return details;
    }

    /**
     * Get indent statistics by department
     */
    public List<Map<String, Object>> getIndentStatsByDepartment(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    d.dept_name as department,
                    COUNT(i.indent_id) as totalIndents,
                    SUM(CASE WHEN i.indent_status IN (2, 4, 6, 7) THEN 1 ELSE 0 END) as approvedIndents,
                    SUM(CASE WHEN i.indent_status IN (3, 5) THEN 1 ELSE 0 END) as rejectedIndents,
                    SUM(CASE WHEN i.indent_status = 1 THEN 1 ELSE 0 END) as pendingIndents
                FROM tbl_indent_master i
                LEFT JOIN tbl_department_master d ON i.indent_dept = d.dept_id
                WHERE i.indent_date BETWEEN :startDate AND :endDate
                GROUP BY d.dept_id, d.dept_name
                ORDER BY totalIndents DESC
                """;

        var query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> stats = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("department", row[0]);
            map.put("totalIndents", ((Number) row[1]).longValue());
            map.put("approvedIndents", ((Number) row[2]).longValue());
            map.put("rejectedIndents", ((Number) row[3]).longValue());
            map.put("pendingIndents", ((Number) row[4]).longValue());
            stats.add(map);
        }

        return stats;
    }

    /**
     * Get indent statistics by priority
     */
    public List<Map<String, Object>> getIndentStatsByPriority(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    CASE i.indent_priority
                        WHEN 1 THEN 'Low'
                        WHEN 2 THEN 'Medium'
                        WHEN 3 THEN 'High'
                        WHEN 4 THEN 'Urgent'
                        ELSE 'Unknown'
                    END as priority,
                    COUNT(i.indent_id) as count
                FROM tbl_indent_master i
                WHERE i.indent_date BETWEEN :startDate AND :endDate
                GROUP BY i.indent_priority
                ORDER BY i.indent_priority DESC
                """;

        var query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> stats = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("priority", row[0]);
            map.put("count", ((Number) row[1]).longValue());
            stats.add(map);
        }

        return stats;
    }
}
