package com.dair.cais.filter.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterConfig {
//    @JsonProperty("combinator")
//    private FilterCombinator combinator;
    @JsonProperty("not")
    private Boolean not;
    @JsonProperty("rules")
    private List<FilterRule> rules;
}
