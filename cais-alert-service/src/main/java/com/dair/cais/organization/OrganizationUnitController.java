package com.dair.cais.organization;

import com.dair.cais.organization.OrganizationUnit;
import com.dair.cais.organization.OrganizationUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.dair.cais.organization.util.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/organization-unit")
@RequiredArgsConstructor
public class OrganizationUnitController {

    private final OrganizationUnitService organizationUnitService;
    private final CsvHelper csvHelper;

    @GetMapping
    public ResponseEntity<List<OrganizationUnit>> getAllOrganizationUnits() {
        log.info("Received request to get all organization units");
        List<OrganizationUnit> organizationUnits = organizationUnitService.getAllOrganizationUnits();
        return ResponseEntity.ok(organizationUnits);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<OrganizationUnit> getOrganizationUnitById(@PathVariable Integer id) {
        log.info("Received request to get organization unit with id: {}", id);
        OrganizationUnit organizationUnit = organizationUnitService.getOrganizationUnitById(id);
        return ResponseEntity.ok(organizationUnit);
    }

    @GetMapping("/org_key/{org_key}")
    public ResponseEntity<OrganizationUnit> getOrganizationUnitByKey(@PathVariable String org_key) {
        log.info("Received request to get organization unit with id: {}", org_key);
        OrganizationUnit organizationUnit = organizationUnitService.getOrganizationUnitByOrgKey(org_key);
        return ResponseEntity.ok(organizationUnit);
    }

    @PostMapping
    public ResponseEntity<OrganizationUnit> createOrganizationUnit(@Valid @RequestBody OrganizationUnit organizationUnit) {
        log.info("Received request to create new organization unit");
        OrganizationUnit createdOrganizationUnit = organizationUnitService.createOrganizationUnit(organizationUnit);
        return new ResponseEntity<>(createdOrganizationUnit, HttpStatus.CREATED);
    }

    @PutMapping("/{org_key}")
    public ResponseEntity<OrganizationUnit> updateOrganizationUnit(@PathVariable String org_key, @Valid @RequestBody OrganizationUnit organizationUnit) {
        log.info("Received request to update organization unit with org_key: {}", org_key);
        OrganizationUnit updatedOrganizationUnit = organizationUnitService.updateOrganizationUnit(org_key, organizationUnit);
        return ResponseEntity.ok(updatedOrganizationUnit);
    }

    @PatchMapping("/id/{id}/activate")
    public ResponseEntity<Void> activateOrganizationUnit(@PathVariable Integer id) {
        log.info("Received request to activate organization unit with id: {}", id);
        organizationUnitService.activateOrganizationUnit(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/id/{id}/deactivate")
    public ResponseEntity<Void> deactivateOrganizationUnit(@PathVariable Integer id) {
        log.info("Received request to deactivate organization unit with id: {}", id);
        organizationUnitService.deactivateOrganizationUnit(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/org_key/{org_key}/activate")
    public ResponseEntity<Void> activateOrganizationUnitByOrgKey(@PathVariable String org_key) {
        log.info("Received request to activate organization unit with org_key: {}", org_key);
        organizationUnitService.activateOrganizationUnitByOrgKey(org_key);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/org_key/{org_key}/deactivate")
    public ResponseEntity<Void> deactivateOrganizationUnitByOrgKey(@PathVariable String org_key) {
        log.info("Received request to deactivate organization unit with org_key: {}", org_key);
        organizationUnitService.deactivateOrganizationUnitByOrgKey(org_key);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Received request to upload CSV file");
        if (csvHelper.hasCSVFormat(file)) {
            try {
                organizationUnitService.processCsvFile(file);
                return ResponseEntity.ok("Uploaded the file successfully: " + file.getOriginalFilename());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Could not upload the file: " + file.getOriginalFilename() + "!");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a csv file!");
    }
}
