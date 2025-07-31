package com.dair.cais.attachment;

import com.dair.cais.attachment.exception.AttachmentException;
import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class AttachmentService {

   private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
   private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
       "application/pdf", "image/jpeg", "image/png", "image/gif", 
       "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
       "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
       "text/plain", "text/csv"
   );

   @Autowired
   private AttachmentMapper attachmentMapper;
   @Autowired
   private AttachmentRepository attachmentRepository;
   @Autowired
   private AttachmentMapperExtended attachmentMapperextended;


   // Add attachment to MongoDB
   @Transactional
   public ExtendedAttachment uploadAttachment(MultipartFile file, String alertId, String createdBy, String comment) {
      log.info("Starting file upload for alertId: {}, fileName: {}, fileSize: {}", alertId, file.getOriginalFilename(), file.getSize());
      
      validateFile(file);
      
      try {
         ExtendedAttachment attachment = new ExtendedAttachment();
         attachment.setAlertId(alertId);
         attachment.setFileName(file.getOriginalFilename());
         attachment.setFileType(file.getContentType());
         attachment.setFileSize(file.getSize());
         attachment.setFileData(file.getBytes());
         attachment.setCreatedBy(createdBy);
         attachment.setUpdatedBy(createdBy);
         attachment.setCreatedDate(new Date());
         attachment.setUpdatedDate(new Date());
         attachment.setComment(comment);

         // Save the attachment
         ExtendedAttachment savedAttachment = attachmentRepository.save(attachment);
         log.info("Successfully uploaded attachment for alertId: {}, attachmentId: {}", alertId, savedAttachment.getId());
         return savedAttachment;
      } catch (IOException e) {
         log.error("Failed to upload attachment for alertId: {}, error: {}", alertId, e.getMessage());
         throw new AttachmentException("Failed to process file upload: " + e.getMessage(), e);
      }
   }

   private void validateFile(MultipartFile file) {
      if (file.isEmpty()) {
         throw new AttachmentException("File cannot be empty");
      }
      
      if (file.getSize() > MAX_FILE_SIZE) {
         throw new AttachmentException("File size exceeds maximum allowed size of " + MAX_FILE_SIZE / (1024 * 1024) + "MB");
      }
      
      String contentType = file.getContentType();
      if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType)) {
         throw new AttachmentException("File type not allowed. Allowed types: " + ALLOWED_FILE_TYPES);
      }
      
      String fileName = file.getOriginalFilename();
      if (fileName == null || fileName.trim().isEmpty()) {
         throw new AttachmentException("File name cannot be empty");
      }
   }

   public List<ExtendedAttachment> getAttachmentsByAlertId(String alertId) {
      log.debug("Retrieving attachments for alertId: {}", alertId);
      List<AttachmentEntityExtended> attachmentEntities = attachmentRepository.getAttachmentsByAlertId(alertId);
      return attachmentEntities.stream()
              .map(attachmentMapperextended::toModel)
              .collect(Collectors.toList());
   }



   public Attachment createAttachment(String alertId, Attachment attachment) {
      AttachmentEntity upsertedAttachment = attachmentRepository
            .createUpsertAttachment(attachmentMapper.toEntity(alertId, attachment));
      return attachmentMapper.toModel(upsertedAttachment);
   }

   public Attachment patchAttachment(String alertId, String attachmentId, Attachment attachment) {
      AttachmentEntity upsertedAttachment = attachmentRepository
            .patchAttachment(attachmentMapper.toEntity(alertId, attachmentId, attachment));
      return attachmentMapper.toModel(upsertedAttachment);
   }

   public Attachment getAttachmentById(final String alertId, String attachmentId) {
      AttachmentEntity attachmentById = attachmentRepository.getAttachmentById(attachmentId);
      if (attachmentById == null) {
         throw new CaisIllegalArgumentException();
      } else if (!attachmentById.getAlertId().equals(alertId)) {
         throw new CaisIllegalArgumentException("attachment does not belong to this alert");
      }
      return attachmentMapper.toModel(attachmentById);
   }

   public Map<String, Object> getAllAttachments(String alertId, String name, Date createdDateFrom, Date createdDateTo,
         @Valid int limit,
         @Valid int offset) {
      validateRequestParams(name, createdDateFrom, createdDateTo, offset,
            limit);

      try {

         List<AttachmentEntity> allAttachmentEntities = attachmentRepository.getAllAttachments(alertId, name,
               createdDateFrom, createdDateTo, offset, limit);

         List<Attachment> allAttachments = allAttachmentEntities.stream().map(a -> attachmentMapper.toModel(a))
               .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("alerts", allAttachments);
         response.put("count", allAttachments.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving alerts");
      }
   }

   public List<Attachment> createAttachments(String alertId, List<Attachment> alerts) {
      List<Attachment> createdAttachments = alerts.stream().map(a -> createAttachment(alertId, a))
            .collect(Collectors.toList());
      return createdAttachments;
   }

   public ExtendedAttachment downloadAttachment(String alertId, String attachmentId) {
      log.debug("Downloading attachment for alertId: {}, attachmentId: {}", alertId, attachmentId);
      
      ExtendedAttachment attachment = attachmentRepository.getAttachmentWithFileData(attachmentId);
      
      if (attachment == null) {
         throw new AttachmentException("Attachment not found with id: " + attachmentId);
      }
      
      if (!attachment.getAlertId().equals(alertId)) {
         throw new AttachmentException("Attachment does not belong to this alert");
      }
      
      return attachment;
   }

   public byte[] downloadAllAttachmentsAsZip(String alertId) {
      log.info("Creating ZIP archive for all attachments of alertId: {}", alertId);
      
      List<ExtendedAttachment> attachments = attachmentRepository.getAttachmentsWithFileDataByAlertId(alertId);
      
      if (attachments.isEmpty()) {
         throw new AttachmentException("No attachments found for alert ID: " + alertId);
      }
      
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
           ZipOutputStream zos = new ZipOutputStream(baos)) {
         
         Map<String, Integer> fileNameCounters = new HashMap<>();
         
         for (ExtendedAttachment attachment : attachments) {
            String fileName = attachment.getFileName();
            byte[] fileData = attachment.getFileData();
            
            if (fileData == null) {
               log.warn("Skipping attachment {} - no file data", attachment.getId());
               continue;
            }
            
            // Handle duplicate filenames
            String uniqueFileName = getUniqueFileName(fileName, fileNameCounters);
            
            // Create ZIP entry
            ZipEntry zipEntry = new ZipEntry(uniqueFileName);
            zipEntry.setSize(fileData.length);
            zos.putNextEntry(zipEntry);
            zos.write(fileData);
            zos.closeEntry();
            
            log.debug("Added file to ZIP: {} (original: {})", uniqueFileName, fileName);
         }
         
         zos.finish();
         byte[] zipBytes = baos.toByteArray();
         
         log.info("Created ZIP archive for alertId: {} with {} files, total size: {} bytes", 
                  alertId, attachments.size(), zipBytes.length);
         
         return zipBytes;
         
      } catch (IOException e) {
         log.error("Failed to create ZIP archive for alertId: {}, error: {}", alertId, e.getMessage());
         throw new AttachmentException("Failed to create ZIP archive: " + e.getMessage(), e);
      }
   }
   
   private String getUniqueFileName(String originalFileName, Map<String, Integer> fileNameCounters) {
      String fileName = sanitizeFileName(originalFileName);
      
      if (!fileNameCounters.containsKey(fileName)) {
         fileNameCounters.put(fileName, 1);
         return fileName;
      }
      
      int counter = fileNameCounters.get(fileName);
      fileNameCounters.put(fileName, counter + 1);
      
      // Extract extension if present
      int dotIndex = fileName.lastIndexOf('.');
      if (dotIndex > 0) {
         String nameWithoutExt = fileName.substring(0, dotIndex);
         String extension = fileName.substring(dotIndex);
         return nameWithoutExt + "(" + counter + ")" + extension;
      } else {
         return fileName + "(" + counter + ")";
      }
   }
   
   private String sanitizeFileName(String fileName) {
      if (fileName == null || fileName.trim().isEmpty()) {
         return "unnamed_file";
      }
      
      // Replace characters that might cause issues in ZIP files
      return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
   }

   private void validateRequestParams(String name, Date createdDateFrom, Date createdDateTo, int offset, int limit) {
      StringBuilder errorMessage = new StringBuilder();

      if (name != null && !name.isEmpty()) {
         if (name.length() > 20) {
            errorMessage.append("name cannot be longer than 20 characters;");
         }
      }

      if (createdDateFrom != null && createdDateTo != null) {
         if (createdDateFrom.after(createdDateTo)) {
            errorMessage.append("from date cannot be after to date;");
         }
      }

      if (limit < 0) {
         errorMessage.append("limit cannot be negative;");
      }
      if (offset < 0) {
         errorMessage.append("offset cannot be negative;");
      }
      if (errorMessage.length() > 0) {
         log.warn("Validation failed: {}", errorMessage.toString());
         throw new AttachmentException(errorMessage.toString());
      }
   }

}
