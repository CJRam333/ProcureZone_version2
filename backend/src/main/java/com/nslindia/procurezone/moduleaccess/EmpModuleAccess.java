package com.nslindia.procurezone.moduleaccess;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_map_emp_module_access")
public class EmpModuleAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_id")
    private Integer id;

    @Column(name = "access_emp", nullable = false)
    private Integer empNumber;

    @Column(name = "access_module", nullable = false, length = 50)
    private String moduleCode;

    @Column(name = "access_enabled", nullable = false)
    private boolean enabled;

    @Column(name = "access_granted_by", nullable = false)
    private Integer grantedBy;

    @Column(name = "access_granted_at")
    private LocalDateTime grantedAt;

    public EmpModuleAccess() {}

    public EmpModuleAccess(Integer empNumber, String moduleCode, boolean enabled, Integer grantedBy) {
        this.empNumber = empNumber;
        this.moduleCode = moduleCode;
        this.enabled = enabled;
        this.grantedBy = grantedBy;
        this.grantedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Integer getEmpNumber() { return empNumber; }
    public String getModuleCode() { return moduleCode; }
    public boolean isEnabled() { return enabled; }
    public Integer getGrantedBy() { return grantedBy; }
    public LocalDateTime getGrantedAt() { return grantedAt; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setGrantedBy(Integer grantedBy) { this.grantedBy = grantedBy; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
}
