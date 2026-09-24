package com.agency.clientportal.entity;

public enum ProjectStatus {
    PLANNING("Planning", "bg-purple-100 text-purple-800"),
    IN_PROGRESS("In Progress", "bg-blue-100 text-blue-800"),
    REVIEW("In Review", "bg-amber-100 text-amber-800"),
    COMPLETED("Completed", "bg-emerald-100 text-emerald-800"),
    ON_HOLD("On Hold", "bg-rose-100 text-rose-800");

    private final String displayName;
    private final String badgeClass;

    ProjectStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
