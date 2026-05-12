package com.nslindia.procurezone.repository.inventory;

import com.nslindia.procurezone.domain.inventory.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InventoryTransaction entity
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

        /**
         * Find transactions by inventory (using nested property)
         */
        List<InventoryTransaction> findByInventory_Id(Integer inventoryId);

        /**
         * Find transactions by inventory
         */
        List<InventoryTransaction> findByInventoryId(Integer inventoryId);

        /**
         * Find transactions by material
         */
        List<InventoryTransaction> findByMaterialId(Integer materialId);

        /**
         * Find transactions by plant
         */
        List<InventoryTransaction> findByPlantId(Integer plantId);

        /**
         * Find transactions by company
         */
        List<InventoryTransaction> findByCompanyId(Integer companyId);

        /**
         * Find transactions by type
         */
        List<InventoryTransaction> findByTransactionType(String transactionType);

        /**
         * Find transactions by reference
         */
        List<InventoryTransaction> findByReferenceTypeAndReferenceNumber(String referenceType, String referenceNumber);

        /**
         * Find transactions by reference ID
         */
        List<InventoryTransaction> findByReferenceTypeAndReferenceId(String referenceType, Integer referenceId);

        /**
         * Find transactions by date range
         */
        @Query("SELECT t FROM InventoryTransaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
                        "ORDER BY t.transactionDate DESC")
        List<InventoryTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find transactions by material and date range
         */
        @Query("SELECT t FROM InventoryTransaction t WHERE t.material.id = :materialId " +
                        "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
        List<InventoryTransaction> findByMaterialAndDateRange(@Param("materialId") Integer materialId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find transactions by plant and date range
         */
        @Query("SELECT t FROM InventoryTransaction t WHERE t.plant.id = :plantId " +
                        "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
        List<InventoryTransaction> findByPlantAndDateRange(@Param("plantId") Integer plantId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find recent transactions (last N days)
         */
        @Query("SELECT t FROM InventoryTransaction t WHERE t.transactionDate >= :sinceDate " +
                        "ORDER BY t.transactionDate DESC")
        List<InventoryTransaction> findRecentTransactions(@Param("sinceDate") LocalDateTime sinceDate);

        /**
         * Find last N transactions for a material
         */
        @Query("SELECT t FROM InventoryTransaction t WHERE t.material.id = :materialId " +
                        "ORDER BY t.transactionDate DESC")
        List<InventoryTransaction> findLastTransactionsByMaterial(@Param("materialId") Integer materialId);

        /**
         * Find transactions with filters
         */
        @Query("SELECT t FROM InventoryTransaction t WHERE " +
                        "(:companyId IS NULL OR t.company.id = :companyId) AND " +
                        "(:plantId IS NULL OR t.plant.id = :plantId) AND " +
                        "(:materialId IS NULL OR t.material.id = :materialId) AND " +
                        "(:transactionType IS NULL OR t.transactionType = :transactionType) AND " +
                        "(:direction IS NULL OR t.direction = :direction) AND " +
                        "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
                        "(:endDate IS NULL OR t.transactionDate <= :endDate) " +
                        "ORDER BY t.transactionDate DESC")
        List<InventoryTransaction> findByFilters(@Param("companyId") Integer companyId,
                        @Param("plantId") Integer plantId,
                        @Param("materialId") Integer materialId,
                        @Param("transactionType") String transactionType,
                        @Param("direction") String direction,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}
