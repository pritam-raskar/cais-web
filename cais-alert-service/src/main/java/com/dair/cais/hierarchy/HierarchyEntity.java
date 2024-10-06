package com.dair.cais.hierarchy;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_hierarchy", schema = "info_alert")
public class HierarchyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hierarchy_seq")
    @SequenceGenerator(name = "hierarchy_seq", sequenceName = "info_alert.cm_hierarchy_hierarchy_id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "hierarchy_id")
    private Integer hierarchyId;

    @Column(name = "hierarchy_key", nullable = false, length = 255)
    private String hierarchyKey;

    @Column(name = "hierarchy_name", nullable = false, length = 255)
    private String hierarchyName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name ="create_date", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "update_date")
    private LocalDateTime updatedAt;
}
