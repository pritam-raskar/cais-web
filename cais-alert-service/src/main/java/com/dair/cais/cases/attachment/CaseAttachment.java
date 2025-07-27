package com.dair.cais.cases.attachment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Case Attachments.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseAttachment {
    private Long attachmentId;
    private Long caseId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String comment;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}