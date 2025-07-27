package com.dair.cais.cases.attachment.service;

import com.dair.cais.cases.attachment.CaseAttachment;
import com.dair.cais.cases.attachment.entity.CaseAttachmentEntity;
import com.dair.cais.cases.attachment.mapper.CaseAttachmentMapper;
import com.dair.cais.cases.attachment.repository.CaseAttachmentRepository;
import com.dair.cais.cases.repository.CaseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing case attachments.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAttachmentService {

    private final CaseAttachmentRepository caseAttachmentRepository;
    private final CaseAttachmentMapper caseAttachmentMapper;
    private final CaseRepository caseRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * Add an attachment to a case.
     *
     * @param caseId    the case ID
     * @param file      the file to attach
     * @param comment   optional comment for the attachment
     * @param userId    ID of the user uploading the file
     * @return the added attachment
     * @throws EntityNotFoundException if the case is not found
     * @throws IOException if file storage fails
     */
    @Transactional
    public CaseAttachment addAttachment(Long caseId, MultipartFile file, String comment, String userId) throws IOException {
        log.debug("Adding attachment to case ID: {}", caseId);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, "case-" + caseId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Process and save the file
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create and save attachment entity
        CaseAttachmentEntity entity = new CaseAttachmentEntity();
        entity.setCaseId(caseId);
        entity.setFileName(originalFilename);
        entity.setFilePath(filePath.toString());
        entity.setFileType(file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setComment(comment);
        entity.setUploadedBy(userId);
        entity.setUploadedAt(LocalDateTime.now());

        CaseAttachmentEntity savedEntity = caseAttachmentRepository.save(entity);

        log.info("Added attachment with ID: {} to case ID: {}", savedEntity.getAttachmentId(), caseId);
        return caseAttachmentMapper.toModel(savedEntity);
    }

    /**
     * Get all attachments for a case.
     *
     * @param caseId the case ID
     * @return list of attachments
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional(readOnly = true)
    public List<CaseAttachment> getAttachments(Long caseId) {
        log.debug("Getting attachments for case ID: {}", caseId);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        List<CaseAttachmentEntity> entities = caseAttachmentRepository.findByCaseId(caseId);
        return caseAttachmentMapper.toModelList(entities);
    }

    /**
     * Download a case attachment.
     *
     * @param attachmentId the attachment ID
     * @return the file resource
     * @throws EntityNotFoundException if the attachment is not found
     * @throws IOException if file retrieval fails
     */
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId) throws IOException {
        log.debug("Downloading attachment with ID: {}", attachmentId);

        CaseAttachmentEntity entity = caseAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> {
                    log.error("Attachment not found with ID: {}", attachmentId);
                    return new EntityNotFoundException("Attachment not found with ID: " + attachmentId);
                });

        Path filePath = Paths.get(entity.getFilePath());
        Resource resource;

        try {
            resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.info("Successfully retrieved attachment with ID: {}", attachmentId);
                return resource;
            } else {
                log.error("File not found or not readable: {}", filePath);
                throw new IOException("File not found or not readable: " + filePath);
            }
        } catch (MalformedURLException e) {
            log.error("Error loading file: {}", e.getMessage());
            throw new IOException("Error loading file: " + e.getMessage());
        }
    }

    /**
     * Delete a case attachment.
     *
     * @param attachmentId the attachment ID
     * @throws EntityNotFoundException if the attachment is not found
     */
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        log.debug("Deleting attachment with ID: {}", attachmentId);

        CaseAttachmentEntity entity = caseAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> {
                    log.error("Attachment not found with ID: {}", attachmentId);
                    return new EntityNotFoundException("Attachment not found with ID: " + attachmentId);
                });

        // Delete file from filesystem
        try {
            Path filePath = Paths.get(entity.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Error deleting file: {}", e.getMessage());
            // Continue with database deletion even if file deletion fails
        }

        // Delete record from database
        caseAttachmentRepository.deleteById(attachmentId);
        log.info("Deleted attachment with ID: {}", attachmentId);
    }
}