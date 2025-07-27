package com.dair.cais.cases.note.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity class for Case Notes table in database.
 */
@Data
@Entity
@Table(name = "cm_case_note", schema = "info_alert")
@EntityListeners(AuditingEntityListener.class)
public class CaseNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_note_seq")
    @SequenceGenerator(name = "case_note_seq", sequenceName = "info_alert.cm_case_note_note_id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "note_id")
    private Long noteId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}