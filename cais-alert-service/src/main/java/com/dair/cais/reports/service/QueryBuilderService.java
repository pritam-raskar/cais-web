package com.dair.cais.reports.service;

import com.dair.cais.reports.dto.QueryFilterDto;
import com.dair.cais.reports.dto.QueryRuleDto;
import com.dair.cais.reports.exception.InvalidQueryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QueryBuilderService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    public String buildWhereClause(QueryFilterDto filter, List<Object> params) {
        if (filter == null || filter.getRules() == null || filter.getRules().isEmpty()) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder();
        String combinator = filter.getCombinator() != null ? filter.getCombinator().toLowerCase() : "and";

        List<String> conditions = new ArrayList<>();
        for (QueryRuleDto rule : filter.getRules()) {
            String condition = buildCondition(rule, params);
            if (condition != null && !condition.isEmpty()) {
                conditions.add(condition);
            }
        }

        if (!conditions.isEmpty()) {
            whereClause.append("(")
                    .append(String.join(" " + combinator + " ", conditions))
                    .append(")");

            if (Boolean.TRUE.equals(filter.getNot())) {
                whereClause.insert(0, "NOT ");
            }
        }

        return whereClause.toString();
    }

    private String buildCondition(QueryRuleDto rule, List<Object> params) {
        if (rule.getRules() != null && !rule.getRules().isEmpty()) {
            // Handle nested rules
            QueryFilterDto nestedFilter = new QueryFilterDto();
            nestedFilter.setCombinator(rule.getCombinator());
            nestedFilter.setRules(rule.getRules());
            return buildWhereClause(nestedFilter, params);
        }

        validateRule(rule);
        String field = sanitizeFieldName(rule.getField());
        String operator = rule.getOperator().toLowerCase();

        return switch (operator) {
            case "=" -> buildEqualsCondition(field, rule.getValue(), params);
            case ">" -> buildComparisonCondition(field, ">", rule.getValue(), params);
            case ">=" -> buildComparisonCondition(field, ">=", rule.getValue(), params);
            case "<" -> buildComparisonCondition(field, "<", rule.getValue(), params);
            case "<=" -> buildComparisonCondition(field, "<=", rule.getValue(), params);
            case "beginswith" -> buildLikeCondition(field, rule.getValue() + "%", params);
            case "endswith" -> buildLikeCondition(field, "%" + rule.getValue(), params);
            case "contains" -> buildLikeCondition(field, "%" + rule.getValue() + "%", params);
            case "in" -> buildInCondition(field, rule.getValue(), params);
            case "between" -> buildBetweenCondition(field, rule.getValue(), params);
            case "isnull" -> field + " IS NULL";
            case "isnotnull" -> field + " IS NOT NULL";
            default -> throw new InvalidQueryException("Unsupported operator: " + operator);
        };
    }

    private void validateRule(QueryRuleDto rule) {
        if (rule.getField() == null || rule.getOperator() == null) {
            throw new InvalidQueryException("Field and operator are required for query rules");
        }

        if (!Arrays.asList("isnull", "isnotnull").contains(rule.getOperator().toLowerCase())
                && rule.getValue() == null) {
            throw new InvalidQueryException("Value is required for operator: " + rule.getOperator());
        }
    }

    private String sanitizeFieldName(String field) {
        // Prevent SQL injection by validating field name
        if (!field.matches("^[a-zA-Z0-9_]+$")) {
            throw new InvalidQueryException("Invalid field name: " + field);
        }
        return "\"" + field + "\"";
    }

    private String buildEqualsCondition(String field, Object value, List<Object> params) {
        params.add(value);
        return field + " = ?";
    }

    private String buildComparisonCondition(String field, String operator, Object value, List<Object> params) {
        params.add(value);
        return field + " " + operator + " ?";
    }

    private String buildLikeCondition(String field, Object value, List<Object> params) {
        params.add(value);
        return field + " ILIKE ?";
    }

    private String buildInCondition(String field, Object value, List<Object> params) {
        if (!(value instanceof String)) {
            throw new InvalidQueryException("IN operator requires comma-separated string values");
        }

        List<String> values = Arrays.asList(((String) value).split(","))
                .stream()
                .map(String::trim)
                .collect(Collectors.toList());

        params.addAll(values);
        return field + " IN (" + values.stream().map(v -> "?").collect(Collectors.joining(",")) + ")";
    }

    private String buildBetweenCondition(String field, Object value, List<Object> params) {
        if (!(value instanceof String)) {
            throw new InvalidQueryException("BETWEEN operator requires comma-separated values");
        }

        String[] values = ((String) value).split(",");
        if (values.length != 2) {
            throw new InvalidQueryException("BETWEEN operator requires exactly two values");
        }

        try {
            LocalDate start = LocalDate.parse(values[0].trim(), DATE_FORMATTER);
            LocalDate end = LocalDate.parse(values[1].trim(), DATE_FORMATTER);
            params.add(start);
            params.add(end);
        } catch (Exception e) {
            log.error("Error parsing date values for BETWEEN condition", e);
            throw new InvalidQueryException("Invalid date format for BETWEEN operator");
        }

        return field + " BETWEEN ? AND ?";
    }
}