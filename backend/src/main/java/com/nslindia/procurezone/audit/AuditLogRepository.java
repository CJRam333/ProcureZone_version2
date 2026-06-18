package com.nslindia.procurezone.audit;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for AuditLog entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    /**
     * Combined filter query — all params are optional (null = no filter).
     * search matches action, entityType, and username (case-insensitive).
     * performedBy matches username exactly (case-insensitive LIKE).
     */
    @Query("""
        SELECT a FROM AuditLog a WHERE
            (:search IS NULL OR
                LOWER(a.action)     LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(a.username)   LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:performedBy IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :performedBy, '%')))
            AND (:action      IS NULL OR a.action     = :action)
            AND (:entityType  IS NULL OR a.entityType = :entityType)
            AND (:fromDate    IS NULL OR a.timestamp >= :fromDate)
            AND (:toDate      IS NULL OR a.timestamp <= :toDate)
        """)
    Page<AuditLog> searchWithFilters(
        @Param("search")      String  search,
        @Param("performedBy") String  performedBy,
        @Param("action")      String  action,
        @Param("entityType")  String  entityType,
        @Param("fromDate")    Instant fromDate,
        @Param("toDate")      Instant toDate,
        Pageable pageable);
}
