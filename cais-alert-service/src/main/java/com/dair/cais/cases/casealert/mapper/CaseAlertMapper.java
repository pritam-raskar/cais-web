package com.dair.cais.cases.casealert.mapper;

import com.dair.cais.cases.casealert.CaseAlert;
import com.dair.cais.cases.casealert.entity.CaseAlertEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between CaseAlert DTOs and CaseAlertEntity objects.
 */
@Component
public class CaseAlertMapper {

    /**
     * Convert a case-alert entity to a case-alert DTO.
     *
     * @param entity the case-alert entity
     * @return the case-alert DTO
     */
    public CaseAlert toModel(CaseAlertEntity entity) {
        if (entity == null) {
            return null;
        }

        CaseAlert model = new CaseAlert();
        model.setId(entity.getId());
        model.setCaseId(entity.getCaseId());
        model.setAlertId(entity.getAlertId());
        model.setAddedBy(entity.getAddedBy());
        model.setAddedAt(entity.getAddedAt());
        model.setReason(entity.getReason());

        return model;
    }

    /**
     * Convert a case-alert DTO to a case-alert entity.
     *
     * @param model the case-alert DTO
     * @return the case-alert entity
     */
    public CaseAlertEntity toEntity(CaseAlert model) {
        if (model == null) {
            return null;
        }

        CaseAlertEntity entity = new CaseAlertEntity();
        entity.setId(model.getId());
        entity.setCaseId(model.getCaseId());
        entity.setAlertId(model.getAlertId());
        entity.setAddedBy(model.getAddedBy());
        entity.setAddedAt(model.getAddedAt());
        entity.setReason(model.getReason());

        return entity;
    }

    /**
     * Convert a list of case-alert entities to a list of case-alert DTOs.
     *
     * @param entities the list of case-alert entities
     * @return the list of case-alert DTOs
     */
    public List<CaseAlert> toModelList(List<CaseAlertEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}