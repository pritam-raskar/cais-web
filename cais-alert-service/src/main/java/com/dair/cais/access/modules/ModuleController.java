package com.dair.cais.access.modules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public ResponseEntity<List<ModuleEntity>> getAllModules() {
        log.info("Received request to get all modules");
        List<ModuleEntity> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleEntity> getModuleById(@PathVariable Integer id) {
        log.info("Received request to get module with id: {}", id);
        return moduleService.getModuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @PostMapping
//    public ResponseEntity<ModuleEntity> createModule(@RequestBody ModuleEntity module) {
//        log.info("Received request to create a new module");
//        ModuleEntity createdModule = moduleService.createModule(module);
//        return new ResponseEntity<>(createdModule, HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ModuleEntity> updateModule(@PathVariable Integer id, @RequestBody ModuleEntity module) {
//        log.info("Received request to update module with id: {}", id);
//        return moduleService.updateModule(id, module)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteModule(@PathVariable Integer id) {
//        log.info("Received request to delete module with id: {}", id);
//        moduleService.deleteModule(id);
//        return ResponseEntity.noContent().build();
//    }
}