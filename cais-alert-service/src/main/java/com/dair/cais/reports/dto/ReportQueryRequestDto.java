package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportQueryRequestDto {
    private QueryFilterDto query;
    private Long userId;
}

