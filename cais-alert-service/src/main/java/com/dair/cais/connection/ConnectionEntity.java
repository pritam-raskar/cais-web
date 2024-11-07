package com.dair.cais.connection;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "cm_connection", schema = "info_alert")
@Slf4j
public class ConnectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "connection_seq")
    @SequenceGenerator(name = "connection_seq", sequenceName = "info_alert.cm_common_sequence", allocationSize = 1, schema = "info_alert")
    @Column(name = "connection_id")
    private Long connectionId;

    @Column(name = "connection_name", nullable = false, unique = true)
    private String connectionName;

    @Column(name = "connection_type", nullable = false)
    @Convert(converter = ConnectionType.ConnectionTypeConverter.class)
    private ConnectionType connectionType;

    @Column(name = "iv", nullable = false)
    private String iv;

    @Column(name = "encrypted_data", nullable = false)
    private String encryptedData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
        log.debug("Creating new connection entity with name: {}", connectionName);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
        log.debug("Updating connection entity with ID: {}", connectionId);
    }
}