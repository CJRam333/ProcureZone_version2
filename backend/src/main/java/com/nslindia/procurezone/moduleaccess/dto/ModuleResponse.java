package com.nslindia.procurezone.moduleaccess.dto;

public record ModuleResponse(
        String moduleCode,
        String moduleName,
        boolean active,
        boolean isFuture,
        String defaultRoles) {
}
