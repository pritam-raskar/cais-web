package com.dair.cais.access.modules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository repository;

    @Transactional(readOnly = true)
    public List<ModuleEntity> getAllModules() {
        log.info("Fetching all modules");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ModuleEntity> getModuleById(Integer moduleId) {
        log.info("Fetching module with ID: {}", moduleId);
        return repository.findById(moduleId);
    }

    @Transactional
    public ModuleEntity createModule(ModuleEntity module) {
        log.info("Creating new module");
        ModuleEntity savedEntity = repository.save(module);
        log.debug("Created module with ID: {}", savedEntity.getModuleId());
        return savedEntity;
    }

    @Transactional
    public Optional<ModuleEntity> updateModule(Integer moduleId, ModuleEntity module) {
        log.info("Updating module with ID: {}", moduleId);
        return repository.findById(moduleId)
                .map(existingEntity -> {
                    module.setModuleId(existingEntity.getModuleId());
                    ModuleEntity savedEntity = repository.save(module);
                    log.debug("Updated module with ID: {}", savedEntity.getModuleId());
                    return savedEntity;
                });
    }

    @Transactional
    public void deleteModule(Integer moduleId) {
        log.info("Deleting module with ID: {}", moduleId);
        repository.deleteById(moduleId);
        log.debug("Deleted module with ID: {}", moduleId);
    }
}