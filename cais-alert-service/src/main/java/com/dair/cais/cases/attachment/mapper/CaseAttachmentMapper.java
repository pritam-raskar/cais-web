package com.dair.cais.cases.attachment.mapper;

import com.dair.cais.cases.attachment.CaseAttachment;
import com.dair.cais.cases.attachment.entity.CaseAttachmentEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between CaseAttachment DTOs and CaseAttachmentEntity objects.
 */
@Component
public class CaseAttachmentMapper {

    /**
     * Convert a case attachment entity to a case attachment DTO.
     *
     * @param entity the case attachment entity
     * @return the case attachment DTO
     */
    public CaseAttachment toModel(CaseAttachmentEntity entity) {
        if (entity == null) {
            return null;
        }

        CaseAttachment model = new CaseAttachment();
        model.setAttachmentId(entity.getAttachmentId());
        model.setCaseId(entity.getCaseId());
        model.setFileName(entity.getFileName());
        model.setFilePath(entity.getFilePath());
        model.setFileType(entity.getFileType());
        model.setFileSize(entity.getFileSize());
        model.setComment(entity.getComment());
        model.setUploadedBy(entity.getUploadedBy());
        model.setUploadedAt(entity.getUploadedAt());

        return model;
    }

    /**
     * Convert a case attachment DTO to a case attachment entity.
     *
     * @param model the case attachment DTO
     * @return the case attachment entity
     */
    public CaseAttachmentEntity toEntity(CaseAttachment model) {
        if (model == null) {
            return null;
        }

        CaseAttachmentEntity entity = new CaseAttachmentEntity();
        entity.setAttachmentId(model.getAttachmentId());
        entity.setCaseId(model.getCaseId());
        entity.setFileName(model.getFileName());
        entity.setFilePath(model.getFilePath());
        entity.setFileType(model.getFileType());
        entity.setFileSize(model.getFileSize());
        entity.setComment(model.getComment());
        entity.setUploadedBy(model.getUploadedBy());
        entity.setUploadedAt(model.getUploadedAt());

        return entity;
    }

    /**
     * Convert a list of case attachment entities to a list of case attachment DTOs.
     *
     * @param entities the list of case attachment entities
     * @return the list of case attachment DTOs
     */
    public List<CaseAttachment> toModelList(List<CaseAttachmentEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}