package com.dair.cais.steps.service;

import com.dair.cais.steps.dto.StepDTO;
import java.util.List;

public interface StepService {
    List<StepDTO> getAllSteps();
    StepDTO getStepById(Long id);
    StepDTO createStep(StepDTO stepDTO);
    StepDTO updateStep(Long id, StepDTO stepDTO);
    void deleteStep(Long id);
    StepDTO patchStep(Long id, StepDTO stepDTO);
}