package com.dair.cais.access.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_entities", schema = "info_alert")
public class SystemEntityJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_seq")
    @SequenceGenerator(
            name = "entity_seq",
            sequenceName = "info_alert.cm_entities_seq",
            allocationSize = 1,
            schema = "info_alert"
    )
    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "entity_type", nullable = false, unique = true)
    private String entityType;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}