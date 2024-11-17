package com.dair.cais.filter.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterRule {
    @JsonProperty("field")
    private String field;
    @JsonProperty("operator")
    private FilterOperator operator;
    @JsonProperty("value")
    private String value;
//    @JsonProperty("combinator")
//    private FilterCombinator combinator;
    @JsonProperty("rules")
    private List<FilterRule> rules;
}
