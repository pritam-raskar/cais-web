package com.dair.cais.reports.dto;

import com.dair.cais.reports.enums.enums.ParameterType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportParameterDto {
    private Long parameterId;
    private String parameterName;
    private String parameterLabel;
    private ParameterType parameterType;
    private Boolean isRequired;
    private String defaultValue;
    private Map<String, Object> validationRules;
}