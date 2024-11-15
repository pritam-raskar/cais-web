package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryFilterDto {
    private String combinator; // "and" or "or"
    private Boolean not;
    private List<QueryRuleDto> rules;
}
