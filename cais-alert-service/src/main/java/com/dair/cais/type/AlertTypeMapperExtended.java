package com.dair.cais.type;

import org.springframework.stereotype.Component;

@Component
public class AlertTypeMapperExtended {
    public AlertTypeExtended toModel(AlertTypeEntityExtended entity) {
        AlertTypeExtended type = new AlertTypeExtended();
        type.setAlertTypeId(entity.getAlertTypeId());
        type.setTypeSlug(entity.getTypeSlug());
        type.setTypeName(entity.getTypeName());
        type.setDescription(entity.getDescription());
        type.setActive(entity.isActive());
        type.setExtraField(entity.getExtraField());
        type.setField_schema(entity.getField_schema());
        type.setMandatory_fields(entity.getMandatory_fields());
        type.setCreatedAt(entity.getCreatedAt());
        type.setUpdatedAt(entity.getUpdatedAt());

        return type;
    }

    private void mapAlertTypeToEntity(AlertTypeExtended alertType, AlertTypeEntityExtended entity) {
        entity.setAlertTypeId(alertType.getAlertTypeId());
        entity.setTypeSlug(alertType.getTypeSlug());
        entity.setTypeName(alertType.getTypeName());
        entity.setDescription(alertType.getDescription());
        entity.setActive(alertType.isActive());
        entity.setExtraField(alertType.getExtraField());
        entity.setField_schema(alertType.getField_schema());
        entity.setMandatory_fields(alertType.getMandatory_fields());
        entity.setCreatedAt(alertType.getCreatedAt());
        entity.setUpdatedAt(alertType.getUpdatedAt());
    }

    public AlertTypeEntityExtended toEntity(AlertTypeExtended type) {
        AlertTypeEntityExtended typeEntity = new AlertTypeEntityExtended();
        mapAlertTypeToEntity(type, typeEntity);

        return typeEntity;
    }
}
