package com.dair.cais.cases.note.mapper;

import com.dair.cais.cases.note.CaseNote;
import com.dair.cais.cases.note.entity.CaseNoteEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between CaseNote DTOs and CaseNoteEntity objects.
 */
@Component
public class CaseNoteMapper {

    /**
     * Convert a case note entity to a case note DTO.
     *
     * @param entity the case note entity
     * @return the case note DTO
     */
    public CaseNote toModel(CaseNoteEntity entity) {
        if (entity == null) {
            return null;
        }

        CaseNote model = new CaseNote();
        model.setNoteId(entity.getNoteId());
        model.setCaseId(entity.getCaseId());
        model.setContent(entity.getContent());
        model.setCreatedBy(entity.getCreatedBy());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        return model;
    }

    /**
     * Convert a case note DTO to a case note entity.
     *
     * @param model the case note DTO
     * @return the case note entity
     */
    public CaseNoteEntity toEntity(CaseNote model) {
        if (model == null) {
            return null;
        }

        CaseNoteEntity entity = new CaseNoteEntity();
        entity.setNoteId(model.getNoteId());
        entity.setCaseId(model.getCaseId());
        entity.setContent(model.getContent());
        entity.setCreatedBy(model.getCreatedBy());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        return entity;
    }

    /**
     * Convert a list of case note entities to a list of case note DTOs.
     *
     * @param entities the list of case note entities
     * @return the list of case note DTOs
     */
    public List<CaseNote> toModelList(List<CaseNoteEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}