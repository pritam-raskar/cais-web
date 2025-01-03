package com.dair.cais.workflow.service;

import com.dair.cais.workflow.entity.ChecklistEntity;
import com.dair.cais.workflow.model.ChecklistDTO;
import com.dair.cais.workflow.repository.ChecklistRepository;
import com.dair.cais.workflow.exception.ResourceAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChecklistService {
    private final ChecklistRepository checklistRepository;

    @Transactional(readOnly = true)
    public List<ChecklistDTO> getAllChecklists() {
        log.debug("Fetching all checklists");
        return checklistRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChecklistDTO getChecklistById(Long id) {
        log.debug("Fetching checklist with id: {}", id);
        return checklistRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Checklist not found with id: " + id));
    }

    @Transactional
    public ChecklistDTO createChecklist(ChecklistDTO dto) {
        log.debug("Creating new checklist: {}", dto);

        if (checklistRepository.existsByListName(dto.getListName())) {
            log.warn("Checklist with name '{}' already exists", dto.getListName());
            throw new ResourceAlreadyExistsException("Checklist with this name already exists");
        }

        ChecklistEntity entity = toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());

        ChecklistEntity savedEntity = checklistRepository.save(entity);
        log.info("Created new checklist with id: {}", savedEntity.getId());

        return toDTO(savedEntity);
    }

    @Transactional
    public ChecklistDTO updateChecklist(Long id, ChecklistDTO dto) {
        log.debug("Updating checklist with id: {}", id);

        ChecklistEntity existingEntity = checklistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Checklist not found with id: " + id));

        existingEntity.setListName(dto.getListName());
        existingEntity.setUpdatedDate(LocalDateTime.now());
        existingEntity.setUpdatedBy(dto.getUpdatedBy());

        ChecklistEntity updatedEntity = checklistRepository.save(existingEntity);
        log.info("Updated checklist with id: {}", id);

        return toDTO(updatedEntity);
    }

    @Transactional
    public void deleteChecklist(Long id) {
        log.debug("Deleting checklist with id: {}", id);

        if (!checklistRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent checklist with id: {}", id);
            throw new EntityNotFoundException("Checklist not found with id: " + id);
        }

        checklistRepository.deleteById(id);
        log.info("Deleted checklist with id: {}", id);
    }

    private ChecklistDTO toDTO(ChecklistEntity entity) {
        ChecklistDTO dto = new ChecklistDTO();
        dto.setId(entity.getId());
        dto.setListName(entity.getListName());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    private ChecklistEntity toEntity(ChecklistDTO dto) {
        ChecklistEntity entity = new ChecklistEntity();
        entity.setListName(dto.getListName());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        return entity;
    }
}