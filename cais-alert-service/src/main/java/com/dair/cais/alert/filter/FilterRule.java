package com.dair.cais.alert.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterRule {
    private String field;
    private String operator;
    private String value;
    private String combinator;
    private List<FilterRule> rules;

    public boolean isComposite() {
        return rules != null && !rules.isEmpty();
    }
}