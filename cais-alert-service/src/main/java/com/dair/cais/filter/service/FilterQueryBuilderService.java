package com.dair.cais.filter.service;

import com.dair.cais.filter.domain.FilterConfig;
import com.dair.cais.filter.domain.FilterOperator;
import com.dair.cais.filter.domain.FilterRule;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
public class FilterQueryBuilderService {

//    public <T> Specification<T> buildSpecification(FilterConfig filterConfig) {
//        return (root, query, cb) -> buildPredicate(filterConfig, root, cb);
//    }
//
//    private <T> Predicate buildPredicate(FilterConfig filterConfig, Root<T> root, CriteriaBuilder cb) {
//        List<Predicate> predicates = buildPredicates(filterConfig.getRules(), root, cb);
//
//        Predicate predicate = filterConfig.getCombinator().equals("and")
//                ? cb.and(predicates.toArray(new Predicate[0]))
//                : cb.or(predicates.toArray(new Predicate[0]));
//
//        return filterConfig.getNot() != null && filterConfig.getNot()
//                ? cb.not(predicate)
//                : predicate;
//    }
//
//    private <T> List<Predicate> buildPredicates(List<FilterRule> rules, Root<T> root, CriteriaBuilder cb) {
//        List<Predicate> predicates = new ArrayList<>();
//
//        for (FilterRule rule : rules) {
//            if (rule.getRules() != null && !rule.getRules().isEmpty()) {
//                // Handle nested rules
//                List<Predicate> nestedPredicates = buildPredicates(rule.getRules(), root, cb);
//                Predicate nestedPredicate = rule.getCombinator().equals("and")
//                        ? cb.and(nestedPredicates.toArray(new Predicate[0]))
//                        : cb.or(nestedPredicates.toArray(new Predicate[0]));
//                predicates.add(nestedPredicate);
//            } else {
//                // Handle leaf rule
//                predicates.add(buildPredicate(rule, root, cb));
//            }
//        }
//
//        return predicates;
//    }
//
//    private <T> Predicate buildPredicate(FilterRule rule, Root<T> root, CriteriaBuilder cb) {
//        Path<?> path = root.get(rule.getField());
//        FilterOperator operator = rule.getOperator();
//        Object value = convertValue(rule.getValue(), path.getJavaType());
//
//        return switch (operator) {
//            case EQUALS -> cb.equal(path, value);
//            case NOT_EQUALS -> cb.notEqual(path, value);
//            case GREATER_THAN -> cb.greaterThan(path.as(Comparable.class), (Comparable) value);
//            case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
//            case LESS_THAN -> cb.lessThan(path.as(Comparable.class), (Comparable) value);
//            case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
//            case CONTAINS -> cb.like(path.as(String.class), "%" + value + "%");
//            case STARTS_WITH -> cb.like(path.as(String.class), value + "%");
//            case ENDS_WITH -> cb.like(path.as(String.class), "%" + value);
//            case IS_NULL -> cb.isNull(path);
//            case IS_NOT_NULL -> cb.isNotNull(path);
//            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
//        };
//    }
//
//    private Object convertValue(String value, Class<?> targetType) {
//        if (value == null) return null;
//
//        if (targetType == LocalDateTime.class) {
//            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
//        }
//        if (targetType == Boolean.class) {
//            return Boolean.valueOf(value);
//        }
//        if (targetType == Integer.class) {
//            return Integer.valueOf(value);
//        }
//        if (targetType == Long.class) {
//            return Long.valueOf(value);
//        }
//        if (targetType == Double.class) {
//            return Double.valueOf(value);
//        }
//
//        return value;
//    }
}
