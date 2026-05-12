package com.nslindia.procurezone.repository.inventory;

import com.nslindia.procurezone.domain.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Inventory entity
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    /**
     * Find inventory by material and plant
     */
    Optional<Inventory> findByMaterialIdAndPlantId(Integer materialId, Integer plantId);

    /**
     * Find inventory by material and plant and company
     */
    Optional<Inventory> findByMaterialIdAndPlantIdAndCompanyId(Integer materialId, Integer plantId, Integer companyId);

    /**
     * Find all inventory for a material
     */
    List<Inventory> findByMaterialId(Integer materialId);

    /**
     * Find all inventory for a plant
     */
    List<Inventory> findByPlantId(Integer plantId);

    /**
     * Find all inventory for a company
     */
    List<Inventory> findByCompanyId(Integer companyId);

    /**
     * Find low stock items (below reorder level)
     */
    @Query("SELECT i FROM Inventory i WHERE i.reorderLevel IS NOT NULL " +
            "AND i.availableQuantity < i.reorderLevel ORDER BY i.availableQuantity ASC")
    List<Inventory> findLowStockItems();

    /**
     * Find critical stock items (below minimum level)
     */
    @Query("SELECT i FROM Inventory i WHERE i.minLevel IS NOT NULL " +
            "AND i.availableQuantity < i.minLevel ORDER BY i.availableQuantity ASC")
    List<Inventory> findCriticalStockItems();

    /**
     * Find overstock items (above maximum level)
     */
    @Query("SELECT i FROM Inventory i WHERE i.maxLevel IS NOT NULL " +
            "AND i.currentBalance > i.maxLevel ORDER BY i.currentBalance DESC")
    List<Inventory> findOverstockItems();

    /**
     * Find zero stock items
     */
    @Query("SELECT i FROM Inventory i WHERE i.currentBalance = 0 OR i.availableQuantity <= 0")
    List<Inventory> findZeroStockItems();

    /**
     * Get total inventory value for a company
     */
    @Query("SELECT COALESCE(SUM(i.totalValue), 0) FROM Inventory i WHERE i.company.id = :companyId")
    BigDecimal getTotalInventoryValue(@Param("companyId") Integer companyId);

    /**
     * Get total inventory value for a plant
     */
    @Query("SELECT COALESCE(SUM(i.totalValue), 0) FROM Inventory i WHERE i.plant.id = :plantId")
    BigDecimal getTotalInventoryValueByPlant(@Param("plantId") Integer plantId);

    /**
     * Get inventory count by plant
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.plant.id = :plantId AND i.currentBalance > 0")
    Long getActiveInventoryCountByPlant(@Param("plantId") Integer plantId);

    /**
     * Check if inventory exists for material and plant
     */
    boolean existsByMaterialIdAndPlantId(Integer materialId, Integer plantId);

    /**
     * Find inventory with filters
     */
    @Query("SELECT i FROM Inventory i WHERE " +
            "(:companyId IS NULL OR i.company.id = :companyId) AND " +
            "(:plantId IS NULL OR i.plant.id = :plantId) AND " +
            "(:materialId IS NULL OR i.material.id = :materialId) AND " +
            "(:lowStock IS NULL OR :lowStock = false OR (i.reorderLevel IS NOT NULL AND i.availableQuantity < i.reorderLevel))")
    List<Inventory> findByFilters(@Param("companyId") Integer companyId,
            @Param("plantId") Integer plantId,
            @Param("materialId") Integer materialId,
            @Param("lowStock") Boolean lowStock);
}
