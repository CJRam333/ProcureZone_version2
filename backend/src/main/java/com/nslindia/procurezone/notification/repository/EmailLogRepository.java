package com.nslindia.procurezone.notification.repository;

import com.nslindia.procurezone.notification.model.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    /**
     * Find emails by status
     */
    List<EmailLog> findByStatus(String status);

    /**
     * Find failed emails for retry
     */
    @Query("SELECT e FROM EmailLog e WHERE e.status = 'FAILED' AND e.retryCount < 3")
    List<EmailLog> findFailedEmailsForRetry();

    /**
     * Find emails sent in date range
     */
    List<EmailLog> findBySentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find emails by template code
     */
    Page<EmailLog> findByTemplateCode(String templateCode, Pageable pageable);

    /**
     * Count emails by status
     */
    Long countByStatus(String status);
}
