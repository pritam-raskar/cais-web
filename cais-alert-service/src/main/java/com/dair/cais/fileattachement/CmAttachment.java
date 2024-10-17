package com.dair.cais.fileattachement;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cm_attachments", schema = "info_alert")
public class CmAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cm_attachment_seq")
    @SequenceGenerator(name = "cm_attachment_seq", sequenceName = "info_alert.cm_attachment_seq", allocationSize = 1)
    @Column(name = "attachment_id")
    private Long attachmentId;

    @Column(name = "alert_id", nullable = false)
    private String alertId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "comment")
    private String comment;

    @Column(name = "file_path")
    private String filePath;

    // Getters and setters for all fields

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "CmAttachment{" +
                "attachmentId=" + attachmentId +
                ", alertId='" + alertId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", createdDate=" + createdDate +
                ", createdBy='" + createdBy + '\'' +
                ", comment='" + comment + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}