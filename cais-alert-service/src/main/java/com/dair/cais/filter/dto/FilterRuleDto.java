package com.dair.cais.filter.dto;

import com.dair.cais.filter.domain.FilterOperator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterRuleDto {
    private String field;
    private FilterOperator operator;
    private String value;
    //private FilterCombinator combinator;

    //@Valid
    private List<FilterRuleDto> rules;
}