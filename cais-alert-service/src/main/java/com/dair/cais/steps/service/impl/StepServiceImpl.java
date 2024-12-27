package com.dair.cais.steps.service.impl;

import com.dair.cais.steps.Step;
import com.dair.cais.steps.StepRepository;
import com.dair.cais.steps.dto.StepDTO;
import com.dair.cais.steps.exception.StepNameAlreadyExistsException;
import com.dair.cais.steps.service.StepService;
import com.dair.cais.steps.exception.StepNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StepServiceImpl implements StepService {
    private final StepRepository stepRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StepDTO> getAllSteps() {
        log.debug("Fetching all steps");
        return stepRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StepDTO getStepById(Long id) {
        log.debug("Fetching step with id: {}", id);
        return stepRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new StepNotFoundException("Step not found with id: " + id));
    }

    @Override
    @Transactional
    public StepDTO createStep(StepDTO stepDTO) {
        log.debug("Creating new step with name: {}", stepDTO.getStepName());

        // Check if step name already exists
        if (stepRepository.existsByStepName(stepDTO.getStepName())) {
            log.error("Step name '{}' already exists", stepDTO.getStepName());
            throw new StepNameAlreadyExistsException(stepDTO.getStepName());
        }

        // Create new Step entity
        Step step = new Step();

        // Only set the allowed fields
        step.setStepName(stepDTO.getStepName());
        step.setDescription(stepDTO.getDescription());
        step.setCreatedBy(stepDTO.getCreatedBy());
        step.setStepStatusId(stepDTO.getStepStatusId());

        // Set the timestamps
        LocalDateTime now = LocalDateTime.now();
        step.setCreatedDate(now);
        step.setUpdatedDate(now);

        Step savedStep = stepRepository.save(step);
        log.info("Created new step with id: {}", savedStep.getStepId());

        return convertToDTO(savedStep);
    }

    @Override
    @Transactional
    public StepDTO updateStep(Long id, StepDTO stepDTO) {
        log.debug("Updating step with id: {}", id);
        return stepRepository.findById(id)
                .map(existingStep -> {
                    updateEntityFromDTO(stepDTO, existingStep);
                    existingStep.setUpdatedDate(LocalDateTime.now());
                    Step updatedStep = stepRepository.save(existingStep);
                    log.info("Updated step with id: {}", id);
                    return convertToDTO(updatedStep);
                })
                .orElseThrow(() -> new StepNotFoundException("Step not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteStep(Long id) {
        log.debug("Deleting step with id: {}", id);
        if (!stepRepository.existsById(id)) {
            throw new StepNotFoundException("Step not found with id: " + id);
        }
        stepRepository.deleteById(id);
        log.info("Deleted step with id: {}", id);
    }

    @Override
    @Transactional
    public StepDTO patchStep(Long id, StepDTO stepDTO) {
        log.debug("Patching step with id: {}", id);
        return stepRepository.findById(id)
                .map(existingStep -> {
                    updateEntityFromDTO(stepDTO, existingStep);
                    Step updatedStep = stepRepository.save(existingStep);
                    log.info("Patched step with id: {}", id);
                    return convertToDTO(updatedStep);
                })
                .orElseThrow(() -> new StepNotFoundException("Step not found with id: " + id));
    }

    private StepDTO convertToDTO(Step step) {
        StepDTO dto = new StepDTO();
        dto.setStepId(step.getStepId());
        dto.setStepName(step.getStepName());
        dto.setDescription(step.getDescription());
        dto.setCreatedBy(step.getCreatedBy());
        dto.setCreatedDate(step.getCreatedDate());
        dto.setUpdatedDate(step.getUpdatedDate());
        dto.setUpdatedBy(step.getUpdatedBy());
        dto.setStepStatusId(step.getStepStatusId());
        return dto;
    }

    private Step convertToEntity(StepDTO dto) {
        Step step = new Step();
        step.setStepId(dto.getStepId());
        step.setStepName(dto.getStepName());
        step.setDescription(dto.getDescription());
        step.setCreatedBy(dto.getCreatedBy());
        step.setCreatedDate(dto.getCreatedDate());
        step.setUpdatedDate(dto.getUpdatedDate());
        step.setUpdatedBy(dto.getUpdatedBy());
        step.setStepStatusId(dto.getStepStatusId());
        return step;
    }

    private void updateEntityFromDTO(StepDTO dto, Step step) {
        if (dto.getStepName() != null) {
            step.setStepName(dto.getStepName());
        }
        if (dto.getDescription() != null) {
            step.setDescription(dto.getDescription());
        }
        if (dto.getStepStatusId() != null) {
            step.setStepStatusId(dto.getStepStatusId());
        }
        if (dto.getUpdatedBy() != null) {
            step.setUpdatedBy(dto.getUpdatedBy());
        }
        step.setUpdatedDate(LocalDateTime.now());
    }

}