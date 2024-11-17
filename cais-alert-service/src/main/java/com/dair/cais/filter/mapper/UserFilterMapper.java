package com.dair.cais.filter.mapper;

import com.dair.cais.filter.domain.FilterEntityType;
import com.dair.cais.filter.domain.UserSavedFilter;
import com.dair.cais.filter.dto.*;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;

@Component
public class UserFilterMapper {

    public UserSavedFilter toEntity(UserFilterCreateDto dto, String userId, FilterEntityType entityType) {
        UserSavedFilter entity = new UserSavedFilter();
        entity.setUserId(userId);
        entity.setEntityType(entityType);
        entity.setEntityIdentifier(dto.getEntityIdentifier());
        entity.setFilterName(dto.getFilterName());
        entity.setFilterDescription(dto.getFilterDescription());
        entity.setIsDefault(dto.getIsDefault());
        entity.setIsPublic(dto.getIsPublic());
        entity.setFilterConfig(dto.getFilterConfig());
        entity.setCreatedBy(userId);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return entity;
    }

    public void updateEntity(UserSavedFilter entity, UserFilterUpdateDto dto, String userId) {
        if (dto.getFilterName() != null) {
            entity.setFilterName(dto.getFilterName());
        }
        if (dto.getFilterDescription() != null) {
            entity.setFilterDescription(dto.getFilterDescription());
        }
        if (dto.getIsDefault() != null) {
            entity.setIsDefault(dto.getIsDefault());
        }
        if (dto.getIsPublic() != null) {
            entity.setIsPublic(dto.getIsPublic());
        }
        if (dto.getFilterConfig() != null) {
            entity.setFilterConfig(dto.getFilterConfig());
        }
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    }

    public FilterResponseDto toDto(UserSavedFilter entity) {
        FilterResponseDto dto = new FilterResponseDto();
        dto.setFilterId(entity.getFilterId());
        dto.setUserId(entity.getUserId());
        dto.setEntityTypeId(entity.getEntityType().getEntityTypeId());
        dto.setEntityName(entity.getEntityType().getEntityName());
        dto.setEntityIdentifier(entity.getEntityIdentifier());
        dto.setFilterName(entity.getFilterName());
        dto.setFilterDescription(entity.getFilterDescription());
        dto.setIsDefault(entity.getIsDefault());
        dto.setIsPublic(entity.getIsPublic());
        dto.setFilterConfig(entity.getFilterConfig()); // Fixed: Now passing the actual filterConfig
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    public FilterSummaryDto toSummaryDto(UserSavedFilter entity) {
        FilterSummaryDto dto = new FilterSummaryDto();
        dto.setFilterId(entity.getFilterId());
        dto.setFilterName(entity.getFilterName());
        dto.setFilterDescription(entity.getFilterDescription());
        dto.setIsDefault(entity.getIsDefault());
        dto.setIsPublic(entity.getIsPublic());
        dto.setEntityName(entity.getEntityType().getEntityName());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    // Optional: If you need to handle FilterConfig conversion
    private FilterConfigDto deserializeFilterConfig(String filterConfigJson) {
        // Implement if needed - convert JSON string to FilterConfigDto
        return null;
    }

    private String serializeFilterConfig(FilterConfigDto filterConfig) {
        // Implement if needed - convert FilterConfigDto to JSON string
        return null;
    }
}