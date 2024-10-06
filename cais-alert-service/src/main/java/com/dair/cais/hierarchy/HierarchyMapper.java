package com.dair.cais.hierarchy;

import org.springframework.stereotype.Component;

@Component
public class HierarchyMapper {

    public Hierarchy toDto(HierarchyEntity entity) {
        if (entity == null) {
            return null;
        }

        Hierarchy dto = new Hierarchy();
        dto.setHierarchyId(entity.getHierarchyId());
        dto.setHierarchyKey(entity.getHierarchyKey());
        dto.setHierarchyName(entity.getHierarchyName());
        dto.setDescription(entity.getDescription());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public HierarchyEntity toEntity(Hierarchy dto) {
        if (dto == null) {
            return null;
        }

        HierarchyEntity entity = new HierarchyEntity();
        entity.setHierarchyId(dto.getHierarchyId());
        entity.setHierarchyKey(dto.getHierarchyKey());
        entity.setHierarchyName(dto.getHierarchyName());
        entity.setDescription(dto.getDescription());
        entity.setIsActive(dto.getIsActive());
        // Note: createdAt and updatedAt are not set here as they should be managed by the database
        return entity;
    }

    public void updateEntityFromDto(Hierarchy dto, HierarchyEntity entity) {
        if (dto == null || entity == null) {
            return;
        }

        // Don't update hierarchyId as it's the primary key
        entity.setHierarchyKey(dto.getHierarchyKey());
        entity.setHierarchyName(dto.getHierarchyName());
        entity.setDescription(dto.getDescription());
        entity.setIsActive(dto.getIsActive());
        // Note: createdAt and updatedAt are not updated here as they should be managed by the database
    }
}