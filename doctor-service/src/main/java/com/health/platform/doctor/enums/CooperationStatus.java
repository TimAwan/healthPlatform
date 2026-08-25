package com.health.platform.doctor.enums;

public enum CooperationStatus {

    ACTIVE("合作中"),
    TERMINATED("已终止");

    private final String label;

    CooperationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
