package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConditionalFormatDto {
    private String condition;
    private String backgroundColor;
    private String textColor;
    private Boolean isBold;
    private String icon;
}
