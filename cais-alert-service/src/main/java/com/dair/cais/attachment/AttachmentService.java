package com.dair.cais.attachment;

import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AttachmentService {

   @Autowired
   private AttachmentMapper attachmentMapper; // Assuming this is used elsewhere
   @Autowired
   private AttachmentRepository attachmentRepository;
   @Autowired
   private AttachmentMapperExtended attachmentMapperextended; // Assuming this is used elsewhere


   // Add attachment to MongoDB
   public ExtendedAttachment uploadAttachment(MultipartFile file, String alertId, String createdBy , String comment) throws IOException {
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
      return attachmentRepository.save(attachment);
   }

   public List<ExtendedAttachment> getAttachmentsByAlertId(String alertId) {
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
      if (errorMessage.isEmpty()) {
         return;
      }

      throw new CaisIllegalArgumentException(errorMessage.toString());
   }

}
