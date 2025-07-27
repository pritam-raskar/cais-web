package com.dair.cais.cases.mapper;

import com.dair.cais.cases.CaseType;
import com.dair.cais.cases.entity.CaseTypeEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Case Type DTOs and Case Type Entities.
 */
@Component
public class CaseTypeMapper {

    /**
     * Convert a case type entity to a case type DTO.
     *
     * @param entity the case type entity
     * @return the case type DTO
     */
    public CaseType toModel(CaseTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        CaseType model = new CaseType();
        model.setTypeId(entity.getTypeId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setWorkflowId(entity.getWorkflowId());
        model.setIsActive(entity.getIsActive());

        return model;
    }

    /**
     * Convert a case type DTO to a case type entity.
     *
     * @param model the case type DTO
     * @return the case type entity
     */
    public CaseTypeEntity toEntity(CaseType model) {
        if (model == null) {
            return null;
        }

        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setTypeId(model.getTypeId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setWorkflowId(model.getWorkflowId());
        entity.setIsActive(model.getIsActive());

        return entity;
    }

    /**
     * Convert a list of case type entities to a list of case type DTOs.
     *
     * @param entities the list of case type entities
     * @return the list of case type DTOs
     */
    public List<CaseType> toModelList(List<CaseTypeEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}