package com.dair.cais.access.alertType;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cm_alert_type", schema = "info_alert")
public class alertTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cm_alert_type_seq")
    @SequenceGenerator(name = "cm_alert_type_seq", sequenceName = "info_alert.cm_alert_type_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "aty_id")
    private Integer atyId;

    @Column(name = "alert_type_id", unique = true, nullable = false)
    private String alertTypeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "description")
    private String description;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "type_slug", nullable = false)
    private String typeSlug;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = true)
    private Boolean isActive;



    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}