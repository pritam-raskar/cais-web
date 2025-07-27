package com.dair.cais.cases.attachment.controller;

import com.dair.cais.cases.attachment.CaseAttachment;
import com.dair.cais.cases.attachment.mapper.CaseAttachmentMapper;
import com.dair.cais.cases.attachment.repository.CaseAttachmentRepository;
import com.dair.cais.cases.attachment.service.CaseAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for managing case attachments.
 */
@Slf4j
@RestController
@RequestMapping("/case-attachments")
@Tag(name = "Case Attachments", description = "APIs for managing case attachments")
@RequiredArgsConstructor
public class CaseAttachmentController {

    private final CaseAttachmentService caseAttachmentService;
    private final CaseAttachmentMapper caseAttachmentMapper;
    private final CaseAttachmentRepository caseAttachmentRepository;

    /**
     * Add an attachment to a case.
     *
     * @param caseId  the case ID
     * @param file    the file to attach
     * @param comment optional comment for the attachment
     * @return the added attachment
     */
    @PostMapping("/cases/{caseId}")
    @Operation(summary = "Add an attachment to a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attachment added successfully",
                    content = @Content(schema = @Schema(implementation = CaseAttachment.class))),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input or file error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CaseAttachment> addAttachment(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Parameter(description = "File to attach", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Comment about the attachment")
            @RequestParam(value = "comment", required = false) String comment) {
        log.info("REST request to add attachment to case ID: {}", caseId);

        if (file.isEmpty()) {
            log.error("Empty file provided for case ID: {}", caseId);
            return ResponseEntity.badRequest().build();
        }

        try {
            // TODO: Get user ID from security context
            String userId = "system"; // Placeholder

            CaseAttachment attachment = caseAttachmentService.addAttachment(caseId, file, comment, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error handling file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error adding attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all attachments for a case.
     *
     * @param caseId the case ID
     * @return list of attachments
     */
    @GetMapping("/cases/{caseId}")
    @Operation(summary = "Get all attachments for a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CaseAttachment>> getAttachments(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to get attachments for case ID: {}", caseId);

        try {
            List<CaseAttachment> attachments = caseAttachmentService.getAttachments(caseId);
            return ResponseEntity.ok(attachments);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving attachments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download a case attachment.
     *
     * @param attachmentId the attachment ID
     * @return the file resource
     */
    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download a case attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "ID of the attachment", required = true)
            @PathVariable Long attachmentId) {
        log.info("REST request to download attachment with ID: {}", attachmentId);

        try {
            Resource resource = caseAttachmentService.downloadAttachment(attachmentId);

            // Get the attachment entity to determine filename and content type
            CaseAttachment attachment = caseAttachmentMapper.toModel(
                    caseAttachmentRepository.findById(attachmentId)
                            .orElseThrow(() -> new EntityNotFoundException("Attachment not found")));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);
        } catch (EntityNotFoundException e) {
            log.error("Attachment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error retrieving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error downloading attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a case attachment.
     *
     * @param attachmentId the attachment ID
     * @return void response
     */
    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete a case attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "ID of the attachment", required = true)
            @PathVariable Long attachmentId) {
        log.info("REST request to delete attachment with ID: {}", attachmentId);

        try {
            caseAttachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.error("Attachment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}