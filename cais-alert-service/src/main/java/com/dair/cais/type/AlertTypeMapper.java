package com.dair.cais.type;

import org.springframework.stereotype.Component;

@Component
public class AlertTypeMapper {

    public AlertType toModel(AlertTypeEntity entity) {
        AlertType type = new AlertType();
        type.setId(String.valueOf(entity.getId()));
        type.setName(entity.getName());
        type.setDescription(entity.getDescription());
        type.setSlug(entity.getSlug());
        type.setActive(entity.isActive());
        type.setFields(entity.getFields());

        type.setCreatedDate(entity.getCreatedDate());
        type.setUpdatedDate(entity.getUpdatedDate());

        return type;
    }

    public AlertTypeEntity toEntity(AlertType type) {
        AlertTypeEntity typeEntity = new AlertTypeEntity();
        typeEntity.setId(type.getId());
        mapAlertTypeToEntity(type, typeEntity);

        return typeEntity;
    }

    public AlertTypeEntity toEntity(String typeId, AlertType type) {
        AlertTypeEntity typeEntity = new AlertTypeEntity();
        typeEntity.setId(typeId);
        mapAlertTypeToEntity(type, typeEntity);

        return typeEntity;

    }

    private void mapAlertTypeToEntity(AlertType alertType, AlertTypeEntity entity) {
        entity.setName(alertType.getName());
        entity.setDescription(alertType.getDescription());
        entity.setSlug(alertType.getSlug());
        entity.setActive(alertType.isActive());
        entity.setFields(alertType.getFields());

        entity.setCreatedDate(alertType.getCreatedDate());
        entity.setUpdatedDate(alertType.getUpdatedDate());
    }

}