package com.nslindia.procurezone.notification.repository;

import com.nslindia.procurezone.notification.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Integer> {

    /**
     * Find email template by unique code
     */
    Optional<EmailTemplate> findByCode(String code);

    /**
     * Find active templates by category
     */
    List<EmailTemplate> findByCategoryAndStatus(String category, Integer status);

    /**
     * Find all active templates
     */
    List<EmailTemplate> findByStatus(Integer status);

    /**
     * Check if template code exists
     */
    boolean existsByCode(String code);
}
