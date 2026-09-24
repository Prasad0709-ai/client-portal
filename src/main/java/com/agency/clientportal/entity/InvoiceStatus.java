package com.agency.clientportal.entity;

public enum InvoiceStatus {
    DRAFT("Draft", "secondary"),
    SENT("Pending Payment", "warning"),
    PAID("Paid", "success"),
    OVERDUE("Overdue", "danger"),
    CANCELLED("Cancelled", "secondary");

    private final String displayName;
    private final String badgeType;

    InvoiceStatus(String displayName, String badgeType) {
        this.displayName = displayName;
        this.badgeType = badgeType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeType() {
        return badgeType;
    }
}
