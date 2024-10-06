package com.dair.cais.hierarchy;

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
public class HierarchyService {

    private final HierarchyRepository hierarchyRepository;
    private final HierarchyMapper hierarchyMapper;

    @Transactional(readOnly = true)
    public List<Hierarchy> getAllHierarchies() {
        log.debug("Fetching all hierarchies");
        return hierarchyRepository.findAll().stream()
                .map(hierarchyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Hierarchy getHierarchyById(Integer id) {
        log.debug("Fetching hierarchy with id: {}", id);
        return hierarchyRepository.findById(id)
                .map(hierarchyMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Hierarchy not found with id: " + id));
    }

    @Transactional
    public Hierarchy createHierarchy(Hierarchy hierarchy) {
        log.debug("Creating new hierarchy: {}", hierarchy);
        HierarchyEntity entity = hierarchyMapper.toEntity(hierarchy);
        entity = hierarchyRepository.save(entity);
        log.info("Created new hierarchy with id: {}", entity.getHierarchyId());
        return hierarchyMapper.toDto(entity);
    }

    @Transactional
    public Hierarchy updateHierarchy(Integer id, Hierarchy hierarchy) {
        log.debug("Updating hierarchy with id: {}", id);
        return hierarchyRepository.findById(id)
                .map(entity -> {
                    hierarchyMapper.updateEntityFromDto(hierarchy, entity);
                    entity = hierarchyRepository.save(entity);
                    log.info("Updated hierarchy with id: {}", id);
                    return hierarchyMapper.toDto(entity);
                })
                .orElseThrow(() -> new EntityNotFoundException("Hierarchy not found with id: " + id));
    }

    @Transactional
    public void activateHierarchy(Integer id) {
        log.debug("Activating hierarchy with id: {}", id);
        HierarchyEntity entity = hierarchyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hierarchy not found with id: " + id));
        entity.setIsActive(true);
        hierarchyRepository.save(entity);
        log.info("Activated hierarchy with id: {}", id);
    }

    @Transactional
    public void deactivateHierarchy(Integer id) {
        log.debug("Deactivating hierarchy with id: {}", id);
        HierarchyEntity entity = hierarchyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hierarchy not found with id: " + id));
        entity.setIsActive(false);
        hierarchyRepository.save(entity);
        log.info("Deactivated hierarchy with id: {}", id);
    }
}
