package com.nslindia.procurezone.moduleaccess.dto;

public record EmpModuleEntry(
        String moduleCode,
        String moduleName,
        boolean enabled,
        boolean isFuture,
        boolean customOverride) {
}
