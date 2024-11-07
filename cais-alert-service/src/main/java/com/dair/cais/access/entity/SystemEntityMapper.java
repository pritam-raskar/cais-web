package com.dair.cais.access.entity;

import org.springframework.stereotype.Component;

@Component
public class SystemEntityMapper {

    public SystemEntity toDto(SystemEntityJpa entity) {
        if (entity == null) {
            return null;
        }

        SystemEntity dto = new SystemEntity();
        dto.setEntityId(entity.getEntityId());
        dto.setEntityType(entity.getEntityType());
        dto.setEntityName(entity.getEntityName());
        dto.setDescription(entity.getDescription());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public SystemEntityJpa toEntity(SystemEntity dto) {
        if (dto == null) {
            return null;
        }

        SystemEntityJpa entity = new SystemEntityJpa();
        entity.setEntityId(dto.getEntityId());
        entity.setEntityType(dto.getEntityType());
        entity.setEntityName(dto.getEntityName());
        entity.setDescription(dto.getDescription());
        entity.setIsActive(dto.getIsActive());

        return entity;
    }
}