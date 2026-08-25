package com.health.platform.doctor.enums;

public enum DoctorAuditStatus {

    PENDING_REVIEW("待审核"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    SUSPENDED("已暂停"),
    TERMINATED("已解约");

    private final String label;

    DoctorAuditStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
