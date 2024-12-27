package com.dair.cais.steps.controller;

import com.dair.cais.steps.dto.CreateStepDTO;
import com.dair.cais.steps.dto.StepDTO;
import com.dair.cais.steps.service.StepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/steps")
@RequiredArgsConstructor
@Validated
@Tag(name = "Step Management", description = "APIs for managing steps")
public class StepController {
    private final StepService stepService;

    @GetMapping
    @Operation(summary = "Get all steps")
    public ResponseEntity<List<StepDTO>> getAllSteps() {
        log.debug("REST request to get all Steps");
        List<StepDTO> steps = stepService.getAllSteps();
        return ResponseEntity.ok(steps);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get step by ID")
    public ResponseEntity<StepDTO> getStepById(@PathVariable Long id) {
        log.debug("REST request to get Step with id: {}", id);
        StepDTO step = stepService.getStepById(id);
        return ResponseEntity.ok(step);
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new step")
    public ResponseEntity<StepDTO> createStep(@Valid @RequestBody CreateStepDTO createStepDTO) {
        log.debug("REST request to create Step: {}", createStepDTO);

        // Convert CreateStepDTO to StepDTO
        StepDTO stepDTO = new StepDTO();
        stepDTO.setStepName(createStepDTO.getStepName());
        stepDTO.setDescription(createStepDTO.getDescription());
        stepDTO.setCreatedBy(createStepDTO.getCreatedBy());
        stepDTO.setStepStatusId(createStepDTO.getStepStatusId());

        StepDTO createdStep = stepService.createStep(stepDTO);
        return new ResponseEntity<>(createdStep, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing step")
    public ResponseEntity<StepDTO> updateStep(
            @PathVariable Long id,
            @Valid @RequestBody StepDTO stepDTO) {
        log.debug("REST request to update Step with id: {}", id);
        StepDTO updatedStep = stepService.updateStep(id, stepDTO);
        return ResponseEntity.ok(updatedStep);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a step")
    public ResponseEntity<Void> deleteStep(@PathVariable Long id) {
        log.debug("REST request to delete Step with id: {}", id);
        stepService.deleteStep(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a step")
    public ResponseEntity<StepDTO> patchStep(
            @PathVariable Long id,
            @RequestBody StepDTO stepDTO) {
        log.debug("REST request to patch Step with id: {}", id);
        StepDTO patchedStep = stepService.patchStep(id, stepDTO);
        return ResponseEntity.ok(patchedStep);
    }
}