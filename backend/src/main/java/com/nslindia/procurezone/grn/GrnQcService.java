package com.nslindia.procurezone.grn;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.grn.dto.CreateQcResultRequest;
import com.nslindia.procurezone.grn.dto.QcResultResponse;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for GRN Quality Control operations.
 * 
 * Handles:
 * - QC inspection recording
 * - Pass/Fail determination
 * - Re-inspection tracking
 * - QC reports
 * 
 * @author NSL India
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GrnQcService {

    private static final String ENTITY_TYPE = "GRN QC Result";

    private final GrnQcResultRepository qcRepository;
    private final GoodsReceiptRepository grnRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    /**
     * Create a new QC inspection result
     */
    public QcResultResponse createQcResult(CreateQcResultRequest request, Integer inspectorId) {
        log.info("Creating QC result for GRN ID: {} by inspector: {}", request.grnId(), inspectorId);

        // Validate GRN exists
        GoodsReceipt grn = grnRepository.findById(request.grnId())
                .orElseThrow(() -> new ResourceNotFoundException("GRN not found with ID: " + request.grnId()));

        GrnQcResult qc = GrnQcResult.builder()
                .grnId(request.grnId())
                .inspectorId(inspectorId)
                .inspectionDate(LocalDateTime.now())
                .qcStatus(request.qcStatus() != null ? request.qcStatus() : 1)
                // Physical parameters
                .moisture(request.moisture())
                .pureSeed(request.pureSeed())
                .inertMatter(request.inertMatter())
                .ocsCount(request.ocsCount())
                .weedSeedCount(request.weedSeedCount())
                .grain(request.grain())
                .blackSeeds(request.blackSeeds())
                .pinholeSeed(request.pinholeSeed())
                .bulkDensity(request.bulkDensity())
                .thsw(request.thsw())
                // Germination
                .coldVigourGermNormal(request.coldVigourGermNormal())
                .firstCountNormal(request.firstCountNormal())
                .germNormal(request.germNormal())
                .fetNormal(request.fetNormal())
                .soilCountDays(request.soilCountDays())
                .aavGermNormal(request.aavGermNormal())
                // GOT
                .got(request.got())
                .gotGp(request.gotGp())
                .gotFemale(request.gotFemale())
                .gotOthers(request.gotOthers())
                // Trait markers
                .bg1(request.bg1())
                .bg2(request.bg2())
                .ht(request.ht())
                .fqr(request.fqr())
                // Disease/Pest
                .elisa(request.elisa())
                .stl(request.stl())
                .odv(request.odv())
                .odvRes(request.odvRes())
                // Additional QC
                .q1(request.q1())
                .q2(request.q2())
                .q3(request.q3())
                .q4(request.q4())
                .q5(request.q5())
                .q6(request.q6())
                .q7(request.q7())
                .q8(request.q8())
                .q9(request.q9())
                // Assessment
                .overallRemarks(request.overallRemarks())
                .passFailRemarks(request.passFailRemarks())
                .correctiveAction(request.correctiveAction())
                .reInspectionRequired(request.reInspectionRequired())
                .createdBy(inspectorId)
                .build();

        qc = qcRepository.save(qc);

        auditService.logEntityChange("QC_RESULT_CREATED", ENTITY_TYPE, qc.getId(),
                inspectorId, "User " + inspectorId,
                String.format("QC result created for GRN %s with status %s",
                        grn.getGrnNumber(), QcResultResponse.getStatusName(qc.getQcStatus())));

        log.info("QC result created: ID={}, GRN={}, Status={}",
                qc.getId(), grn.getGrnNumber(), qc.getQcStatus());

        return mapToResponse(qc, grn.getGrnNumber(), getInspectorName(inspectorId));
    }

    /**
     * Get QC result by ID
     */
    @Transactional(readOnly = true)
    public QcResultResponse getQcResultById(Integer id) {
        GrnQcResult qc = qcRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QC result not found with ID: " + id));

        GoodsReceipt grn = grnRepository.findById(qc.getGrnId()).orElse(null);
        String grnNumber = grn != null ? grn.getGrnNumber() : null;

        return mapToResponse(qc, grnNumber, getInspectorName(qc.getInspectorId()));
    }

    /**
     * Get all QC results for a GRN
     */
    @Transactional(readOnly = true)
    public List<QcResultResponse> getQcResultsForGrn(Integer grnId) {
        GoodsReceipt grn = grnRepository.findById(grnId)
                .orElseThrow(() -> new ResourceNotFoundException("GRN not found with ID: " + grnId));

        List<GrnQcResult> results = qcRepository.findByGrnIdOrderByInspectionDateDesc(grnId);

        return results.stream()
                .map(qc -> mapToResponse(qc, grn.getGrnNumber(), getInspectorName(qc.getInspectorId())))
                .collect(Collectors.toList());
    }

    /**
     * Get latest QC result for a GRN
     */
    @Transactional(readOnly = true)
    public Optional<QcResultResponse> getLatestQcResultForGrn(Integer grnId) {
        return qcRepository.findFirstByGrnIdOrderByInspectionDateDesc(grnId)
                .map(qc -> {
                    GoodsReceipt grn = grnRepository.findById(grnId).orElse(null);
                    String grnNumber = grn != null ? grn.getGrnNumber() : null;
                    return mapToResponse(qc, grnNumber, getInspectorName(qc.getInspectorId()));
                });
    }

    /**
     * Get QC results by status
     */
    @Transactional(readOnly = true)
    public Page<QcResultResponse> getQcResultsByStatus(Integer status, Pageable pageable) {
        return qcRepository.findByQcStatus(status, pageable)
                .map(qc -> {
                    GoodsReceipt grn = grnRepository.findById(qc.getGrnId()).orElse(null);
                    String grnNumber = grn != null ? grn.getGrnNumber() : null;
                    return mapToResponse(qc, grnNumber, getInspectorName(qc.getInspectorId()));
                });
    }

    /**
     * Update QC status (Pass/Fail)
     */
    public QcResultResponse updateQcStatus(Integer qcId, Integer newStatus, String remarks, Integer userId) {
        log.info("Updating QC status: ID={}, newStatus={}", qcId, newStatus);

        GrnQcResult qc = qcRepository.findById(qcId)
                .orElseThrow(() -> new ResourceNotFoundException("QC result not found with ID: " + qcId));

        int oldStatus = qc.getQcStatus();
        qc.setQcStatus(newStatus);
        qc.setPassFailRemarks(remarks);
        qc.setUpdatedBy(userId);

        // If failed, may require re-inspection
        if (newStatus == GrnQcResult.STATUS_FAILED) {
            qc.setReInspectionRequired(true);
        }

        qc = qcRepository.save(qc);

        GoodsReceipt grn = grnRepository.findById(qc.getGrnId()).orElse(null);
        String grnNumber = grn != null ? grn.getGrnNumber() : null;

        auditService.logEntityChange("QC_STATUS_UPDATED", ENTITY_TYPE, qc.getId(),
                userId, "User " + userId,
                String.format("QC status changed from %s to %s for GRN %s",
                        QcResultResponse.getStatusName(oldStatus),
                        QcResultResponse.getStatusName(newStatus), grnNumber));

        return mapToResponse(qc, grnNumber, getInspectorName(qc.getInspectorId()));
    }

    /**
     * Check if GRN has passed QC
     */
    @Transactional(readOnly = true)
    public boolean hasGrnPassedQc(Integer grnId) {
        return qcRepository.hasPassedQc(grnId);
    }

    /**
     * Get QC statistics for dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getQcStatistics() {
        return Map.of(
                "pending", qcRepository.countByQcStatus(GrnQcResult.STATUS_PENDING),
                "passed", qcRepository.countByQcStatus(GrnQcResult.STATUS_PASSED),
                "failed", qcRepository.countByQcStatus(GrnQcResult.STATUS_FAILED),
                "conditional", qcRepository.countByQcStatus(GrnQcResult.STATUS_CONDITIONAL));
    }

    /**
     * Get pending re-inspections
     */
    @Transactional(readOnly = true)
    public List<QcResultResponse> getPendingReInspections() {
        List<GrnQcResult> results = qcRepository.findPendingReInspections(LocalDateTime.now());

        return results.stream()
                .map(qc -> {
                    GoodsReceipt grn = grnRepository.findById(qc.getGrnId()).orElse(null);
                    String grnNumber = grn != null ? grn.getGrnNumber() : null;
                    return mapToResponse(qc, grnNumber, getInspectorName(qc.getInspectorId()));
                })
                .collect(Collectors.toList());
    }

    // ========== HELPER METHODS ==========

    private String getInspectorName(Integer inspectorId) {
        if (inspectorId == null)
            return null;
        return employeeRepository.findById(inspectorId)
                .map(Employee::getFullName)
                .orElse("Unknown");
    }

    private QcResultResponse mapToResponse(GrnQcResult qc, String grnNumber, String inspectorName) {
        return new QcResultResponse(
                qc.getId(),
                qc.getGrnId(),
                grnNumber,
                qc.getInspectorId(),
                inspectorName,
                qc.getInspectionDate(),
                qc.getQcStatus(),
                QcResultResponse.getStatusName(qc.getQcStatus()),
                // Physical
                qc.getMoisture(),
                qc.getPureSeed(),
                qc.getInertMatter(),
                qc.getOcsCount(),
                qc.getWeedSeedCount(),
                qc.getGrain(),
                qc.getBlackSeeds(),
                qc.getPinholeSeed(),
                qc.getBulkDensity(),
                qc.getThsw(),
                // Germination
                qc.getColdVigourGermNormal(),
                qc.getFirstCountNormal(),
                qc.getGermNormal(),
                qc.getFetNormal(),
                qc.getSoilCountDays(),
                qc.getAavGermNormal(),
                // GOT
                qc.getGot(),
                qc.getGotGp(),
                qc.getGotFemale(),
                qc.getGotOthers(),
                // Trait markers
                qc.getBg1(),
                qc.getBg2(),
                qc.getHt(),
                qc.getFqr(),
                // Disease
                qc.getElisa(),
                qc.getStl(),
                qc.getOdv(),
                qc.getOdvRes(),
                // Additional
                qc.getQ1(),
                qc.getQ2(),
                qc.getQ3(),
                qc.getQ4(),
                qc.getQ5(),
                qc.getQ6(),
                qc.getQ7(),
                qc.getQ8(),
                qc.getQ9(),
                // Assessment
                qc.getOverallRemarks(),
                qc.getPassFailRemarks(),
                qc.getCorrectiveAction(),
                qc.getReInspectionRequired(),
                qc.getReInspectionDate(),
                // Audit
                qc.getCreatedDate(),
                qc.getUpdatedDate());
    }
}
