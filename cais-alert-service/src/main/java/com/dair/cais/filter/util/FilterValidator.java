package com.dair.cais.filter.util;

import com.dair.cais.filter.domain.FilterRule;
import com.dair.cais.filter.exception.FilterValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FilterValidator {
    private static final int MAX_NESTED_DEPTH = 5;
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public void validateFieldNames(List<FilterRule> rules) {
        List<String> errors = new ArrayList<>();
        validateFieldNamesRecursive(rules, errors, 0);

        if (!errors.isEmpty()) {
            throw new FilterValidationException("Invalid field names", errors);
        }
    }

    private void validateFieldNamesRecursive(List<FilterRule> rules, List<String> errors, int depth) {
        if (depth > MAX_NESTED_DEPTH) {
            errors.add("Maximum nesting depth exceeded");
            // File: com/dair/cais/filter/util/FilterValidator.java (continued)
            return;
        }

        for (FilterRule rule : rules) {
            if (rule.getRules() != null && !rule.getRules().isEmpty()) {
                validateFieldNamesRecursive(rule.getRules(), errors, depth + 1);
            } else if (rule.getField() != null) {
                if (!FIELD_NAME_PATTERN.matcher(rule.getField()).matches()) {
                    errors.add("Invalid field name: " + rule.getField());
                }
            }
        }
    }

    public void validateOperatorValue(FilterRule rule) {
        if (rule.getOperator() == null) {
            return;
        }

        switch (rule.getOperator()) {
            case BETWEEN:
                validateBetweenValue(rule);
                break;
            case IN:
            case NOT_IN:
                validateInValue(rule);
                break;
            case IS_NULL:
            case IS_NOT_NULL:
                validateNullOperator(rule);
                break;
            default:
                validateStandardValue(rule);
        }
    }

    private void validateBetweenValue(FilterRule rule) {
        if (rule.getValue() == null || !rule.getValue().contains(",")) {
            throw new FilterValidationException("BETWEEN operator requires two comma-separated values",
                    List.of("Invalid BETWEEN value format"));
        }
        String[] values = rule.getValue().split(",");
        if (values.length != 2) {
            throw new FilterValidationException("BETWEEN operator requires exactly two values",
                    List.of("Invalid number of values for BETWEEN operator"));
        }
    }

    private void validateInValue(FilterRule rule) {
        if (rule.getValue() == null || rule.getValue().trim().isEmpty()) {
            throw new FilterValidationException("IN/NOT_IN operator requires comma-separated values",
                    List.of("Invalid IN/NOT_IN value format"));
        }
    }

    private void validateNullOperator(FilterRule rule) {
        if (rule.getValue() != null) {
            throw new FilterValidationException("IS_NULL/IS_NOT_NULL operators should not have a value",
                    List.of("Value not allowed for IS_NULL/IS_NOT_NULL operators"));
        }
    }

    private void validateStandardValue(FilterRule rule) {
        if (rule.getValue() == null || rule.getValue().trim().isEmpty()) {
            throw new FilterValidationException("Value is required for operator: " + rule.getOperator(),
                    List.of("Missing value for operator: " + rule.getOperator()));
        }
    }
}


