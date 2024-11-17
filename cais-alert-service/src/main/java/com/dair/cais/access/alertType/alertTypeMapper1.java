package com.dair.cais.access.alertType;

import org.springframework.stereotype.Component;

@Component
public class alertTypeMapper1 {

    public alertType toDto(alertTypeEntity entity) {
        alertType dto = new alertType();
        dto.setAlertTypeId(entity.getAlertTypeId());
        dto.setDescription(entity.getDescription());
        dto.setTypeSlug(entity.getTypeSlug());
        dto.setTypeName(entity.getTypeName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setAtyId(entity.getAtyId());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }

    public alertTypeEntity toEntity(alertType dto) {
        alertTypeEntity entity = new alertTypeEntity();
        entity.setDescription(dto.getDescription());
        entity.setAlertTypeId(dto.getAlertTypeId());
        entity.setTypeSlug(dto.getTypeSlug());
        entity.setTypeName(dto.getTypeName());
        entity.setIsActive(dto.getIsActive());
        return entity;
    }
}
