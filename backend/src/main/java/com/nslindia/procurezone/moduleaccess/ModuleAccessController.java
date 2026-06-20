package com.nslindia.procurezone.moduleaccess;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nslindia.procurezone.moduleaccess.dto.EmpModuleEntry;
import com.nslindia.procurezone.moduleaccess.dto.ModuleResponse;
import com.nslindia.procurezone.moduleaccess.dto.UpdateModuleAccessRequest;

@RestController
@RequestMapping("/api/v1/module-access")
public class ModuleAccessController {

    private final ModuleAccessService moduleAccessService;

    public ModuleAccessController(ModuleAccessService moduleAccessService) {
        this.moduleAccessService = moduleAccessService;
    }

    /**
     * GET /api/v1/module-access/my-modules
     * Returns module codes the current user can access (used by frontend after login).
     */
    @GetMapping("/my-modules")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getMyModules() {
        return ResponseEntity.ok(moduleAccessService.getMyModuleCodes());
    }

    /**
     * GET /api/v1/module-access/modules
     * Returns all modules from tbl_module_master (active + future with flag).
     */
    @GetMapping("/modules")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<ModuleResponse>> getAllModules() {
        return ResponseEntity.ok(moduleAccessService.getAllModules());
    }

    /**
     * GET /api/v1/module-access/employee/{empNumber}
     * Returns module access list for a specific employee.
     * ADMIN and SUPERADMIN only.
     */
    @GetMapping("/employee/{empNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<EmpModuleEntry>> getEmployeeModules(@PathVariable Integer empNumber) {
        return ResponseEntity.ok(moduleAccessService.getEmployeeModules(empNumber));
    }

    /**
     * PUT /api/v1/module-access/employee/{empNumber}
     * Upserts per-employee module access.
     * SUPERADMIN can set any module; ADMIN cannot set ADMINISTRATION or AUDIT_LOGS.
     */
    @PutMapping("/employee/{empNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Void> updateEmployeeModules(
            @PathVariable Integer empNumber,
            @RequestBody UpdateModuleAccessRequest request) {
        moduleAccessService.updateEmployeeModules(empNumber, request);
        return ResponseEntity.noContent().build();
    }
}
