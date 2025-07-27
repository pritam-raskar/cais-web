package com.dair.cais.cases.service;

import com.dair.cais.cases.CaseType;
import com.dair.cais.cases.entity.CaseTypeEntity;
import com.dair.cais.cases.mapper.CaseTypeMapper;
import com.dair.cais.cases.repository.CaseTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Case Type management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseTypeService {

    private final CaseTypeRepository caseTypeRepository;
    private final CaseTypeMapper caseTypeMapper;

    /**
     * Creates a new case type.
     *
     * @param caseType the case type data to create
     * @return the created case type
     * @throws IllegalArgumentException if a case type with the same name already exists
     */
    @Transactional
    public CaseType createCaseType(CaseType caseType) {
        log.debug("Creating new case type: {}", caseType);

        validateCaseType(caseType);

        // Check if a case type with the same name already exists
        if (caseTypeRepository.findByName(caseType.getName()).isPresent()) {
            log.error("Case type with name '{}' already exists", caseType.getName());
            throw new IllegalArgumentException("Case type with name '" + caseType.getName() + "' already exists");
        }

        // Set default values
        if (caseType.getIsActive() == null) {
            caseType.setIsActive(true);
        }

        CaseTypeEntity entity = caseTypeMapper.toEntity(caseType);
        CaseTypeEntity savedEntity = caseTypeRepository.save(entity);

        log.info("Created new case type with ID: {}", savedEntity.getTypeId());
        return caseTypeMapper.toModel(savedEntity);
    }

    /**
     * Retrieves a case type by its ID.
     *
     * @param typeId the case type ID
     * @return the case type
     * @throws EntityNotFoundException if the case type is not found
     */
    @Transactional(readOnly = true)
    public CaseType getCaseType(Long typeId) {
        log.debug("Fetching case type with ID: {}", typeId);

        CaseTypeEntity entity = caseTypeRepository.findById(typeId)
                .orElseThrow(() -> {
                    log.error("Case type not found with ID: {}", typeId);
                    return new EntityNotFoundException("Case type not found with ID: " + typeId);
                });

        return caseTypeMapper.toModel(entity);
    }

    /**
     * Retrieves all case types.
     *
     * @return the list of all case types
     */
    @Transactional(readOnly = true)
    public List<CaseType> getAllCaseTypes() {
        log.debug("Fetching all case types");

        List<CaseTypeEntity> entities = caseTypeRepository.findAll();
        return caseTypeMapper.toModelList(entities);
    }

    /**
     * Updates an existing case type.
     *
     * @param typeId the case type ID
     * @param caseType the updated case type data
     * @return the updated case type
     * @throws EntityNotFoundException if the case type is not found
     * @throws IllegalArgumentException if another case type with the same name already exists
     */
    @Transactional
    public CaseType updateCaseType(Long typeId, CaseType caseType) {
        log.debug("Updating case type with ID: {}", typeId);

        validateCaseType(caseType);

        CaseTypeEntity existingEntity = caseTypeRepository.findById(typeId)
                .orElseThrow(() -> {
                    log.error("Case type not found with ID: {}", typeId);
                    return new EntityNotFoundException("Case type not found with ID: " + typeId);
                });

        // Check if another case type with the same name already exists
        caseTypeRepository.findByName(caseType.getName())
                .ifPresent(entity -> {
                    if (!entity.getTypeId().equals(typeId)) {
                        log.error("Another case type with name '{}' already exists", caseType.getName());
                        throw new IllegalArgumentException("Another case type with name '" + caseType.getName() + "' already exists");
                    }
                });

        CaseTypeEntity updateEntity = caseTypeMapper.toEntity(caseType);
        updateEntity.setTypeId(typeId);
        updateEntity.setUpdatedAt(LocalDateTime.now());

        CaseTypeEntity savedEntity = caseTypeRepository.save(updateEntity);

        log.info("Updated case type with ID: {}", typeId);
        return caseTypeMapper.toModel(savedEntity);
    }

    /**
     * Deletes a case type.
     *
     * @param typeId the case type ID
     * @throws EntityNotFoundException if the case type is not found
     */
    @Transactional
    public void deleteCaseType(Long typeId) {
        log.debug("Deleting case type with ID: {}", typeId);

        // Check if case type exists
        if (!caseTypeRepository.existsById(typeId)) {
            log.error("Case type not found with ID: {}", typeId);
            throw new EntityNotFoundException("Case type not found with ID: " + typeId);
        }

        // TODO: Check if this case type is in use by any cases
        // If so, throw an exception or deactivate instead of deleting

        caseTypeRepository.deleteById(typeId);
        log.info("Deleted case type with ID: {}", typeId);
    }

    /**
     * Validates case type data.
     *
     * @param caseType the case type data to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCaseType(CaseType caseType) {
        if (caseType == null) {
            throw new IllegalArgumentException("Case type data cannot be null");
        }

        if (caseType.getName() == null || caseType.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Case type name is required");
        }

        // Add more validation as needed
    }
}