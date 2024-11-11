package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataFormatDto {
    private String formatType; // NUMBER, DATE, CURRENCY, PERCENTAGE, TEXT
    private String formatPattern;
    private String prefix;
    private String suffix;
    private Integer decimalPlaces;
    private Boolean thousandsSeparator;
    private String nullDisplay;
    private String dateFormat;
}