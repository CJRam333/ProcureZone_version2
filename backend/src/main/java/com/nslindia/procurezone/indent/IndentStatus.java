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
 * Status Reference (from database):
 * 1 = Draft - Initial creation
 * 2 = Submitted - Pending approval
 * 3 = Department Head Approved
 * 4 = Finance Approved
 * 5 = Procurement Approved (PO_CREATED)
 * 6 = Rejected
 * 7 = On Hold - MIGRATION FIX: Restored legacy HOLD status
 * 8 = Completed
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
