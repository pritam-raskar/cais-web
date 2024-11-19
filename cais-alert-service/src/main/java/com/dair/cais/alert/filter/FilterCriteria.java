package com.dair.cais.alert.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterCriteria {
    private String combinator;  // "and" or "or"
    private Boolean not;
    private List<FilterRule> rules;
}