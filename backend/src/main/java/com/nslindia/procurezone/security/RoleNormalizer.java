package com.nslindia.procurezone.security;

import java.util.Map;

/**
 * Centralized role normalization.
 *
 * The database stores role_code values in tbl_roles_master as human-readable
 * strings (e.g., "Plant Manager", "Super Admin"). Spring Security @PreAuthorize
 * expressions require a single-word, UPPERCASE token. This class is the SINGLE
 * location that converts raw DB values to normalized role codes.
 *
 * Call site: AuthService.java — immediately after Role::getCode collection.
 * All other layers (JWT, UserPrincipal, @PreAuthorize, frontend) receive the
 * normalized form and never need to know the raw DB value.
 *
 * To add or change a mapping: edit the MAP constant below.
 */
public final class RoleNormalizer {

    private RoleNormalizer() {}

    /**
     * DB role_code → normalized role code used in @PreAuthorize and JWT.
     *
     * Key   = exact tbl_roles_master.role_code value (case-sensitive)
     * Value = normalized code (uppercase, no spaces)
     */
    private static final Map<String, String> MAP = Map.ofEntries(
            Map.entry("Super Admin",      "SUPERADMIN"),
            Map.entry("Admin",            "ADMIN"),
            Map.entry("User",             "USER"),
            Map.entry("Supervisor",       "SUPERVISOR"),
            Map.entry("Department Head",  "DEPTHEAD"),   // actual DB role_code (with space)
            Map.entry("Department",       "DEPTHEAD"),   // legacy alias — kept for safety
            Map.entry("Procurement",      "PROCUREMENT"),
            Map.entry("Plant Manager",    "PLANTMANAGER"),
            Map.entry("FloorIncharge",    "FLOORINCHARGE"),
            Map.entry("DataEntry",        "DATAENTRYOPERATOR"),
            Map.entry("GoodsIncharge",    "GOODSINCHARGE"),
            Map.entry("GRNIncharge",      "GRNINCHARGE"),
            Map.entry("IssueConfirm",     "ISSUECONFIRM"),
            Map.entry("ReceiptConfirm",   "RECEIPTCONFIRM"),
            Map.entry("QualityManager",   "QUALITYMANAGER")
    );

    /**
     * Normalize a raw DB role_code to the token used in @PreAuthorize.
     *
     * If the code is already normalized (not in the map) it is returned
     * uppercased without spaces so previously issued JWTs remain valid.
     *
     * @param rawCode raw value from tbl_roles_master.role_code
     * @return normalized role code
     */
    public static String normalize(String rawCode) {
        if (rawCode == null) return "UNKNOWN";
        String mapped = MAP.get(rawCode);
        if (mapped != null) return mapped;
        // Fallback: strip spaces and uppercase so unknown codes are still usable
        return rawCode.replaceAll("\\s+", "").toUpperCase();
    }
}
