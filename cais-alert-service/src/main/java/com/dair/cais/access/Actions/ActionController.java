package com.dair.cais.access.Actions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/actions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Actions", description = "API for managing actions")
public class ActionController {

    private final ActionService actionService;

    @GetMapping("/by-action-type/{actionType}")
    @Operation(summary = "Get actions by action type", description = "Retrieves all available actions for a given action type")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved actions")
    @ApiResponse(responseCode = "400", description = "Invalid action type provided")
    @ApiResponse(responseCode = "404", description = "No actions found for the given action type")
    public ResponseEntity<List<ActionEntity>> getActionsByActionType(
            @Parameter(description = "Action type to fetch actions for", required = true)
            @PathVariable @NotBlank String actionType) {
        log.info("Received request to get actions for action type: {}", actionType);
        List<ActionEntity> actions = actionService.getActionsByActionType(actionType);
        
        if (actions.isEmpty()) {
            log.warn("No actions found for action type: {}", actionType);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/by-action-category/{actionCategory}")
    @Operation(summary = "Get actions by action category", description = "Retrieves all available actions for a given action category")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved actions")
    @ApiResponse(responseCode = "400", description = "Invalid action category provided")
    @ApiResponse(responseCode = "404", description = "No actions found for the given action category")
    public ResponseEntity<List<ActionEntity>> getActionsByActionCategory(
            @Parameter(description = "Action category to fetch actions for", required = true)
            @PathVariable @NotBlank String actionCategory) {
        log.info("Received request to get actions for action category: {}", actionCategory);
        List<ActionEntity> actions = actionService.getActionsByActionCategory(actionCategory);

        if (actions.isEmpty()) {
            log.warn("No actions found for action category: {}", actionCategory);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(actions);
    }

    @GetMapping
    @Operation(summary = "Get all actions", description = "Retrieves all available actions in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all actions")
    @ApiResponse(responseCode = "204", description = "No actions found in the system")
    public ResponseEntity<List<ActionEntity>> getAllActions() {
        log.info("Received request to get all actions");
        List<ActionEntity> actions = actionService.getAllActions();
        
        if (actions.isEmpty()) {
            log.warn("No actions found in the system");
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(actions);
    }
}
