package com.dair.cais.filter.service;

import com.dair.cais.filter.domain.FilterConfig;
import com.dair.cais.filter.domain.FilterRule;
import com.dair.cais.filter.exception.FilterValidationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FilterValidationService {
    public void validateFilterConfig(FilterConfig filterConfig) {
//        List<String> errors = new ArrayList<>();
//
//        if (filterConfig == null) {
//            errors.add("Filter configuration cannot be null");
//            throw new FilterValidationException("Filter validation failed", errors);
//        }
//
//        if (filterConfig.getCombinator() == null) {
//            errors.add("Filter combinator is required");
//        }
//
//        if (filterConfig.getRules() == null || filterConfig.getRules().isEmpty()) {
//            errors.add("At least one filter rule is required");
//        } else {
//            validateRules(filterConfig.getRules(), errors);
//        }
//
//        if (!errors.isEmpty()) {
//            throw new FilterValidationException("Filter validation failed", errors);
//        }
    }

    private void validateRules(List<FilterRule> rules, List<String> errors) {
//        for (FilterRule rule : rules) {
//            if (rule.getRules() != null && !rule.getRules().isEmpty()) {
//                // Nested rules
//                if (rule.getCombinator() == null) {
//                    errors.add("Combinator is required for nested rules");
//                }
//                validateRules(rule.getRules(), errors);
//            } else {
//                // Leaf rule
//                if (rule.getField() == null) {
//                    errors.add("Field is required");
//                }
//                if (rule.getOperator() == null) {
//                    errors.add("Operator is required");
//                }
//                if (rule.getValue() == null && !isNullableOperator(rule.getOperator().getValue())) {
//                    errors.add("Value is required for operator: " + rule.getOperator());
//                }
//            }
//        }
    }

    private boolean isNullableOperator(String operator) {
        //return "isNull".equals(operator) || "isNotNull".equals(operator);
        return true;
    }
}
