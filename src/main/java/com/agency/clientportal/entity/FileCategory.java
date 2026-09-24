package com.agency.clientportal.entity;

public enum FileCategory {
    CONTRACT("Contract & SOW"),
    DESIGN_SPEC("Design & Specifications"),
    DELIVERABLE_ASSET("Deliverable Asset"),
    BRAND_IDENTITY("Brand & Style Guide"),
    BRIEF_REQUIREMENT("Project Brief"),
    OTHER("General Document");

    private final String displayName;

    FileCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
