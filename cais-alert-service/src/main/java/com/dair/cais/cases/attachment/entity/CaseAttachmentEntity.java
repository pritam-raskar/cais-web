package com.dair.cais.cases.attachment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity class for Case Attachments table in database.
 */
@Data
@Entity
@Table(name = "cm_case_attachment", schema = "info_alert")
@EntityListeners(AuditingEntityListener.class)
public class CaseAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_attachment_seq")
    @SequenceGenerator(name = "case_attachment_seq", sequenceName = "info_alert.cm_case_attachment_attachment_id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "attachment_id")
    private Long attachmentId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}