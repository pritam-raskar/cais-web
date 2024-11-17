package com.dair.cais.filter.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterConfigDto {
    //@NotNull(message = "Combinator is required")
    //private FilterCombinator combinator;

    private Boolean not;

    @NotEmpty(message = "At least one rule is required")
    @Valid
    private List<FilterRuleDto> rules;
}
