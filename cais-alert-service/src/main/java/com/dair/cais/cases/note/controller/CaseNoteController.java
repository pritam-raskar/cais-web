package com.dair.cais.cases.note.controller;

import com.dair.cais.cases.note.CaseNote;
import com.dair.cais.cases.note.service.CaseNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing case notes.
 */
@Slf4j
@RestController
@RequestMapping("/case-notes")
@Tag(name = "Case Notes", description = "APIs for managing case notes")
@RequiredArgsConstructor
public class CaseNoteController {

    private final CaseNoteService caseNoteService;

    /**
     * Add a note to a case.
     *
     * @param caseId the case ID
     * @param note   the note to add
     * @return the added note
     */
    @PostMapping("/cases/{caseId}")
    @Operation(summary = "Add a note to a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Note added successfully",
                    content = @Content(schema = @Schema(implementation = CaseNote.class))),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CaseNote> addNote(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Valid @RequestBody CaseNote note) {
        log.info("REST request to add note to case ID: {}", caseId);

        try {
            // TODO: Get user ID from security context
            if (note.getCreatedBy() == null) {
                note.setCreatedBy("system"); // Placeholder
            }

            CaseNote addedNote = caseNoteService.addNote(caseId, note);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedNote);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid note data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error adding note", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all notes for a case.
     *
     * @param caseId the case ID
     * @return list of notes
     */
    @GetMapping("/cases/{caseId}")
    @Operation(summary = "Get all notes for a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CaseNote>> getNotes(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to get notes for case ID: {}", caseId);

        try {
            List<CaseNote> notes = caseNoteService.getNotes(caseId);
            return ResponseEntity.ok(notes);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving notes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a case note.
     *
     * @param noteId the note ID
     * @param note   the updated note
     * @return the updated note
     */
    @PutMapping("/{noteId}")
    @Operation(summary = "Update a case note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note updated successfully",
                    content = @Content(schema = @Schema(implementation = CaseNote.class))),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CaseNote> updateNote(
            @Parameter(description = "ID of the note", required = true)
            @PathVariable Long noteId,
            @Valid @RequestBody CaseNote note) {
        log.info("REST request to update note with ID: {}", noteId);

        try {
            CaseNote updatedNote = caseNoteService.updateNote(noteId, note);
            return ResponseEntity.ok(updatedNote);
        } catch (EntityNotFoundException e) {
            log.error("Note not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid note data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating note", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a case note.
     *
     * @param noteId the note ID
     * @return void response
     */
    @DeleteMapping("/{noteId}")
    @Operation(summary = "Delete a case note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Note deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteNote(
            @Parameter(description = "ID of the note", required = true)
            @PathVariable Long noteId) {
        log.info("REST request to delete note with ID: {}", noteId);

        try {
            caseNoteService.deleteNote(noteId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.error("Note not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting note", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}