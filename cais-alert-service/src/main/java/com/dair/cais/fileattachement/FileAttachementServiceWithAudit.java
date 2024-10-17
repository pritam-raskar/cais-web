package com.dair.cais.fileattachement;

import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.audit.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileAttachementServiceWithAudit {


    private StorageService storageService;
    private AuditTrailService auditTrailService;
    private CmAttachmentRepository cmAttachmentRepository;



//    @Autowired
//    public FileAttachementServiceWithAudit(CmAttachmentRepository cmAttachmentRepository, AuditTrailService auditTrailService) {
//        this.cmAttachmentRepository = cmAttachmentRepository;
//        this.auditTrailService = auditTrailService;
//    }
    @Autowired
    public FileAttachementServiceWithAudit(StorageService storageService, AuditTrailService auditTrailService, CmAttachmentRepository cmAttachmentRepository) {
        this.storageService = storageService;
        this.auditTrailService = auditTrailService;
        this.cmAttachmentRepository = cmAttachmentRepository;
    }

    public FileAttachment uploadAttachmentWithAudit(MultipartFile file, String alertId, String createdBy, String comment, AuditLogRequest auditLogRequest) throws IOException {
        if (alertId == null || alertId.trim().isEmpty()) {
            throw new IllegalArgumentException("AlertId cannot be null or empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Original filename cannot be null or empty");
        }

        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // Now storageService.store() returns the full S3 path
        String s3Path = storageService.store(alertId, uniqueFilename, file.getInputStream());

        CmAttachment cmAttachment = new CmAttachment();
        cmAttachment.setAlertId(alertId.trim());
        cmAttachment.setFileName(originalFilename.trim());
        cmAttachment.setFileType(file.getContentType());
        cmAttachment.setFileSize(file.getSize());
        cmAttachment.setCreatedBy(createdBy);
        cmAttachment.setCreatedDate(LocalDateTime.now());
        cmAttachment.setComment(comment);
        cmAttachment.setFilePath(s3Path);  // Now we're setting the full S3 path

        // Log CmAttachment details before saving
        System.out.println("CmAttachment before saving: " + cmAttachment.toString());

        CmAttachment savedAttachment = cmAttachmentRepository.save(cmAttachment);

        // Log saved CmAttachment details
        System.out.println("Saved CmAttachment: " + savedAttachment.toString());

        FileAttachment attachment = new FileAttachment();
        attachment.setAlertId(alertId);
        attachment.setFileName(originalFilename);
        attachment.setUniqueFileName(uniqueFilename);
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setCreatedBy(createdBy);
        attachment.setCreatedDate(LocalDateTime.now());
        attachment.setComment(comment);
//        attachment.setFilePath(s3Path);  // Set the full S3 path here as well

        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
                auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
                auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());

        return attachment;
    }

    public List<FileAttachment> uploadMultipleAttachmentsWithAudit(List<MultipartFile> files, String alertId, String createdBy, String comment, AuditLogRequest auditLogRequest) throws IOException {
        List<FileAttachment> attachments = new ArrayList<>();

        for (MultipartFile file : files) {
            attachments.add(uploadAttachmentWithAudit(file, alertId, createdBy, comment, auditLogRequest));
        }

        return attachments;
    }

    public List<FileAttachment> getAttachmentsByAlertIdWithAudit(String alertId, AuditLogRequest auditLogRequest) {
        List<CmAttachment> cmAttachments = cmAttachmentRepository.findByAlertId(alertId);

        // Log the audit
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
                auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
                auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());

        // Convert CmAttachment to FileAttachment
        return cmAttachments.stream().map(this::convertToFileAttachment).collect(Collectors.toList());
    }

    private FileAttachment convertToFileAttachment(CmAttachment cmAttachment) {
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setAlertId(cmAttachment.getAlertId());
        fileAttachment.setFileName(cmAttachment.getFileName());
        fileAttachment.setFileType(cmAttachment.getFileType());
        fileAttachment.setFileSize(cmAttachment.getFileSize());
        fileAttachment.setCreatedBy(cmAttachment.getCreatedBy());
        fileAttachment.setCreatedDate(cmAttachment.getCreatedDate());
        fileAttachment.setComment(cmAttachment.getComment());
//        fileAttachment.setFilePath(cmAttachment.getFilePath());
        // Set other fields as necessary
        return fileAttachment;
    }


    public List<String> getAttachmentsByAlertIdWithAuditS3(String alertId , AuditLogRequest auditLogRequest) throws IOException {

        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
                auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
                auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());

        return storageService.listByAlertId(alertId);
    }

    public byte[] downloadAttachmentWithAudit(String alertId, String fileName , AuditLogRequest auditLogRequest) throws IOException {

        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
                auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
                auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());

        return storageService.retrieve(alertId, fileName);
    }

    public FileAttachment getAttachmentMetadataWithAudit(String alertId, String fileName , AuditLogRequest auditLogRequest) {

        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
                auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
                auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());

        // This method would typically retrieve metadata from a database
        // For this example, we're creating a dummy FileAttachment object
        FileAttachment attachment = new FileAttachment();
        attachment.setAlertId(alertId);
        attachment.setFileName(fileName);
        attachment.setUniqueFileName(fileName);
        // Set other metadata fields as needed
        return attachment;
    }
}
