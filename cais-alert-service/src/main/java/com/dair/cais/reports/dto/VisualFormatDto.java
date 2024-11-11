package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VisualFormatDto {
    private String backgroundColor;
    private String textColor;
    private String fontFamily;
    private String fontSize;
    private Boolean isBold;
    private Boolean isItalic;
    private Boolean wrapText;
}