package com.nslindia.procurezone.issuenote;

import com.nslindia.procurezone.common.persistence.VarcharDateTimeConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Issue Note Entity
 * Maps to tbl_issue_note table
 * Represents materials issued from stores to departments/projects
 * Completes inventory cycle: GRN (In) → Issue Note (Out)
 * 
 * Status Workflow with RM Approval:
 * 1 = Created (draft)
 * 2 = Pending RM Approval (submitted)
 * 3 = RM Approved (pending Dept Head approval)
 * 4 = Approved by Manager
 * 5 = Rejected by RM
 * 6 = Rejected by Manager
 * 7 = Pending Store Issue
 * 8 = Issued (materials released)
 * 9 = Rejected by Stores (insufficient stock)
 * 10 = Returned (materials returned to stores)
 * 
 * @author NSL India
 * @version 2.0
 */
@Entity
@Table(name = "tbl_issue_note")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_note_id")
    private Integer id;

    @Column(name = "issue_note_no", nullable = false, unique = true, length = 100)
    private String issueNoteNumber;

    /**
     * MIGRATION FIX (Dec 2025): Legacy audit field for financial year tracking
     * Format: "YYYY-YY" (e.g., "2025-26")
     */
    @Column(name = "issue_note_year", length = 45)
    private String issueNoteYear;

    @Column(name = "issue_note_date", nullable = false)
    private LocalDateTime issueDate;

    // Optional geo metadata — captured server-side when available, else null; never blocks creation.
    @Column(name = "issue_note_company")
    private Integer companyId;

    @Column(name = "issue_note_dept")
    private Integer departmentId;

    @Column(name = "issue_note_sec")
    private Integer sectionId;

    @Column(name = "issue_note_plant")
    private Integer plantId;

    @Column(name = "issue_note_issued_to", length = 200)
    private String issuedTo;

    @Column(name = "issue_note_purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "issue_note_comments", columnDefinition = "TEXT")
    private String comments;

    /**
     * MIGRATION FIX (Dec 2025): Legacy audit field for creation date string
     */
    @Column(name = "issue_note_created_date", length = 20)
    private String issueNoteCreatedDate;

    /**
     * MIGRATION FIX (Dec 2025): Legacy audit field for creation remarks
     */
    @Column(name = "issue_note_created_remarks", columnDefinition = "MEDIUMTEXT")
    private String issueNoteCreatedRemarks;

    @Column(name = "issue_note_createdby", nullable = false)
    private Integer createdBy;

    // RM (Reporting Manager) Approval Fields
    @Column(name = "issue_note_rm_approvedby")
    private Integer rmApprovedBy;

    @Column(name = "issue_note_rm_approvedby_date", length = 20)
    @Convert(converter = VarcharDateTimeConverter.class)
    private LocalDateTime rmApprovedByDate;

    @Column(name = "issue_note_rm_approvedby_remarks", columnDefinition = "TEXT")
    private String rmApprovedRemarks;

    @Column(name = "issue_note_rm_status")
    private Integer rmApprovedStatus;

    @Column(name = "issue_note_approvedby")
    private Integer approvedBy;

    @Column(name = "issue_note_approvedby_date", length = 20)
    @Convert(converter = VarcharDateTimeConverter.class)
    private LocalDateTime approvedByDate;

    /**
     * MIGRATION FIX (Dec 2025): Updated column definition to match legacy
     * MEDIUMTEXT
     */
    @Column(name = "issue_note_approvedby_remarks", columnDefinition = "MEDIUMTEXT")
    private String approvedRemarks;

    @Column(name = "issue_note_storesby")
    private Integer storesBy;

    @Column(name = "issue_note_storesby_date", length = 20)
    @Convert(converter = VarcharDateTimeConverter.class)
    private LocalDateTime storesByDate;

    /**
     * MIGRATION FIX (Dec 2025): Legacy audit field for stores remarks
     */
    @Column(name = "issue_note_storesby_remarks", columnDefinition = "MEDIUMTEXT")
    private String storesByRemarks;

    // Supervisor bypass flag
    @Column(name = "issue_note_supervisor_bypass")
    private Boolean supervisorBypass;

    /**
     * Issue Note Status (Updated with RM approval):
     * 1 = Created (draft)
     * 2 = Pending RM Approval
     * 3 = RM Approved
     * 4 = Approved by Manager
     * 5 = Rejected by RM
     * 6 = Rejected by Manager
     * 7 = Pending Store Issue
     * 8 = Issued (materials released)
     * 9 = Rejected by Stores (insufficient stock)
     * 10 = Returned (materials returned to stores)
     */
    @Column(name = "issue_note_status", nullable = false)
    private Integer status;

    @Column(name = "issue_note_approved_status")
    private Integer approvedStatus;

    @Column(name = "issue_note_storesby_status")
    private Integer storesByStatus;

    @Column(name = "issue_note_lmd", nullable = false, length = 20)
    @Convert(converter = VarcharDateTimeConverter.class)
    private LocalDateTime lastModifiedDate;

    @Column(name = "issue_note_lmu", nullable = false)
    private Integer lastModifiedBy;

    @OneToMany(mappedBy = "issueNote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<IssueNoteDetails> details = new ArrayList<>();

    // Status helper methods
    public boolean canBeSubmitted() {
        return status != null && status == 1;
    }

    public boolean canBeRmApproved() {
        return status != null && status == 2 && !Boolean.TRUE.equals(supervisorBypass);
    }

    public boolean canBeRmRejected() {
        return status != null && status == 2 && !Boolean.TRUE.equals(supervisorBypass);
    }

    public boolean canBeApproved() {
        // Can be approved if RM approved OR if supervisor bypass is enabled
        return status != null && (status == 3 || (status == 2 && Boolean.TRUE.equals(supervisorBypass)));
    }

    public boolean canBeRejectedByManager() {
        return status != null && (status == 3 || (status == 2 && Boolean.TRUE.equals(supervisorBypass)));
    }

    public boolean canBeIssued() {
        return status != null && status == 3;
    }

    public boolean canBeRejectedByStores() {
        return status != null && status == 3;
    }

    public boolean canBeCancelled() {
        return status != null && (status == 1 || status == 2);
    }

    public boolean isRmApproved() {
        return status != null && status == 3;
    }

    public boolean isApproved() {
        return status != null && status == 4;
    }

    public boolean isIssued() {
        return status != null && status == 8;
    }

    public boolean isReturned() {
        return status != null && status == 10;
    }

    /**
     * Check if issued materials can be returned to stores
     */
    public boolean canBeReturned() {
        return status != null && status == 8; // Only issued notes can be returned
    }

    public boolean isRejected() {
        return status != null && (status == 5 || status == 6 || status == 9);
    }

    @PrePersist
    protected void onCreate() {
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
        if (lastModifiedDate == null) {
            lastModifiedDate = LocalDateTime.now();
        }
        if (status == null) {
            status = 1; // Created
        }
        if (supervisorBypass == null) {
            supervisorBypass = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }

    /**
     * Get status description
     */
    public String getStatusDescription() {
        return switch (status) {
            case 0 -> "Cancelled";
            case 1 -> "Draft";
            case 2 -> "Pending RM Approval";
            case 3 -> "Awaiting Stores Issue";
            case 5 -> "Rejected by RM";
            case 8 -> "Goods Issued";
            case 9 -> "Rejected by Stores";
            case 10 -> "Returned";
            default -> "Unknown";
        };
    }

    /**
     * Add detail to issue note
     */
    public void addDetail(IssueNoteDetails detail) {
        details.add(detail);
        detail.setIssueNote(this);
    }

    /**
     * Remove detail from issue note
     */
    public void removeDetail(IssueNoteDetails detail) {
        details.remove(detail);
        detail.setIssueNote(null);
    }
}
