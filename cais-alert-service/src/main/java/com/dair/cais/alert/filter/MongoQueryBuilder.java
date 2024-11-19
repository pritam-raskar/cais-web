package com.dair.cais.alert.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MongoQueryBuilder {

    public Criteria buildCriteria(FilterCriteria filterCriteria) {
        if (filterCriteria == null || filterCriteria.getRules() == null || filterCriteria.getRules().isEmpty()) {
            log.debug("No filter criteria provided, returning empty criteria");
            return new Criteria();
        }

        Criteria criteria = processCriteriaGroup(filterCriteria.getRules(), filterCriteria.getCombinator());
        return filterCriteria.getNot() != null && filterCriteria.getNot() ? criteria.not() : criteria;
    }

    private Criteria processCriteriaGroup(List<FilterRule> rules, String combinator) {
        List<Criteria> criteriaList = new ArrayList<>();

        for (FilterRule rule : rules) {
            if (rule.isComposite()) {
                criteriaList.add(processCriteriaGroup(rule.getRules(), rule.getCombinator()));
            } else {
                criteriaList.add(buildSingleCriteria(rule));
            }
        }

        return combineCriteria(criteriaList, combinator);
    }

    private Criteria buildSingleCriteria(FilterRule rule) {
        String field = convertFieldName(rule.getField());
        String value = rule.getValue();

        try {
            return switch (rule.getOperator()) {
                case "=" -> Criteria.where(field).is(value);
                case "!=" -> Criteria.where(field).ne(value);
                case ">" -> Criteria.where(field).gt(parseNumeric(value));
                case ">=" -> Criteria.where(field).gte(parseNumeric(value));
                case "<" -> Criteria.where(field).lt(parseNumeric(value));
                case "<=" -> Criteria.where(field).lte(parseNumeric(value));
                case "in" -> Criteria.where(field).in(value.split(","));
                case "contains" -> Criteria.where(field).regex(value, "i");
                default -> {
                    log.warn("Unsupported operator: {}", rule.getOperator());
                    yield Criteria.where(field).is(value);
                }
            };
        } catch (Exception e) {
            log.error("Error building criteria for field: {}, operator: {}, value: {}", field, rule.getOperator(), value, e);
            return Criteria.where(field).is(value);
        }
    }

    private Criteria combineCriteria(List<Criteria> criteriaList, String combinator) {
        if (criteriaList.isEmpty()) {
            return new Criteria();
        }

        Criteria[] criteriaArray = criteriaList.toArray(new Criteria[0]);
        return "or".equalsIgnoreCase(combinator) ?
                new Criteria().orOperator(criteriaArray) :
                new Criteria().andOperator(criteriaArray);
    }

    private String convertFieldName(String field) {
        // Convert snake_case to camelCase for MongoDB field names
        String[] parts = field.split("_");
        StringBuilder camelCase = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCase.append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1));
        }
        return camelCase.toString();
    }

    private Number parseNumeric(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse numeric value: {}", value);
            return 0;
        }
    }
}