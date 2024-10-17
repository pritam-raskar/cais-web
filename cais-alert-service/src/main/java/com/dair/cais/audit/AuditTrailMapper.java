package com.dair.cais.audit;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditTrailMapper {

    public AuditTrail entityToDto(AuditTrailEntity entity) {
        if (entity == null) {
            return null;
        }

        AuditTrail dto = new AuditTrail();
        dto.setAuditId(entity.getAuditId());
        dto.setActionId(entity.getAction() != null ? entity.getAction().getActionId() : null);
        dto.setUserId(entity.getUserId());
        dto.setUserRole(entity.getUserRole());
        dto.setActionTimestamp(entity.getActionTimestamp());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setAffectedItemType(entity.getAffectedItemType());
        dto.setAffectedItemId(entity.getAffectedItemId());
        dto.setOldValue(entity.getOldValue());
        dto.setNewValue(entity.getNewValue());

        return dto;
    }

    public AuditTrailEntity dtoToEntity(AuditTrail dto) {
        if (dto == null) {
            return null;
        }

        AuditTrailEntity entity = new AuditTrailEntity();
        entity.setAuditId(dto.getAuditId());
        // We'll need to set the Action separately, as we don't have the full ActionEntity here
        entity.setUserId(dto.getUserId());
        entity.setUserRole(dto.getUserRole());
        entity.setActionTimestamp(dto.getActionTimestamp());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setAffectedItemType(dto.getAffectedItemType());
        entity.setAffectedItemId(dto.getAffectedItemId());
        entity.setOldValue(dto.getOldValue());
        entity.setNewValue(dto.getNewValue());

        return entity;
    }

    public List<AuditTrail> entitiesToDtos(List<AuditTrailEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }
}