package com.dair.cais.fileattachement;

import com.dair.cais.audit.AuditLogRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file-attachment")
public class FileAttachmentController {

    @Autowired
    private FileAttachmentService attachmentService;

    @Autowired
    private FileAttachementServiceWithAudit attachmentServiceWithAudit;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileAttachment> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("alertId") String alertId,
            @RequestParam("createdBy") String createdBy,
            @RequestParam("comment") String comment) throws IOException {

        FileAttachment attachment = attachmentService.uploadAttachment(file, alertId, createdBy, comment);
        return ResponseEntity.ok(attachment);
    }

    @PostMapping(value = "/upload-audit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileAttachment> uploadAttachmentWithAudit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("alertId") String alertId,
            @RequestParam("createdBy") String createdBy,
            @RequestParam("comment") String comment,
            @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {

        System.out.println("Received request for file upload");
        System.out.println("File name: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize());
        System.out.println("File content type: " + file.getContentType());
        System.out.println("Received alertId: " + alertId);
        System.out.println("Received createdBy: " + createdBy);
        System.out.println("Received comment: " + comment);

        ObjectMapper objectMapper = new ObjectMapper();
        AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);
        System.out.println("Parsed AuditLogRequest: " + auditLogRequest.toString());

        FileAttachment attachment = attachmentServiceWithAudit.uploadAttachmentWithAudit(file, alertId, createdBy, comment, auditLogRequest);
        System.out.println("File upload completed. Returning FileAttachment: " + attachment.toString());

        return ResponseEntity.ok(attachment);
    }

    @PostMapping("/uploadMultipleFiles")
    public ResponseEntity<List<FileAttachment>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files,
                                                                    @RequestParam("alertId") String alertId,
                                                                    @RequestParam("createdBy") String createdBy,
                                                                    @RequestParam("comment") String comment) throws IOException {
        List<FileAttachment> attachments = attachmentService.uploadMultipleAttachments(files, alertId, createdBy, comment);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }

    @PostMapping(value = "/upload-multi-alerts-audit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, FileAttachment>> uploadAttachmentForMultipleAlertsWithAudit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("alertIds") List<String> alertIds,
            @RequestParam("createdBy") String createdBy,
            @RequestParam("comment") String comment,
            @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);

        Map<String, FileAttachment> result = attachmentServiceWithAudit.uploadAttachmentForMultipleAlertsWithAudit(
                file, alertIds, createdBy, comment, auditLogRequest);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/upload-multiple-files-multi-alerts-audit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, List<FileAttachment>>> uploadMultipleAttachmentsForMultipleAlertsWithAudit(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("alertIds") List<String> alertIds,
            @RequestParam("createdBy") String createdBy,
            @RequestParam("comment") String comment,
            @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);

        Map<String, List<FileAttachment>> result = attachmentServiceWithAudit.uploadMultipleAttachmentsForMultipleAlertsWithAudit(
                files, alertIds, createdBy, comment, auditLogRequest);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/uploadMultipleFiles-audit")
    public ResponseEntity<List<FileAttachment>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files,
                                                                    @RequestParam("alertId") String alertId,
                                                                    @RequestParam("createdBy") String createdBy,
                                                                    @RequestParam("comment") String comment,
                                                                    @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);
        List<FileAttachment> attachments = attachmentServiceWithAudit.uploadMultipleAttachmentsWithAudit(files, alertId, createdBy, comment, auditLogRequest);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }


    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<String>> getAttachmentsByAlertId(@PathVariable String alertId) throws IOException {
        List<String> attachments = attachmentService.getAttachmentsByAlertId(alertId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/alert-audit/{alertId}")
    public ResponseEntity<List<FileAttachment>> getAttachmentsByAlertId(
            @PathVariable String alertId,
            @RequestBody String auditLogRequestJson) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);

        List<FileAttachment> attachments = attachmentServiceWithAudit.getAttachmentsByAlertIdWithAudit(alertId, auditLogRequest);
        return ResponseEntity.ok(attachments);
    }
//    @GetMapping("/alert-audit/{alertId}")
//    public ResponseEntity<List<String>> getAttachmentsByAlertId(@PathVariable String alertId,
//                                                                @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);
//        List<String> attachments = attachmentServiceWithAudit.getAttachmentsByAlertIdWithAudit(alertId, auditLogRequest);
//        return ResponseEntity.ok(attachments);
//    }

    @GetMapping("/download/{alertId}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadAttachment(
            @PathVariable String alertId,
            @PathVariable String fileName) throws IOException {
        byte[] data = attachmentService.downloadAttachment(alertId, fileName);
        ByteArrayResource resource = new ByteArrayResource(data);

        FileAttachment metadata = attachmentService.getAttachmentMetadata(alertId, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                .contentLength(data.length)
                .contentType(MediaType.parseMediaType(metadata.getFileType()))
                .body(resource);
    }

    @GetMapping("/download-audit/{alertId}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadAttachment(
            @PathVariable String alertId,
            @PathVariable String fileName,
            @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);
        byte[] data = attachmentService.downloadAttachment(alertId, fileName);
        ByteArrayResource resource = new ByteArrayResource(data);

        FileAttachment metadata = attachmentServiceWithAudit.getAttachmentMetadataWithAudit(alertId, fileName, auditLogRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                .contentLength(data.length)
                .contentType(MediaType.parseMediaType(metadata.getFileType()))
                .body(resource);
    }
}