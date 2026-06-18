package com.nslindia.procurezone.indent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing indent status values.
 * Maps to tbl_indent_status table.
 * 
 * Status Reference (aligned with legacy tbl_indent_status and IndentService transitions):
 * 1 = Draft              — createIndent()
 * 2 = Submitted          — submitIndent()
 * 3 = Dept Head Approved — approveIndent() / l2Approve()
 * 4 = Rejected           — l1Reject() / l2Reject() / rejectIndent() / cancelIndent()
 *                          (legacy DB ID 4 = REJECTED — preserved for compatibility)
 * 5 = Proc. In Progress  — finalApproveIndent()
 * 6 = PO Created         — procurementApproveIndent()
 *                          (legacy DB ID 6 = PO_CREATED — preserved for compatibility)
 * 7 = On Hold            — holdIndent()
 * 8 = Completed          — completeIndent()
 * 
 * @author NSL India
 * @version 2.0
 */
@Entity
@Table(name = "tbl_indent_status")
public class IndentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indent_status_id")
    private Integer id;

    @Column(name = "indent_status_name", nullable = false, length = 100)
    private String name;

    protected IndentStatus() {
    }

    // Getters

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
