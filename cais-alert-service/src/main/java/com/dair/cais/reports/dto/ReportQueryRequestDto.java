package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportQueryRequestDto {
    private QueryFilterDto query;
    private Long userId;
    private Integer pageNumber; // Add this field for pagination

    @Min(value = 1, message = "Page size must be greater than 0")
    @Max(value = 1000, message = "Page size cannot exceed 1000")
    private Integer pageSize;  // Add this field
}

