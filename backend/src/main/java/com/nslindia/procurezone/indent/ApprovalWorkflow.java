package com.nslindia.procurezone.indent;

import java.time.LocalDateTime;

import com.nslindia.procurezone.identity.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity representing approval workflow history for indents.
 * Tracks all approval/rejection actions on an indent.
 */
@Entity
@Table(name = "tbl_approval_workflow")
public class ApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workflow_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_id", nullable = false)
    private Indent indent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_emp_number", nullable = false)
    private Employee approver;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // SUBMITTED, APPROVED, REJECTED, INFO_REQUESTED

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "level", nullable = false)
    private Integer level; // 1 = Dept Head, 2 = Final Approver, etc.

    @Column(name = "info_requested", columnDefinition = "TEXT")
    private String infoRequested;

    protected ApprovalWorkflow() {
    }

    // Getters and setters

    public Integer getId() {
        return id;
    }

    public Indent getIndent() {
        return indent;
    }

    public void setIndent(Indent indent) {
        this.indent = indent;
    }

    public Employee getApprover() {
        return approver;
    }

    public void setApprover(Employee approver) {
        this.approver = approver;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getInfoRequested() {
        return infoRequested;
    }

    public void setInfoRequested(String infoRequested) {
        this.infoRequested = infoRequested;
    }
}
