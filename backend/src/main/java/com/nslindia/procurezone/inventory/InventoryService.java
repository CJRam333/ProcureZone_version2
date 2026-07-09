package com.nslindia.procurezone.inventory;

import com.nslindia.procurezone.domain.inventory.Inventory;
import com.nslindia.procurezone.domain.inventory.InventoryTransaction;
import com.nslindia.procurezone.dto.inventory.InventoryResponse;
import com.nslindia.procurezone.dto.inventory.InventoryStatisticsDTO;
import com.nslindia.procurezone.dto.inventory.InventoryTransactionResponse;
import com.nslindia.procurezone.dto.inventory.StockAdjustmentRequest;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.mapping.CompanyPlantMaterialMapRepository;
import com.nslindia.procurezone.mapping.CompanyPlantMaterialService;
import com.nslindia.procurezone.masterdata.dto.MaterialDropdownResponse;
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.UnitOfMeasure;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.repository.MaterialRepository;
import com.nslindia.procurezone.masterdata.repository.UnitOfMeasureRepository;
import com.nslindia.procurezone.masterdata.PlantRepository;
import com.nslindia.procurezone.repository.inventory.InventoryRepository;
import com.nslindia.procurezone.repository.inventory.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Service - Complete database-backed implementation
 * Handles inventory balance tracking and transaction history
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

        private final InventoryRepository inventoryRepository;
        private final InventoryTransactionRepository transactionRepository;
        private final MaterialRepository materialRepository;
        private final PlantRepository plantRepository;
        private final CompanyRepository companyRepository;
        private final UnitOfMeasureRepository uomRepository;
        private final EmployeeRepository employeeRepository;
        private final CompanyPlantMaterialService companyPlantMaterialService;
        private final CompanyPlantMaterialMapRepository companyPlantMaterialMapRepository;

        /**
         * Read-only stock view for the operational Inventory module. Reads the REAL stock source
         * (tbl_map_company_plant_material.map_quantity_stores) via the same query the material
         * dropdown uses, so the numbers always agree with creation/issue. Paginated in memory —
         * the catalogue is bounded (hundreds–low thousands of rows) and this reuses the proven,
         * already-filtered/ordered dropdown query rather than a fragile paginated GROUP BY.
         */
        @Transactional(readOnly = true)
        public Page<MaterialDropdownResponse> getStockView(String search, int page, int size) {
                String term = (search == null) ? "" : search.trim();
                List<MaterialDropdownResponse> all =
                                companyPlantMaterialMapRepository.searchForDropdownAllCompanies(term);
                Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
                int from = (int) pageable.getOffset();
                if (from >= all.size()) {
                        return new PageImpl<>(List.of(), pageable, all.size());
                }
                int to = Math.min(from + pageable.getPageSize(), all.size());
                return new PageImpl<>(all.subList(from, to), pageable, all.size());
        }

        /**
         * Get or create inventory record
         */
        @Transactional
        public Inventory getOrCreateInventory(Integer materialId, Integer plantId, Integer companyId, Integer uomId) {
                return inventoryRepository.findByMaterialIdAndPlantIdAndCompanyId(materialId, plantId, companyId)
                                .orElseGet(() -> {
                                        Material material = materialRepository.findById(materialId)
                                                        .orElseThrow(() -> new IllegalArgumentException(
                                                                        "Material not found: " + materialId));
                                        Plant plant = plantRepository.findById(plantId)
                                                        .orElseThrow(() -> new IllegalArgumentException(
                                                                        "Plant not found: " + plantId));
                                        Company company = companyRepository.findById(companyId)
                                                        .orElseThrow(() -> new IllegalArgumentException(
                                                                        "Company not found: " + companyId));
                                        UnitOfMeasure uom = uomRepository.findById(uomId)
                                                        .orElseThrow(() -> new IllegalArgumentException(
                                                                        "UOM not found: " + uomId));

                                        Inventory inventory = Inventory.builder()
                                                        .material(material)
                                                        .plant(plant)
                                                        .company(company)
                                                        .unitOfMeasure(uom)
                                                        .openingBalance(BigDecimal.ZERO)
                                                        .currentBalance(BigDecimal.ZERO)
                                                        .reservedQuantity(BigDecimal.ZERO)
                                                        .availableQuantity(BigDecimal.ZERO)
                                                        .avgRate(BigDecimal.ZERO)
                                                        .totalValue(BigDecimal.ZERO)
                                                        .build();

                                        return inventoryRepository.save(inventory);
                                });
        }

        /**
         * Add stock (GRN, opening balance, adjustment)
         * Validates material availability at plant before adding stock
         */
        @Transactional
        public InventoryResponse addStock(Integer materialId, Integer plantId, Integer companyId, Integer uomId,
                        BigDecimal quantity, BigDecimal rate, String transactionType,
                        String referenceNumber, Integer referenceId, String remarks) {

                // Validate material is available at this plant
                if (!companyPlantMaterialService.isMaterialAvailable(companyId, plantId, materialId)) {
                        log.error("Material {} is not mapped to Plant {} in Company {}. Cannot add stock.",
                                        materialId, plantId, companyId);
                        throw new IllegalStateException(
                                        String.format("Material ID %d is not authorized for use at Plant ID %d. " +
                                                        "Please configure the Company-Plant-Material mapping first.",
                                                        materialId, plantId));
                }

                Inventory inventory = getOrCreateInventory(materialId, plantId, companyId, uomId);
                BigDecimal beforeBalance = inventory.getCurrentBalance();

                // Update inventory balance
                inventory.setCurrentBalance(beforeBalance.add(quantity));

                // Update average rate using weighted average
                if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal oldValue = beforeBalance.multiply(inventory.getAvgRate());
                        BigDecimal newValue = quantity.multiply(rate);
                        BigDecimal totalValue = oldValue.add(newValue);
                        BigDecimal totalQty = inventory.getCurrentBalance();

                        if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
                                inventory.setAvgRate(totalValue.divide(totalQty, 2, RoundingMode.HALF_UP));
                        }
                }

                inventory.setLastReceiptDate(LocalDateTime.now());
                inventory = inventoryRepository.save(inventory);

                // Record transaction
                recordTransaction(inventory, quantity, rate, transactionType, "IN",
                                referenceNumber, referenceId, remarks, beforeBalance, inventory.getCurrentBalance());

                log.info("Stock added: Material={}, Plant={}, Qty={}, Type={}, Ref={}, Balance={}",
                                materialId, plantId, quantity, transactionType, referenceNumber,
                                inventory.getCurrentBalance());

                return mapToResponse(inventory);
        }

        /**
         * Deduct stock (Issue Note, consumption, wastage)
         * Validates material availability at plant before deducting stock
         */
        @Transactional
        public InventoryResponse deductStock(Integer materialId, Integer plantId, Integer companyId, Integer uomId,
                        BigDecimal quantity, String transactionType,
                        String referenceNumber, Integer referenceId, String remarks) {

                // Validate material is available at this plant
                if (!companyPlantMaterialService.isMaterialAvailable(companyId, plantId, materialId)) {
                        log.error("Material {} is not mapped to Plant {} in Company {}. Cannot deduct stock.",
                                        materialId, plantId, companyId);
                        throw new IllegalStateException(
                                        String.format("Material ID %d is not authorized for use at Plant ID %d. " +
                                                        "Please configure the Company-Plant-Material mapping first.",
                                                        materialId, plantId));
                }

                Inventory inventory = inventoryRepository
                                .findByMaterialIdAndPlantIdAndCompanyId(materialId, plantId, companyId)
                                .orElseThrow(() -> new InsufficientStockException(
                                                String.format("No inventory found for material %d at plant %d",
                                                                materialId, plantId)));

                BigDecimal beforeBalance = inventory.getCurrentBalance();

                // Check sufficient stock
                if (inventory.getAvailableQuantity().compareTo(quantity) < 0) {
                        throw new InsufficientStockException(
                                        String.format("Insufficient stock: Required=%s, Available=%s",
                                                        quantity, inventory.getAvailableQuantity()));
                }

                // Update inventory balance
                inventory.setCurrentBalance(beforeBalance.subtract(quantity));
                inventory.setLastIssueDate(LocalDateTime.now());
                inventory = inventoryRepository.save(inventory);

                // Record transaction
                recordTransaction(inventory, quantity, inventory.getAvgRate(), transactionType, "OUT",
                                referenceNumber, referenceId, remarks, beforeBalance, inventory.getCurrentBalance());

                log.info("Stock deducted: Material={}, Plant={}, Qty={}, Type={}, Ref={}, Balance={}",
                                materialId, plantId, quantity, transactionType, referenceNumber,
                                inventory.getCurrentBalance());

                return mapToResponse(inventory);
        }

        /**
         * Stock adjustment
         */
        @Transactional
        public InventoryResponse adjustStock(StockAdjustmentRequest request) {

                if ("ADD".equals(request.getAdjustmentType())) {
                        return addStock(request.getMaterialId(), request.getPlantId(), request.getCompanyId(),
                                        request.getUomId(), request.getQuantity(), request.getRate(),
                                        "ADJUSTMENT", "ADJ-" + System.currentTimeMillis(), null, request.getRemarks());
                } else {
                        return deductStock(request.getMaterialId(), request.getPlantId(), request.getCompanyId(),
                                        request.getUomId(), request.getQuantity(), "ADJUSTMENT",
                                        "ADJ-" + System.currentTimeMillis(), null, request.getRemarks());
                }
        }

        /**
         * Get available stock
         */
        public BigDecimal getAvailableStock(Integer materialId, Integer plantId) {
                return inventoryRepository.findByMaterialIdAndPlantId(materialId, plantId)
                                .map(Inventory::getAvailableQuantity)
                                .orElse(BigDecimal.ZERO);
        }

        /**
         * Check sufficient stock
         */
        public boolean isSufficientStock(Integer materialId, Integer plantId, BigDecimal requiredQuantity) {
                BigDecimal available = getAvailableStock(materialId, plantId);
                return available.compareTo(requiredQuantity) >= 0;
        }

        /**
         * Get inventory by filters
         */
        @Transactional(readOnly = true)
        public List<InventoryResponse> getInventory(Integer companyId, Integer plantId,
                        Integer materialId, Boolean lowStock) {
                return inventoryRepository.findByFilters(companyId, plantId, materialId, lowStock)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get low stock items
         */
        public List<InventoryResponse> getLowStockItems() {
                return inventoryRepository.findLowStockItems()
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get critical stock items
         */
        public List<InventoryResponse> getCriticalStockItems() {
                return inventoryRepository.findCriticalStockItems()
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get inventory statistics
         */
        public InventoryStatisticsDTO getStatistics(Integer companyId) {
                List<Inventory> allInventory = companyId != null
                                ? inventoryRepository.findByCompanyId(companyId)
                                : inventoryRepository.findAll();

                BigDecimal totalValue = companyId != null
                                ? inventoryRepository.getTotalInventoryValue(companyId)
                                : allInventory.stream()
                                                .map(Inventory::getTotalValue)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return InventoryStatisticsDTO.builder()
                                .totalInventoryItems((long) allInventory.size())
                                .activeInventoryItems(allInventory.stream()
                                                .filter(i -> i.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0)
                                                .count())
                                .zeroStockItems(allInventory.stream()
                                                .filter(i -> i.getCurrentBalance().compareTo(BigDecimal.ZERO) == 0)
                                                .count())
                                .lowStockItems(allInventory.stream().filter(Inventory::isBelowReorderLevel).count())
                                .criticalStockItems(allInventory.stream().filter(Inventory::isBelowMinLevel).count())
                                .overstockItems(allInventory.stream().filter(Inventory::isAboveMaxLevel).count())
                                .totalInventoryValue(totalValue)
                                .avgInventoryValue(allInventory.isEmpty() ? BigDecimal.ZERO
                                                : totalValue.divide(new BigDecimal(allInventory.size()), 2,
                                                                RoundingMode.HALF_UP))
                                .build();
        }

        /**
         * Get transaction history
         */
        @Transactional(readOnly = true)
        public List<InventoryTransactionResponse> getTransactionHistory(Integer companyId, Integer plantId,
                        Integer materialId, String transactionType,
                        String direction, LocalDateTime startDate,
                        LocalDateTime endDate) {
                return transactionRepository.findByFilters(companyId, plantId, materialId,
                                transactionType, direction, startDate, endDate)
                                .stream()
                                .map(this::mapTransactionToResponse)
                                .collect(Collectors.toList());
        }

        // Helper methods

        private void recordTransaction(Inventory inventory, BigDecimal quantity, BigDecimal rate,
                        String transactionType, String direction, String referenceNumber,
                        Integer referenceId, String remarks, BigDecimal beforeBalance,
                        BigDecimal afterBalance) {

                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Employee employee = employeeRepository.findByEmail(username).orElse(null);

                InventoryTransaction transaction = InventoryTransaction.builder()
                                .inventory(inventory)
                                .material(inventory.getMaterial())
                                .plant(inventory.getPlant())
                                .company(inventory.getCompany())
                                .unitOfMeasure(inventory.getUnitOfMeasure())
                                .transactionType(transactionType)
                                .direction(direction)
                                .quantity(quantity)
                                .rate(rate)
                                .beforeBalance(beforeBalance)
                                .afterBalance(afterBalance)
                                .referenceType(transactionType)
                                .referenceNumber(referenceNumber)
                                .referenceId(referenceId)
                                .remarks(remarks)
                                .createdBy(employee)
                                .transactionDate(LocalDateTime.now())
                                .build();

                transactionRepository.save(transaction);
        }

        private InventoryResponse mapToResponse(Inventory inventory) {
                return InventoryResponse.builder()
                                .id(inventory.getId())
                                .materialId(inventory.getMaterial().getId())
                                .materialCode(inventory.getMaterial().getCode())
                                .materialName(inventory.getMaterial().getName())
                                .plantId(inventory.getPlant().getId())
                                .plantCode(inventory.getPlant().getCode())
                                .plantName(inventory.getPlant().getName())
                                .companyId(inventory.getCompany().getId())
                                .companyCode(inventory.getCompany().getCode())
                                .companyName(inventory.getCompany().getName())
                                .uomId(inventory.getUnitOfMeasure().getId())
                                .uomCode(inventory.getUnitOfMeasure().getCode())
                                .uomName(inventory.getUnitOfMeasure().getName())
                                .openingBalance(inventory.getOpeningBalance())
                                .currentBalance(inventory.getCurrentBalance())
                                .reservedQuantity(inventory.getReservedQuantity())
                                .availableQuantity(inventory.getAvailableQuantity())
                                .reorderLevel(inventory.getReorderLevel())
                                .maxLevel(inventory.getMaxLevel())
                                .minLevel(inventory.getMinLevel())
                                .avgRate(inventory.getAvgRate())
                                .totalValue(inventory.getTotalValue())
                                .lastReceiptDate(inventory.getLastReceiptDate())
                                .lastIssueDate(inventory.getLastIssueDate())
                                .lastUpdated(inventory.getLastUpdated())
                                .isLowStock(inventory.isBelowReorderLevel())
                                .isCriticalStock(inventory.isBelowMinLevel())
                                .isOverstock(inventory.isAboveMaxLevel())
                                .build();
        }

        private InventoryTransactionResponse mapTransactionToResponse(InventoryTransaction transaction) {
                return InventoryTransactionResponse.builder()
                                .id(transaction.getId())
                                .inventoryId(transaction.getInventory().getId())
                                .materialId(transaction.getMaterial().getId())
                                .materialCode(transaction.getMaterial().getCode())
                                .materialName(transaction.getMaterial().getName())
                                .plantId(transaction.getPlant().getId())
                                .plantCode(transaction.getPlant().getCode())
                                .plantName(transaction.getPlant().getName())
                                .companyId(transaction.getCompany().getId())
                                .companyCode(transaction.getCompany().getCode())
                                .uomCode(transaction.getUnitOfMeasure().getCode())
                                .transactionType(transaction.getTransactionType())
                                .direction(transaction.getDirection())
                                .quantity(transaction.getQuantity())
                                .rate(transaction.getRate())
                                .amount(transaction.getAmount())
                                .beforeBalance(transaction.getBeforeBalance())
                                .afterBalance(transaction.getAfterBalance())
                                .referenceType(transaction.getReferenceType())
                                .referenceNumber(transaction.getReferenceNumber())
                                .referenceId(transaction.getReferenceId())
                                .remarks(transaction.getRemarks())
                                .createdBy(transaction.getCreatedBy() != null
                                                ? transaction.getCreatedBy().getEmployeeNumber()
                                                : null)
                                .createdByName(transaction.getCreatedBy() != null
                                                ? transaction.getCreatedBy().getFullName()
                                                : null)
                                .transactionDate(transaction.getTransactionDate())
                                .createdAt(transaction.getCreatedAt())
                                .build();
        }

        /**
         * Custom exception for insufficient stock
         */
        public static class InsufficientStockException extends RuntimeException {
                public InsufficientStockException(String message) {
                        super(message);
                }
        }
}
