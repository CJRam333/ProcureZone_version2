package com.nslindia.procurezone.moduleaccess.dto;

import java.util.List;

public record UpdateModuleAccessRequest(List<ModuleUpdate> moduleCodes) {

    public record ModuleUpdate(String code, boolean enabled) {}
}
