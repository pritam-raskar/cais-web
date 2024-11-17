package com.dair.cais.filter.util;

import com.dair.cais.filter.domain.FilterRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import com.dair.cais.filter.domain.FilterConfig;
import com.dair.cais.filter.domain.FilterRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class FilterQueryBuilder {

    private static final String AND_OPERATOR = " AND ";
    private static final String OR_OPERATOR = " OR ";

    public String buildQuery(String baseQuery, FilterConfig filterConfig) {
        StringBuilder query = new StringBuilder(baseQuery);
        List<Object> parameters = new ArrayList<>();

        String whereClause = buildWhereClause(filterConfig, parameters);
        if (!whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }

        log.debug("Built query: {}", query);
        log.debug("With parameters: {}", parameters);

        return query.toString();
    }

    private String buildWhereClause(FilterConfig filterConfig, List<Object> parameters) {
//        if (filterConfig.getRules() == null || filterConfig.getRules().isEmpty()) {
//            return "";
//        }
//
//        List<String> conditions = new ArrayList<>();
//        for (FilterRule rule : filterConfig.getRules()) {
//            String condition = buildCondition(rule, parameters);
//            if (condition != null && !condition.isEmpty()) {
//                conditions.add(condition);
//            }
//        }
//
//        //String operator = filterConfig.getCombinator().equals("and") ? AND_OPERATOR : OR_OPERATOR;
//        //String whereClause = String.join(operator, conditions);
//
//        if (!conditions.isEmpty()) {
//            whereClause = "(" + whereClause + ")";
//            if (Boolean.TRUE.equals(filterConfig.getNot())) {
//                whereClause = "NOT " + whereClause;
//            }
//        }
//
//        return whereClause;
        return "";
    }

    private String buildCondition(FilterRule rule, List<Object> parameters) {
//        if (rule.getRules() != null && !rule.getRules().isEmpty()) {
//            // Handle nested rules
//            FilterConfig nestedConfig = new FilterConfig();
//            nestedConfig.setCombinator(rule.getCombinator());
//            nestedConfig.setRules(rule.getRules());
//            return buildWhereClause(nestedConfig, parameters);
//        }
//
//        // Handle leaf rule
//        String field = sanitizeFieldName(rule.getField());
//        String operator = rule.getOperator().getValue();
//
//        return switch (operator) {
//            case "=" -> buildEqualsCondition(field, rule.getValue(), parameters);
//            case ">" -> buildComparisonCondition(field, ">", rule.getValue(), parameters);
//            case ">=" -> buildComparisonCondition(field, ">=", rule.getValue(), parameters);
//            case "<" -> buildComparisonCondition(field, "<", rule.getValue(), parameters);
//            case "<=" -> buildComparisonCondition(field, "<=", rule.getValue(), parameters);
//            case "contains" -> buildLikeCondition(field, "%" + rule.getValue() + "%", parameters);
//            case "startsWith" -> buildLikeCondition(field, rule.getValue() + "%", parameters);
//            case "endsWith" -> buildLikeCondition(field, "%" + rule.getValue(), parameters);
//            case "in" -> buildInCondition(field, rule.getValue(), parameters);
//            case "between" -> buildBetweenCondition(field, rule.getValue(), parameters);
//            case "isNull" -> field + " IS NULL";
//            case "isNotNull" -> field + " IS NOT NULL";
//            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
//        };
        return "";
    }

    private String sanitizeFieldName(String fieldName) {
        // Basic SQL injection prevention
        if (!fieldName.matches("^[a-zA-Z0-9_\\.]+$")) {
            throw new IllegalArgumentException("Invalid field name: " + fieldName);
        }
        return fieldName;
    }

    private String buildEqualsCondition(String field, String value, List<Object> parameters) {
        parameters.add(value);
        return field + " = ?";
    }

    private String buildComparisonCondition(String field, String operator, String value, List<Object> parameters) {
        parameters.add(value);
        return field + " " + operator + " ?";
    }

    private String buildLikeCondition(String field, String value, List<Object> parameters) {
        parameters.add(value);
        return field + " ILIKE ?";
    }

    private String buildInCondition(String field, String value, List<Object> parameters) {
        String[] values = value.split(",");
        StringBuilder condition = new StringBuilder(field + " IN (");
        for (int i = 0; i < values.length; i++) {
            parameters.add(values[i].trim());
            condition.append(i == 0 ? "?" : ", ?");
        }
        condition.append(")");
        return condition.toString();
    }

    private String buildBetweenCondition(String field, String value, List<Object> parameters) {
        String[] values = value.split(",");
        if (values.length != 2) {
            throw new IllegalArgumentException("BETWEEN operator requires exactly two values");
        }
        parameters.add(values[0].trim());
        parameters.add(values[1].trim());
        return field + " BETWEEN ? AND ?";
    }
}
