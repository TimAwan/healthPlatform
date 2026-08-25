package com.health.platform.doctor.enums;

public enum DoctorAction {

    APPROVE("审核通过"),
    REJECT("审核拒绝"),
    SUSPEND("暂停合作"),
    TERMINATE("解约"),
    RESUBMIT("重新提交审核");

    private final String label;

    DoctorAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
