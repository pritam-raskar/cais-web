package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryRuleDto {
    private String field;
    private String operator;
    private Object value;
    private String combinator;
    private List<QueryRuleDto> rules; // For nested rules
}
