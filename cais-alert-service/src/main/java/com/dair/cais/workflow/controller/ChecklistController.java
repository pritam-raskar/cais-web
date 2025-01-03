package com.dair.cais.workflow.controller;

import com.dair.cais.workflow.model.ChecklistDTO;
import com.dair.cais.workflow.service.ChecklistService;
import com.dair.cais.workflow.exception.ErrorResponse;
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
@RequestMapping("/api/v1/checklists")
@Tag(name = "Checklist Management", description = "APIs for managing workflow step checklists")
@RequiredArgsConstructor
public class ChecklistController {
    private final ChecklistService checklistService;

    @GetMapping
    @Operation(summary = "Get all checklists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved checklists"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ChecklistDTO>> getAllChecklists() {
        log.info("REST request to get all checklists");
        return ResponseEntity.ok(checklistService.getAllChecklists());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get checklist by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved checklist"),
            @ApiResponse(responseCode = "404", description = "Checklist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ChecklistDTO> getChecklistById(
            @Parameter(description = "ID of the checklist to retrieve", required = true)
            @PathVariable Long id) {
        log.info("REST request to get checklist : {}", id);
        return ResponseEntity.ok(checklistService.getChecklistById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new checklist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Checklist created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Checklist already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ChecklistDTO> createChecklist(
            @Parameter(description = "Checklist to create", required = true)
            @Valid @RequestBody ChecklistDTO checklistDTO) {
        log.info("REST request to create checklist : {}", checklistDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checklistService.createChecklist(checklistDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing checklist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Checklist updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Checklist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ChecklistDTO> updateChecklist(
            @Parameter(description = "ID of the checklist to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated checklist details", required = true)
            @Valid @RequestBody ChecklistDTO checklistDTO) {
        log.info("REST request to update checklist : {}", id);
        return ResponseEntity.ok(checklistService.updateChecklist(id, checklistDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a checklist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Checklist deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Checklist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteChecklist(
            @Parameter(description = "ID of the checklist to delete", required = true)
            @PathVariable Long id) {
        log.info("REST request to delete checklist : {}", id);
        checklistService.deleteChecklist(id);
        return ResponseEntity.noContent().build();
    }
}

