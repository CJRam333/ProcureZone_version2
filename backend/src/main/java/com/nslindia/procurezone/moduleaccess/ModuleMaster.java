package com.nslindia.procurezone.moduleaccess;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_module_master")
public class ModuleMaster {

    @Id
    @Column(name = "module_code", length = 50)
    private String moduleCode;

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Column(name = "module_status", nullable = false)
    private boolean moduleStatus;

    @Column(name = "module_is_future", nullable = false)
    private boolean isFuture;

    @Column(name = "module_default_roles", length = 255)
    private String defaultRoles;

    public ModuleMaster() {}

    public String getModuleCode() { return moduleCode; }
    public String getModuleName() { return moduleName; }
    public boolean isModuleStatus() { return moduleStatus; }
    public boolean isFuture() { return isFuture; }
    public String getDefaultRoles() { return defaultRoles; }
}
