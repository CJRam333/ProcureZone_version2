package com.nslindia.procurezone.document.controller;

import com.nslindia.procurezone.document.service.IndentPdfService;
import com.nslindia.procurezone.document.service.PurchaseOrderPdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Document Generation Controller
 * Provides endpoints for generating PDF documents (PO, Indent, GRN, Issue
 * Slips, etc.)
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final PurchaseOrderPdfService purchaseOrderPdfService;
    private final IndentPdfService indentPdfService;

    // ============================================================================
    // PURCHASE ORDER PDFs
    // ============================================================================

    /**
     * Generate Purchase Order PDF
     * GET /api/v1/documents/po/{id}/pdf
     */
    @GetMapping("/po/{id}/pdf")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'PLANTMANAGER', 'DEPTHEAD')")
    public ResponseEntity<byte[]> generatePOPdf(@PathVariable Long id) {
        log.info("Request to generate PO PDF for ID: {}", id);

        byte[] pdfBytes = purchaseOrderPdfService.generatePOPdf(id);

        String filename = String.format("PO_%d.pdf", id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Preview Purchase Order PDF (inline display)
     * GET /api/v1/documents/po/{id}/preview
     */
    @GetMapping("/po/{id}/preview")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'PLANTMANAGER', 'DEPTHEAD', 'VIEWER')")
    public ResponseEntity<byte[]> previewPOPdf(@PathVariable Long id) {
        log.info("Request to preview PO PDF for ID: {}", id);

        byte[] pdfBytes = purchaseOrderPdfService.generatePOPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ============================================================================
    // INDENT PDFs
    // ============================================================================

    /**
     * Generate Indent PDF
     * GET /api/v1/documents/indent/{id}/pdf
     */
    @GetMapping("/indent/{id}/pdf")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'PLANTMANAGER', 'EMPLOYEE', 'VIEWER')")
    public ResponseEntity<byte[]> generateIndentPdf(@PathVariable Long id) {
        log.info("Request to generate Indent PDF for ID: {}", id);

        byte[] pdfBytes = indentPdfService.generateIndentPdf(id);

        String filename = String.format("Indent_%d.pdf", id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Preview Indent PDF (inline display)
     * GET /api/v1/documents/indent/{id}/preview
     */
    @GetMapping("/indent/{id}/preview")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'PLANTMANAGER', 'EMPLOYEE', 'VIEWER')")
    public ResponseEntity<byte[]> previewIndentPdf(@PathVariable Long id) {
        log.info("Request to preview Indent PDF for ID: {}", id);

        byte[] pdfBytes = indentPdfService.generateIndentPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ============================================================================
    // TODO: GRN & ISSUE NOTE PDFs (Next Phase)
    // ============================================================================

    // TODO: Add GRN receipt generation
    // TODO: Add Issue Note slip generation
}
