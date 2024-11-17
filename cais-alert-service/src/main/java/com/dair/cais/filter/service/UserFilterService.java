// File: com/dair/cais/filter/service/UserFilterService.java
package com.dair.cais.filter.service;

import com.dair.cais.filter.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserFilterService {
    FilterResponseDto createFilter(String userId, UserFilterCreateDto createDto);
    FilterResponseDto updateFilter(String userId, Long filterId, UserFilterUpdateDto updateDto);
    void deleteFilter(String userId, Long filterId);
    FilterResponseDto getFilter(String userId, Long filterId);
    List<FilterResponseDto> getUserFilters(String userId, Long entityTypeId, String entityIdentifier);
    List<FilterResponseDto> getPublicFilters(Long entityTypeId, String entityIdentifier);
    Page<FilterSummaryDto> searchFilters(String searchTerm, Pageable pageable);
    FilterResponseDto setDefaultFilter(String userId, Long filterId);
    FilterResponseDto togglePublicAccess(String userId, Long filterId);
    FilterResponseDto copyFilter(String userId, Long filterId, String newName);
    List<FilterResponseDto> getAllFilters();
}
