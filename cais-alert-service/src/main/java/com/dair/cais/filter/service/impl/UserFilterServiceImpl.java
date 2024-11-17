package com.dair.cais.filter.service.impl;

import com.dair.cais.filter.domain.FilterEntityType;
import com.dair.cais.filter.domain.UserSavedFilter;
import com.dair.cais.filter.dto.FilterResponseDto;
import com.dair.cais.filter.dto.FilterSummaryDto;
import com.dair.cais.filter.dto.UserFilterCreateDto;
import com.dair.cais.filter.dto.UserFilterUpdateDto;
import com.dair.cais.filter.exception.FilterNotFoundException;
import com.dair.cais.filter.exception.FilterOperationException;
import com.dair.cais.filter.exception.FilterValidationException;
import com.dair.cais.filter.mapper.UserFilterMapper;
import com.dair.cais.filter.repository.FilterEntityTypeRepository;
import com.dair.cais.filter.repository.UserFilterRepository;
import com.dair.cais.filter.service.FilterValidationService;
import com.dair.cais.filter.service.UserFilterService;
import com.dair.cais.filter.util.FilterValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFilterServiceImpl implements UserFilterService {
    private final UserFilterRepository userFilterRepository;
    private final FilterEntityTypeRepository filterEntityTypeRepository;
    private final UserFilterMapper userFilterMapper;
    private final FilterValidationService filterValidationService;
    private final FilterValidator filterValidator;

    @Override
    @Transactional
    public FilterResponseDto createFilter(String userId, UserFilterCreateDto createDto) {
        FilterEntityType entityType = filterEntityTypeRepository.findByEntityNameIgnoreCase(createDto.getEntityIdentifier())
                .orElseThrow(() -> new FilterOperationException("Entity type not found: " + createDto.getEntityIdentifier()));

        validateFilterName(userId, entityType.getEntityTypeId(), createDto.getEntityIdentifier(), createDto.getFilterName());
//        filterValidationService.validateFilterConfig(createDto.getFilterConfig());
//        filterValidator.validateFieldNames(createDto.getFilterConfig().getRules());

        UserSavedFilter filter = userFilterMapper.toEntity(createDto, userId, entityType);
        UserSavedFilter savedFilter = userFilterRepository.save(filter);
        log.info("Filter created successfully: {}", savedFilter.getFilterId());
        return userFilterMapper.toDto(savedFilter);
    }

    @Override
    @Transactional
    public FilterResponseDto updateFilter(String userId, Long filterId, UserFilterUpdateDto updateDto) {
        UserSavedFilter filter = userFilterRepository.findByFilterIdAndUserId(filterId, userId)
                .orElseThrow(() -> new FilterNotFoundException(filterId));

//        filterValidationService.validateFilterConfig(updateDto.getFilterConfig());
//        filterValidator.validateFieldNames(updateDto.getFilterConfig().getRules());

        userFilterMapper.updateEntity(filter, updateDto, userId);
        UserSavedFilter updatedFilter = userFilterRepository.save(filter);
        log.info("Filter updated successfully: {}", updatedFilter.getFilterId());
        return userFilterMapper.toDto(updatedFilter);
    }

    @Override
    @Transactional
    public void deleteFilter(String userId, Long filterId) {
        UserSavedFilter filter = userFilterRepository.findByFilterIdAndUserId(filterId, userId)
                .orElseThrow(() -> new FilterNotFoundException(filterId));

        userFilterRepository.delete(filter);
        log.info("Filter deleted successfully: {}", filterId);
    }

    @Override
    public FilterResponseDto getFilter(String userId, Long filterId) {
        UserSavedFilter filter = userFilterRepository.findByFilterIdAndUserId(filterId, userId)
                .orElseThrow(() -> new FilterNotFoundException(filterId));
        return userFilterMapper.toDto(filter);
    }

    @Override
    public List<FilterResponseDto> getUserFilters(String userId, Long entityTypeId, String entityIdentifier) {
        List<UserSavedFilter> filters = userFilterRepository.findByUserIdAndEntityType_EntityTypeIdAndEntityIdentifier(userId, entityTypeId, entityIdentifier);
        return filters.stream()
                .map(userFilterMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FilterResponseDto> getPublicFilters(Long entityTypeId, String entityIdentifier) {
        List<UserSavedFilter> filters = userFilterRepository.findByIsPublicTrueAndEntityType_EntityTypeIdAndEntityIdentifier(entityTypeId, entityIdentifier);
        return filters.stream()
                .map(userFilterMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FilterSummaryDto> searchFilters(String searchTerm, Pageable pageable) {
        Page<UserSavedFilter> filterPage = userFilterRepository.searchFilters(searchTerm, pageable);
        return filterPage.map(userFilterMapper::toSummaryDto);
    }

    @Override
    @Transactional
    public FilterResponseDto setDefaultFilter(String userId, Long filterId) {
//        UserSavedFilter filter = userFilterRepository.findByUserIdAndEntityType_EntityTypeIdAndEntityIdentifierAndIsDefault(userId, filter.getEntityType().getEntityTypeId(), filter.getEntityIdentifier(), true)
//                .ifPresent(f -> {
//                    f.setIsDefault(false);
//                    userFilterRepository.save(f);
//                });

        UserSavedFilter newDefaultFilter = userFilterRepository.findByFilterIdAndUserId(filterId, userId)
                .orElseThrow(() -> new FilterNotFoundException(filterId));
        newDefaultFilter.setIsDefault(true);
        UserSavedFilter savedFilter = userFilterRepository.save(newDefaultFilter);
        log.info("Filter set as default: {}", savedFilter.getFilterId());
        return userFilterMapper.toDto(savedFilter);
    }

    @Override
    @Transactional
    public FilterResponseDto togglePublicAccess(String userId, Long filterId) {
        UserSavedFilter filter = userFilterRepository.findByFilterIdAndUserId(filterId, userId)
                .orElseThrow(() -> new FilterNotFoundException(filterId));
        filter.setIsPublic(!filter.getIsPublic());
        UserSavedFilter savedFilter = userFilterRepository.save(filter);
        log.info("Filter public access toggled: {}", savedFilter.getFilterId());
        return userFilterMapper.toDto(savedFilter);
    }

    @Override
    @Transactional
    public FilterResponseDto copyFilter(String userId, Long filterId, String newName) {
        UserSavedFilter originalFilter = userFilterRepository.findByFilterIdAndUserId(filterId, userId)
                .orElseThrow(() -> new FilterNotFoundException(filterId));

        validateFilterName(userId, originalFilter.getEntityType().getEntityTypeId(), originalFilter.getEntityIdentifier(), newName);

        UserSavedFilter copiedFilter = new UserSavedFilter();
        copiedFilter.setUserId(userId);
        copiedFilter.setEntityType(originalFilter.getEntityType());
        copiedFilter.setEntityIdentifier(originalFilter.getEntityIdentifier());
        copiedFilter.setFilterName(newName);
        copiedFilter.setFilterDescription(originalFilter.getFilterDescription());
        copiedFilter.setIsDefault(false);
        copiedFilter.setIsPublic(originalFilter.getIsPublic());
        copiedFilter.setFilterConfig(originalFilter.getFilterConfig());
        copiedFilter.setCreatedBy(userId);
        copiedFilter.setCreatedAt(originalFilter.getCreatedAt());

        UserSavedFilter savedCopiedFilter = userFilterRepository.save(copiedFilter);
        log.info("Filter copied successfully: {}", savedCopiedFilter.getFilterId());
        return userFilterMapper.toDto(savedCopiedFilter);
    }

    private void validateFilterName(String userId, Long entityTypeId, String entityIdentifier, String filterName) {
        if (userFilterRepository.existsByUserIdAndEntityType_EntityTypeIdAndEntityIdentifierAndFilterNameIgnoreCase(userId, entityTypeId, entityIdentifier, filterName)) {
            throw new FilterValidationException("Duplicate filter name", List.of("Filter name already exists"));
        }
    }

    @Override
    public List<FilterResponseDto> getAllFilters() {
        List<UserSavedFilter> allFilters = userFilterRepository.findAll();
        return allFilters.stream()
                .map(userFilterMapper::toDto)
                .collect(Collectors.toList());
    }
}