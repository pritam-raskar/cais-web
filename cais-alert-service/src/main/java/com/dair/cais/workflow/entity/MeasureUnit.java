package com.dair.cais.workflow.entity;

public enum MeasureUnit {
    DAYS,
    WEEKS,
    MONTHS;

    public static boolean isValid(String measure) {
        try {
            valueOf(measure.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
