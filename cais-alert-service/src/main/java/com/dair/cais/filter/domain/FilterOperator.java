package com.dair.cais.filter.domain;

public enum FilterOperator {
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    CONTAINS("contains"),
    STARTS_WITH("startsWith"),
    ENDS_WITH("endsWith"),
    IN("in"),
    NOT_IN("notIn"),
    BETWEEN("between"),
    IS_NULL("isNull"),
    IS_NOT_NULL("isNotNull");

    private final String value;

    FilterOperator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FilterOperator fromValue(String value) {
        for (FilterOperator operator : FilterOperator.values()) {
            if (operator.value.equalsIgnoreCase(value)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("Unknown operator: " + value);
    }
}
