package com.dair.cais.filter.mapper;

import com.dair.cais.filter.domain.FilterEntityType;
import com.dair.cais.filter.domain.UserSavedFilter;
import com.dair.cais.filter.dto.*;
import com.dair.cais.filter.service.SourceIdentifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFilterMapper {
    private final SourceIdentifierService sourceIdentifierService;

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

        // Set source identifier for Analytics entity type using sourceIdentifier from request
        if ("Analytics".equalsIgnoreCase(entityType.getEntityName())) {
            try {
                String reportId = sourceIdentifierService.getSourceIdentifierForAnalytics(dto.getSourceIdentifier());
                entity.setSourceIdentifier(reportId);
                log.debug("Set source identifier {} for Analytics filter with source identifier {}",
                        reportId, dto.getSourceIdentifier());
            } catch (Exception e) {
                log.error("Failed to set source identifier for Analytics filter with source identifier {}: {}",
                        dto.getSourceIdentifier(), e.getMessage());
            }
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return entity;
    }

    public void updateEntity(UserSavedFilter entity, UserFilterUpdateDto dto, String userId) {
        boolean isAnalytics = "Analytics".equalsIgnoreCase(entity.getEntityType().getEntityName());

        if (dto.getFilterName() != null) {
            entity.setFilterName(dto.getFilterName());
            log.debug("Updated filter name to: {}", dto.getFilterName());
        }

        if (dto.getFilterDescription() != null) {
            entity.setFilterDescription(dto.getFilterDescription());
            log.debug("Updated filter description");
        }

        if (dto.getIsDefault() != null) {
            entity.setIsDefault(dto.getIsDefault());
            log.debug("Updated isDefault to: {}", dto.getIsDefault());
        }

        if (dto.getIsPublic() != null) {
            entity.setIsPublic(dto.getIsPublic());
            log.debug("Updated isPublic to: {}", dto.getIsPublic());
        }

        if (dto.getFilterConfig() != null) {
            entity.setFilterConfig(dto.getFilterConfig());
            log.debug("Updated filter configuration");
        }

        // Handle sourceIdentifier update for Analytics entity type
        if (isAnalytics && dto.getSourceIdentifier() != null) {
            try {
                String reportId = sourceIdentifierService.getSourceIdentifierForAnalytics(dto.getSourceIdentifier());
                entity.setSourceIdentifier(reportId);
                log.debug("Updated source identifier to {} for Analytics filter", reportId);
            } catch (Exception e) {
                log.error("Failed to update source identifier for Analytics filter: {}", e.getMessage());
            }
        }

        if (dto.getEntityIdentifier() != null) {
            entity.setEntityIdentifier(dto.getEntityIdentifier());
            log.debug("Updated entity identifier to: {}", dto.getEntityIdentifier());
        }

        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        log.debug("Updated metadata: updatedBy={}, updatedAt={}", userId, entity.getUpdatedAt());
    }

    public FilterResponseDto toDto(UserSavedFilter entity) {
        FilterResponseDto dto = new FilterResponseDto();
        dto.setFilterId(entity.getFilterId());
        dto.setUserId(entity.getUserId());
        dto.setEntityIdentifier(entity.getEntityIdentifier());
        dto.setSourceIdentifier(entity.getSourceIdentifier());
        dto.setFilterName(entity.getFilterName());
        dto.setFilterDescription(entity.getFilterDescription());
        dto.setIsDefault(entity.getIsDefault());
        dto.setIsPublic(entity.getIsPublic());
        dto.setFilterConfig(entity.getFilterConfig());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());

        log.trace("Mapped filter entity {} to DTO with sourceIdentifier {}",
                entity.getFilterId(), entity.getSourceIdentifier());

        return dto;
    }

    public FilterSummaryDto toSummaryDto(UserSavedFilter entity) {
        FilterSummaryDto dto = new FilterSummaryDto();
        dto.setFilterId(entity.getFilterId());
        dto.setFilterName(entity.getFilterName());
        dto.setFilterDescription(entity.getFilterDescription());
        dto.setIsDefault(entity.getIsDefault());
        dto.setIsPublic(entity.getIsPublic());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }
}