package com.dair.cais.connection.metadata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class ConnectionMetadataMapper {

    public ConnectionMetadata toDto(ConnectionMetadataEntity entity) {
        if (entity == null) {
            log.debug("Mapping null entity to DTO");
            return null;
        }

        log.trace("Mapping entity to DTO: {}", entity.getConnectionType());
        ConnectionMetadata dto = new ConnectionMetadata();
        dto.setConnectionType(entity.getConnectionType());
        dto.setJsonStructure(entity.getJsonStructure());
        return dto;
    }

    public ConnectionMetadataEntity toEntity(ConnectionMetadata dto) {
        if (dto == null) {
            log.debug("Mapping null DTO to entity");
            return null;
        }

        log.trace("Mapping DTO to entity: {}", dto.getConnectionType());
        ConnectionMetadataEntity entity = new ConnectionMetadataEntity();
        entity.setConnectionType(dto.getConnectionType());
        entity.setJsonStructure(dto.getJsonStructure());
        return entity;
    }
}