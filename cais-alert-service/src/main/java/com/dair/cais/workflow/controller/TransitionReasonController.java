package com.dair.cais.workflow.controller;

import com.dair.cais.workflow.exception.ErrorResponse;
import com.dair.cais.workflow.model.TransitionReasonDTO;
import com.dair.cais.workflow.service.TransitionReasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transition-reasons")
@Tag(name = "Transition Reason Management", description = "APIs for managing workflow transition reasons")
@RequiredArgsConstructor
public class TransitionReasonController {
    private final TransitionReasonService transitionReasonService;

    @GetMapping
    @Operation(summary = "Get all transition reasons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transition reasons"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TransitionReasonDTO>> getAllReasons() {
        log.info("REST request to get all transition reasons");
        return ResponseEntity.ok(transitionReasonService.getAllReasons());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transition reason by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transition reason"),
            @ApiResponse(responseCode = "404", description = "Transition reason not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransitionReasonDTO> getReasonById(
            @Parameter(description = "ID of the transition reason to retrieve", required = true)
            @PathVariable Long id) {
        log.info("REST request to get transition reason : {}", id);
        return ResponseEntity.ok(transitionReasonService.getReasonById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new transition reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transition reason created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Transition reason already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransitionReasonDTO> createReason(
            @Parameter(description = "Transition reason to create", required = true)
            @Valid @RequestBody TransitionReasonDTO reasonDTO) {
        log.info("REST request to create transition reason : {}", reasonDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transitionReasonService.createReason(reasonDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transition reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transition reason updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transition reason not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransitionReasonDTO> updateReason(
            @Parameter(description = "ID of the transition reason to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated transition reason details", required = true)
            @Valid @RequestBody TransitionReasonDTO reasonDTO) {
        log.info("REST request to update transition reason : {}", id);
        return ResponseEntity.ok(transitionReasonService.updateReason(id, reasonDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transition reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transition reason deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transition reason not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteReason(
            @Parameter(description = "ID of the transition reason to delete", required = true)
            @PathVariable Long id) {
        log.info("REST request to delete transition reason : {}", id);
        transitionReasonService.deleteReason(id);
        return ResponseEntity.noContent().build();
    }
}
