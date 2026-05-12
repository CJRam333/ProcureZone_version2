package com.nslindia.procurezone.po;

/**
 * Enum for Purchase Order Status
 * Defines all possible states in the PO lifecycle
 */
public enum POStatus {
    DRAFT(1, "Draft", "PO is in draft state, can be edited"),
    SUBMITTED(2, "Submitted", "PO submitted for approval"),
    APPROVED(3, "Approved", "PO approved and ready to be sent to vendor"),
    SENT_TO_VENDOR(4, "Sent to Vendor", "PO sent to vendor, awaiting delivery"),
    PARTIALLY_RECEIVED(5, "Partially Received", "Some items received, some pending"),
    FULLY_RECEIVED(6, "Fully Received", "All items received"),
    CANCELLED(7, "Cancelled", "PO cancelled"),
    CLOSED(8, "Closed", "PO closed after completion");

    private final int code;
    private final String displayName;
    private final String description;

    POStatus(int code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static POStatus fromCode(int code) {
        for (POStatus status : POStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PO status code: " + code);
    }

    public static String getDisplayName(int code) {
        try {
            return fromCode(code).displayName;
        } catch (IllegalArgumentException e) {
            return "Unknown";
        }
    }

    // Workflow validation methods
    public boolean canTransitionTo(POStatus targetStatus) {
        return switch (this) {
            case DRAFT -> targetStatus == SUBMITTED || targetStatus == CANCELLED;
            case SUBMITTED -> targetStatus == APPROVED || targetStatus == DRAFT || targetStatus == CANCELLED;
            case APPROVED -> targetStatus == SENT_TO_VENDOR || targetStatus == CANCELLED;
            case SENT_TO_VENDOR ->
                targetStatus == PARTIALLY_RECEIVED || targetStatus == FULLY_RECEIVED || targetStatus == CANCELLED;
            case PARTIALLY_RECEIVED -> targetStatus == FULLY_RECEIVED || targetStatus == CANCELLED;
            case FULLY_RECEIVED -> targetStatus == CLOSED;
            case CANCELLED, CLOSED -> false; // Terminal states
        };
    }

    public boolean isEditable() {
        return this == DRAFT;
    }

    public boolean canBeApproved() {
        return this == SUBMITTED;
    }

    public boolean canBeSentToVendor() {
        return this == APPROVED;
    }

    public boolean canReceiveGoods() {
        return this == SENT_TO_VENDOR || this == PARTIALLY_RECEIVED;
    }

    public boolean canBeCancelled() {
        return this != CANCELLED && this != CLOSED && this != FULLY_RECEIVED;
    }

    public boolean isTerminal() {
        return this == CANCELLED || this == CLOSED;
    }
}
