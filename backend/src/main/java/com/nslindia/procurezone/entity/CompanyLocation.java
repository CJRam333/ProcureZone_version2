package com.nslindia.procurezone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing the mapping between companies and locations.
 * Maps to tbl_map_company_location in the database.
 * 
 * This mapping allows multiple locations to be associated with a single company
 * and enables location-specific operations and access control.
 */
@Entity
@Table(name = "tbl_map_company_location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer id;

    @Column(name = "map_comp", nullable = false)
    private Integer companyId;

    @Column(name = "map_loc", nullable = false)
    private Integer locationId;

    @Column(name = "map_status", nullable = false)
    private Integer status;

    @Column(name = "map_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "map_lmu", nullable = false)
    private Integer lastModifiedBy;

    /**
     * Check if this mapping is active
     * 
     * @return true if status = 1
     */
    public boolean isActive() {
        return this.status != null && this.status == 1;
    }

    /**
     * Get status text
     * 
     * @return "Active" or "Inactive"
     */
    public String getStatusText() {
        return isActive() ? "Active" : "Inactive";
    }

    /**
     * Activate this mapping
     */
    public void activate() {
        this.status = 1;
    }

    /**
     * Deactivate this mapping
     */
    public void deactivate() {
        this.status = 0;
    }
}
