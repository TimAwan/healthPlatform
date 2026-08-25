package com.health.platform.doctor.enums;

public enum ShelfStatus {

    ON_SHELF("上架"),
    OFF_SHELF("下架");

    private final String label;

    ShelfStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
