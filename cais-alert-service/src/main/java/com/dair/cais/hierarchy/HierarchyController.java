package com.dair.cais.hierarchy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/hierarchy")
@RequiredArgsConstructor
public class HierarchyController {

    private final HierarchyService hierarchyService;

    @GetMapping
    public ResponseEntity<List<Hierarchy>> getAllHierarchies() {
        log.info("Received request to get all hierarchies");
        List<Hierarchy> hierarchies = hierarchyService.getAllHierarchies();
        return ResponseEntity.ok(hierarchies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hierarchy> getHierarchyById(@PathVariable Integer id) {
        log.info("Received request to get hierarchy with id: {}", id);
        Hierarchy hierarchy = hierarchyService.getHierarchyById(id);
        return ResponseEntity.ok(hierarchy);
    }

    @PostMapping
    public ResponseEntity<Hierarchy> createHierarchy(@Valid @RequestBody Hierarchy hierarchy) {
        log.info("Received request to create new hierarchy");
        Hierarchy createdHierarchy = hierarchyService.createHierarchy(hierarchy);
        return new ResponseEntity<>(createdHierarchy, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hierarchy> updateHierarchy(@PathVariable Integer id, @Valid @RequestBody Hierarchy hierarchy) {
        log.info("Received request to update hierarchy with id: {}", id);
        Hierarchy updatedHierarchy = hierarchyService.updateHierarchy(id, hierarchy);
        return ResponseEntity.ok(updatedHierarchy);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateHierarchy(@PathVariable Integer id) {
        log.info("Received request to activate hierarchy with id: {}", id);
        hierarchyService.activateHierarchy(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateHierarchy(@PathVariable Integer id) {
        log.info("Received request to deactivate hierarchy with id: {}", id);
        hierarchyService.deactivateHierarchy(id);
        return ResponseEntity.noContent().build();
    }
}
