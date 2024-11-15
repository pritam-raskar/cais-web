package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormattingConfigDto {
    private VisualFormatDto visualFormat;
    private DataFormatDto dataFormat;
    private List<ConditionalFormatDto> conditionalFormats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VisualFormatDto {
        private String backgroundColor;
        private String textColor;
        private String fontFamily;
        private String fontSize;
        private Boolean isBold;
        private Boolean isItalic;
        private Boolean wrapText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DataFormatDto {
        private String formatType;
        private String formatPattern;
        private String prefix;
        private String suffix;
        private Integer decimalPlaces;
        private Boolean thousandsSeparator;
        private String nullDisplay;
        private String dateFormat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConditionalFormatDto {
        private String condition;
        private String backgroundColor;
        private String textColor;
        private Boolean isBold;
        private String icon;
    }
}



//package com.dair.cais.reports.dto;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.Data;
//
//import java.util.List;
//
///**
// * DTO for formatting configuration (JSONB column)
// */
//@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class FormattingConfigDto {
//    private VisualFormatDto visualFormat;
//    private DataFormatDto dataFormat;
//    private List<ConditionalFormatDto> conditionalFormats;
//}