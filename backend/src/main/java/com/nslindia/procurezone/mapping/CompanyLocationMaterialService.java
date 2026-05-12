package com.nslindia.procurezone.mapping;

import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.mapping.dto.CompanyLocationMaterialRequest;
import com.nslindia.procurezone.mapping.dto.CompanyLocationMaterialResponse;
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Location;
import com.nslindia.procurezone.masterdata.LocationRepository;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyLocationMaterialService {

        private final CompanyLocationMaterialMapRepository repository;
        private final CompanyRepository companyRepository;
        private final LocationRepository locationRepository;
        private final MaterialRepository materialRepository;

        @Transactional
        public CompanyLocationMaterialResponse createMapping(CompanyLocationMaterialRequest request, Integer userId) {
                Company company = companyRepository.findById(request.companyId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Company not found with id: " + request.companyId()));
                Location location = locationRepository.findById(request.locationId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id: " + request.locationId()));
                Material material = materialRepository.findById(request.materialId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Material not found with id: " + request.materialId()));

                repository.findByCompanyAndLocationAndMaterial(
                                request.companyId(), request.locationId(), request.materialId()).ifPresent(existing -> {
                                        throw new IllegalStateException("Mapping already exists");
                                });

                CompanyLocationMaterialMap mapping = CompanyLocationMaterialMap.builder()
                                .company(company)
                                .location(location)
                                .material(material)
                                .isActive(true)
                                .minStockLevel(request.minStockLevel())
                                .maxStockLevel(request.maxStockLevel())
                                .reorderLevel(request.reorderLevel())
                                .reorderQuantity(request.reorderQuantity())
                                .remarks(request.remarks())
                                .createdBy(userId)
                                .createdDate(LocalDateTime.now())
                                .lastModifiedBy(userId)
                                .lastModifiedDate(LocalDateTime.now())
                                .build();

                return CompanyLocationMaterialResponse.from(repository.save(mapping));
        }

        @Transactional
        public CompanyLocationMaterialResponse updateMapping(Integer id, CompanyLocationMaterialRequest request,
                        Integer userId) {
                CompanyLocationMaterialMap mapping = repository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id));

                if (request.minStockLevel() != null)
                        mapping.setMinStockLevel(request.minStockLevel());
                if (request.maxStockLevel() != null)
                        mapping.setMaxStockLevel(request.maxStockLevel());
                if (request.reorderLevel() != null)
                        mapping.setReorderLevel(request.reorderLevel());
                if (request.reorderQuantity() != null)
                        mapping.setReorderQuantity(request.reorderQuantity());
                if (request.remarks() != null)
                        mapping.setRemarks(request.remarks());

                mapping.setLastModifiedBy(userId);
                mapping.setLastModifiedDate(LocalDateTime.now());

                return CompanyLocationMaterialResponse.from(repository.save(mapping));
        }

        @Transactional
        public void deactivateMapping(Integer id, Integer userId) {
                CompanyLocationMaterialMap mapping = repository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id));
                mapping.setIsActive(false);
                mapping.setLastModifiedBy(userId);
                mapping.setLastModifiedDate(LocalDateTime.now());
                repository.save(mapping);
        }

        @Transactional(readOnly = true)
        public org.springframework.data.domain.Page<CompanyLocationMaterialResponse> getAllMappings(org.springframework.data.domain.Pageable pageable) {
                return repository.findByIsActiveTrue(pageable)
                                .map(CompanyLocationMaterialResponse::from);
        }

        @Transactional(readOnly = true)
        public List<CompanyLocationMaterialResponse> getActiveMappingsByLocation(Integer locationId) {
                return repository.findActiveByLocation(locationId).stream()
                                .map(CompanyLocationMaterialResponse::from)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<CompanyLocationMaterialResponse> getActiveMappingsByCompany(Integer companyId) {
                return repository.findActiveByCompany(companyId).stream()
                                .map(CompanyLocationMaterialResponse::from)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<CompanyLocationMaterialResponse> getMaterialsByCompanyAndLocation(Integer companyId,
                        Integer locationId) {
                return repository.findMaterialsByCompanyAndLocation(companyId, locationId).stream()
                                .map(CompanyLocationMaterialResponse::from)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public boolean isMaterialAvailable(Integer companyId, Integer locationId, Integer materialId) {
                return repository.isMaterialAvailableAtLocation(companyId, locationId, materialId);
        }

        @Transactional(readOnly = true)
        public List<CompanyLocationMaterialResponse> getMaterialsForReorder(Integer locationId) {
                return repository.findMaterialsForReorder(locationId).stream()
                                .map(CompanyLocationMaterialResponse::from)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public CompanyLocationMaterialResponse getMappingById(Integer id) {
                return CompanyLocationMaterialResponse.from(repository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id)));
        }
}
