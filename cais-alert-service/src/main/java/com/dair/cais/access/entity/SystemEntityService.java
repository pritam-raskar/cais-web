package com.dair.cais.access.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemEntityService {

    private final SystemEntityRepository systemEntityRepository;
    private final SystemEntityMapper systemEntityMapper;

    @Transactional(readOnly = true)
    public List<SystemEntity> getAllEntities() {
        log.debug("Fetching all entities");
        List<SystemEntity> entities = systemEntityRepository.findAll().stream()
                .map(systemEntityMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Found {} entities", entities.size());
        return entities;
    }

    @Transactional(readOnly = true)
    public List<SystemEntity> getActiveEntities() {
        log.debug("Fetching active entities");
        List<SystemEntity> entities = systemEntityRepository.findByIsActiveTrue().stream()
                .map(systemEntityMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Found {} active entities", entities.size());
        return entities;
    }

    @Transactional(readOnly = true)
    public SystemEntity getEntityById(Integer id) {
        log.debug("Fetching entity with ID: {}", id);
        return systemEntityRepository.findById(id)
                .map(systemEntityMapper::toDto)
                .orElseThrow(() -> {
                    log.error("Entity not found with ID: {}", id);
                    return new EntityNotFoundException("Entity not found with ID: " + id);
                });
    }

    @Transactional(readOnly = true)
    public SystemEntity getEntityByType(String entityType) {
        log.debug("Fetching entity with type: {}", entityType);
        return systemEntityRepository.findByEntityType(entityType)
                .map(systemEntityMapper::toDto)
                .orElseThrow(() -> {
                    log.error("Entity not found with type: {}", entityType);
                    return new EntityNotFoundException("Entity not found with type: " + entityType);
                });
    }

    @Transactional
    public SystemEntity createEntity(SystemEntity entity) {
        log.debug("Creating new entity: {}", entity);

        if (systemEntityRepository.existsByEntityType(entity.getEntityType())) {
            log.error("Entity type already exists: {}", entity.getEntityType());
            throw new IllegalArgumentException("Entity type already exists: " + entity.getEntityType());
        }

        SystemEntityJpa entityJpa = systemEntityMapper.toEntity(entity);
        entityJpa.setIsActive(true);
        SystemEntityJpa savedEntity = systemEntityRepository.save(entityJpa);
        log.info("Created new entity with ID: {}", savedEntity.getEntityId());
        return systemEntityMapper.toDto(savedEntity);
    }

    @Transactional
    public SystemEntity updateEntity(Integer id, SystemEntity entity) {
        log.debug("Updating entity with ID: {}", id);

        return systemEntityRepository.findById(id)
                .map(existingEntity -> {
                    existingEntity.setEntityName(entity.getEntityName());
                    existingEntity.setDescription(entity.getDescription());
                    existingEntity.setIsActive(entity.getIsActive());

                    SystemEntityJpa updatedEntity = systemEntityRepository.save(existingEntity);
                    log.info("Updated entity with ID: {}", updatedEntity.getEntityId());
                    return systemEntityMapper.toDto(updatedEntity);
                })
                .orElseThrow(() -> {
                    log.error("Entity not found with ID: {}", id);
                    return new EntityNotFoundException("Entity not found with ID: " + id);
                });
    }

    @Transactional
    public void deactivateEntity(Integer id) {
        log.debug("Deactivating entity with ID: {}", id);

        systemEntityRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(false);
                    SystemEntityJpa deactivatedEntity = systemEntityRepository.save(entity);
                    log.info("Deactivated entity with ID: {}", deactivatedEntity.getEntityId());
                    return deactivatedEntity;
                })
                .orElseThrow(() -> {
                    log.error("Entity not found with ID: {}", id);
                    return new EntityNotFoundException("Entity not found with ID: " + id);
                });
    }
}