package com.dair.cais.alert.dto;

import lombok.Data;
import java.util.List;

@Data
public class StepTransitionDTO {
    private List<StepInfo> nextSteps;
    private List<StepInfo> backSteps;
}

