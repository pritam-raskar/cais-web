package com.dair.cais.filter.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_saved_filters", schema = "info_alert")
public class UserSavedFilter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_id")
    private Long filterId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "entity_type_id", nullable = false)
    private FilterEntityType entityType;

    @Column(name = "entity_identifier", nullable = false)
    private String entityIdentifier;

    @Column(name = "source_identifier")
    @Comment("Stores reference ID for specific entity types (e.g., report_id for Analytics)")
    private String sourceIdentifier;

    @Column(name = "filter_name", nullable = false)
    private String filterName;

    @Column(name = "filter_description")
    private String filterDescription;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "filter_config", nullable = false)
    private String filterConfig;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
}