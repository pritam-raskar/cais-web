package com.dair.cais.connection.metadata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "cm_md_connection", schema = "info_alert")
class ConnectionMetadataEntity {
    @Id
    @Column(name = "connection_type")
    private String connectionType;

    @Column(name = "json_structure", length = 4000)
    private String jsonStructure;
}