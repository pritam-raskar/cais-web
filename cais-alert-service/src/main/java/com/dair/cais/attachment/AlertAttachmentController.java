package com.dair.cais.attachment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Dair
 * @since 2022
 */

@RestController
@RequestMapping("/alerts/attachments/{alertId}")
@Tag(name = "alert attachments")
public class AlertAttachmentController {

   @Autowired
   private AttachmentService attachmentService;

   @PostMapping("/upload")
   @Operation(summary = "Add/Upload the attachment")
   public ResponseEntity<ExtendedAttachment> uploadAttachment(
           @RequestParam("file") @Valid MultipartFile file,
           @PathVariable @NotBlank String alertId,
           @RequestParam("createdBy") @NotBlank String createdBy,
           @RequestParam("comment") @NotBlank String comment) {

      // Call the service to handle the file upload
      ExtendedAttachment attachment = attachmentService.uploadAttachment(file, alertId, createdBy, comment);
      return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
   }

   @GetMapping("/getattachments")
   @Operation(summary = "List all attachments for a given alert ID")
   public ResponseEntity<List<ExtendedAttachment>> getAttachmentsByAlertId(@PathVariable String alertId) {
      List<ExtendedAttachment> attachments = attachmentService.getAttachmentsByAlertId(alertId);
      return ResponseEntity.ok(attachments);
   }


   @PostMapping
   @Operation(summary = "Create an attachment")
   public ResponseEntity<Attachment> createAttachment(@PathVariable final String alertId,
         @RequestBody Attachment attachment) {
      Attachment createdAttachment = attachmentService.createAttachment(alertId, attachment);
      return ResponseEntity.ok().body(createdAttachment);
   }

   @PatchMapping("{attachmentId}")
   @Operation(summary = "Update an attachment")
   public ResponseEntity<Attachment> patchAttachment(@PathVariable final String alertId,
         @PathVariable final String attachmentId,
         @RequestBody Attachment attachment) {
      Attachment updatedAttachment = attachmentService.patchAttachment(alertId, attachmentId, attachment);
      return ResponseEntity.ok().body(updatedAttachment);
   }

   @PostMapping("/bulk")
   @Operation(summary = "Create bulk attachments")
   public ResponseEntity<List<Attachment>> createAttachments(@PathVariable final String alertId,
         @RequestBody List<Attachment> attachments) {
      List<Attachment> createdAttachments = attachmentService.createAttachments(alertId, attachments);
      return ResponseEntity.ok().body(createdAttachments);
   }

   @GetMapping("{attachmentId}")
   @Operation(summary = "Get an attachment by its id")
   public ResponseEntity<Attachment> getAttachmentById(@PathVariable final String alertId,
         @PathVariable final String attachmentId) {
      Attachment attachmentById = attachmentService.getAttachmentById(alertId, attachmentId);
      return ResponseEntity.ok().body(attachmentById);
   }

   @GetMapping("")
   @Operation(summary = "Get all attachments; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllAttachments(
         @PathVariable String alertId,
         @RequestParam(required = false) String name,
         @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
         @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @Valid @RequestParam(defaultValue = "0") int offset) {
      return ResponseEntity.ok()
            .body(attachmentService.getAllAttachments(alertId, name,
                  createdDateFrom, createdDateTo, limit, offset));
   }

   @GetMapping("/{attachmentId}/download")
   @Operation(summary = "Download attachment file")
   public ResponseEntity<byte[]> downloadAttachment(
           @PathVariable @NotBlank String alertId,
           @PathVariable @NotBlank String attachmentId) {
      
      ExtendedAttachment attachment = attachmentService.downloadAttachment(alertId, attachmentId);
      
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(attachment.getFileType()));
      headers.setContentDispositionFormData("attachment", attachment.getFileName());
      headers.setContentLength(attachment.getFileSize());
      
      return ResponseEntity.ok()
              .headers(headers)
              .body(attachment.getFileData());
   }

   @GetMapping("/downloadAll")
   @Operation(summary = "Download all attachments as ZIP file")
   public ResponseEntity<byte[]> downloadAllAttachments(
           @PathVariable @NotBlank String alertId) {
      
      byte[] zipData = attachmentService.downloadAllAttachmentsAsZip(alertId);
      
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType("application/zip"));
      headers.setContentDispositionFormData("attachment", "alert-" + alertId + "-attachments.zip");
      headers.setContentLength(zipData.length);
      
      return ResponseEntity.ok()
              .headers(headers)
              .body(zipData);
   }
}