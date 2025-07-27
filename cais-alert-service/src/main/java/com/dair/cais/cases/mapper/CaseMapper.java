package com.dair.cais.cases.mapper;

import com.dair.cais.cases.Case;
import com.dair.cais.cases.entity.CaseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Case DTOs and Case Entities.
 */
@Component
public class CaseMapper {

    /**
     * Convert a case entity to a case DTO.
     *
     * @param entity the case entity
     * @return the case DTO
     */
    public Case toModel(CaseEntity entity) {
        if (entity == null) {
            return null;
        }

        Case model = new Case();
        model.setCaseId(entity.getCaseId());
        model.setCaseNumber(entity.getCaseNumber());
        model.setTitle(entity.getTitle());
        model.setDescription(entity.getDescription());
        model.setStatus(entity.getStatus());
        model.setPriority(entity.getPriority());
        model.setCaseType(entity.getCaseType());
        model.setOrgUnitId(entity.getOrgUnitId());
        model.setOrgFamily(entity.getOrgFamily());
        model.setOwnerId(entity.getOwnerId());
        model.setOwnerName(entity.getOwnerName());
        // model.setWorkflowId(entity.getWorkflowId()); // Not required here, get it by joining type
        model.setCurrentStepId(entity.getCurrentStepId());
        model.setCurrentStepName(entity.getCurrentStepName());
        model.setDueDate(entity.getDueDate());
        model.setResolution(entity.getResolution());
        model.setCreatedBy(entity.getCreatedBy());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setClosedAt(entity.getClosedAt());
        model.setIsActive(entity.getIsActive());

        return model;
    }

    /**
     * Convert a case DTO to a case entity.
     *
     * @param model the case DTO
     * @return the case entity
     */
    public CaseEntity toEntity(Case model) {
        if (model == null) {
            return null;
        }

        CaseEntity entity = new CaseEntity();
        entity.setCaseId(model.getCaseId());
        entity.setCaseNumber(model.getCaseNumber());
        entity.setTitle(model.getTitle());
        entity.setDescription(model.getDescription());
        entity.setStatus(model.getStatus());
        entity.setPriority(model.getPriority());
        entity.setCaseType(model.getCaseType());
        entity.setOrgUnitId(model.getOrgUnitId());
        entity.setOrgFamily(model.getOrgFamily());
        entity.setOwnerId(model.getOwnerId());
        entity.setOwnerName(model.getOwnerName());
        // entity.setWorkflowId(model.getWorkflowId());
        entity.setCurrentStepId(model.getCurrentStepId());
        entity.setCurrentStepName(model.getCurrentStepName());
        entity.setDueDate(model.getDueDate());
        entity.setResolution(model.getResolution());
        entity.setCreatedBy(model.getCreatedBy());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        entity.setClosedAt(model.getClosedAt());
        entity.setIsActive(model.getIsActive());

        return entity;
    }

    /**
     * Convert a list of case entities to a list of case DTOs.
     *
     * @param entities the list of case entities
     * @return the list of case DTOs
     */
    public List<Case> toModelList(List<CaseEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}