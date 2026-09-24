package com.agency.clientportal.entity;

public enum DeliverableStatus {
    PENDING_REVIEW("Pending Review", "warning"),
    APPROVED("Approved", "success"),
    CHANGES_REQUESTED("Changes Requested", "danger");

    private final String displayName;
    private final String badgeType;

    DeliverableStatus(String displayName, String badgeType) {
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
