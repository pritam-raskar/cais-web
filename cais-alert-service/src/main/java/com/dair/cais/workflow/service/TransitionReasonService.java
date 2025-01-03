package com.dair.cais.workflow.service;

import com.dair.cais.workflow.entity.TransitionReasonEntity;
import com.dair.cais.workflow.model.TransitionReasonDTO;
import com.dair.cais.workflow.repository.TransitionReasonRepository;
import com.dair.cais.workflow.exception.ResourceAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link TransitionReasonEntity}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransitionReasonService {
    private final TransitionReasonRepository transitionReasonRepository;

    /**
     * Get all transition reasons.
     *
     * @return list of all transition reasons
     */
    @Transactional(readOnly = true)
    public List<TransitionReasonDTO> getAllReasons() {
        log.debug("Request to get all Transition Reasons");
        return transitionReasonRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get transition reason by ID.
     *
     * @param id the id of the transition reason
     * @return the transition reason
     * @throws EntityNotFoundException if the transition reason is not found
     */
    @Transactional(readOnly = true)
    public TransitionReasonDTO getReasonById(Long id) {
        log.debug("Request to get Transition Reason : {}", id);
        return transitionReasonRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Transition reason not found with id: {}", id);
                    return new EntityNotFoundException("Transition reason not found with id: " + id);
                });
    }

    /**
     * Create a new transition reason.
     *
     * @param dto the transition reason to create
     * @return the created transition reason
     * @throws ResourceAlreadyExistsException if a transition reason with the same details already exists
     */
    @Transactional
    public TransitionReasonDTO createReason(TransitionReasonDTO dto) {
        log.debug("Request to create Transition Reason : {}", dto);

        if (transitionReasonRepository.existsByReasonDetails(dto.getReasonDetails())) {
            log.warn("Transition reason already exists with details: {}", dto.getReasonDetails());
            throw new ResourceAlreadyExistsException("Transition reason with these details already exists");
        }

        TransitionReasonEntity entity = toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());

        TransitionReasonEntity savedEntity = transitionReasonRepository.save(entity);
        log.info("Created new Transition Reason with id: {}", savedEntity.getId());

        return toDTO(savedEntity);
    }

    /**
     * Update an existing transition reason.
     *
     * @param id the id of the transition reason to update
     * @param dto the updated transition reason data
     * @return the updated transition reason
     * @throws EntityNotFoundException if the transition reason is not found
     */
    @Transactional
    public TransitionReasonDTO updateReason(Long id, TransitionReasonDTO dto) {
        log.debug("Request to update Transition Reason : {}", id);

        TransitionReasonEntity existingEntity = transitionReasonRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Transition reason not found with id: {}", id);
                    return new EntityNotFoundException("Transition reason not found with id: " + id);
                });

        // Check if another reason exists with the same details (excluding the current one)
        if (!existingEntity.getReasonDetails().equals(dto.getReasonDetails()) &&
                transitionReasonRepository.existsByReasonDetails(dto.getReasonDetails())) {
            log.warn("Another transition reason already exists with details: {}", dto.getReasonDetails());
            throw new ResourceAlreadyExistsException("Another transition reason already exists with these details");
        }

        existingEntity.setReasonDetails(dto.getReasonDetails());
        existingEntity.setUpdatedDate(LocalDateTime.now());
        existingEntity.setUpdatedBy(dto.getUpdatedBy());

        TransitionReasonEntity updatedEntity = transitionReasonRepository.save(existingEntity);
        log.info("Updated Transition Reason with id: {}", id);

        return toDTO(updatedEntity);
    }

    /**
     * Delete a transition reason.
     *
     * @param id the id of the transition reason to delete
     * @throws EntityNotFoundException if the transition reason is not found
     */
    @Transactional
    public void deleteReason(Long id) {
        log.debug("Request to delete Transition Reason : {}", id);

        if (!transitionReasonRepository.existsById(id)) {
            log.error("Unable to delete. Transition reason not found with id: {}", id);
            throw new EntityNotFoundException("Transition reason not found with id: " + id);
        }

        transitionReasonRepository.deleteById(id);
        log.info("Deleted Transition Reason with id: {}", id);
    }

    /**
     * Convert entity to DTO.
     *
     * @param entity the entity to convert
     * @return the converted DTO
     */
    private TransitionReasonDTO toDTO(TransitionReasonEntity entity) {
        TransitionReasonDTO dto = new TransitionReasonDTO();
        dto.setId(entity.getId());
        dto.setReasonDetails(entity.getReasonDetails());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    /**
     * Convert DTO to entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity
     */
    private TransitionReasonEntity toEntity(TransitionReasonDTO dto) {
        TransitionReasonEntity entity = new TransitionReasonEntity();
        entity.setReasonDetails(dto.getReasonDetails());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        return entity;
    }
}