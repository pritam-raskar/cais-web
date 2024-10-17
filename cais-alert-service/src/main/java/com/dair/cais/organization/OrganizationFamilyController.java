package com.dair.cais.organization;

import com.dair.exception.CaisNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/organization-families")
@Tag(name = "Organization Family Management", description = "APIs for managing organization families")
@RequiredArgsConstructor
public class OrganizationFamilyController {

    private final OrganizationFamilyService organizationFamilyService;

    @PostMapping("/generate")
    @Operation(summary = "Generate and save organization families", 
               description = "Generates the organization family hierarchy for all organizations and saves it to the database")
    @ApiResponse(responseCode = "200", description = "Organization families generated and saved successfully")
    @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    public ResponseEntity<String> generateOrganizationFamilies() {
        log.info("Received request to generate organization families");
        try {
            organizationFamilyService.generateAndSaveOrganizationFamilies();
            log.info("Organization families generated and saved successfully");
            return ResponseEntity.ok("Organization families generated and saved successfully");
        } catch (Exception e) {
            log.error("Error occurred while generating organization families", e);
            return ResponseEntity.internalServerError().body("An error occurred while generating organization families");
        }
    }

    @GetMapping("/{orgKey}")
    @Operation(summary = "Get organization family by org_key",
            description = "Retrieves the org_key, parent_org_key, and org_family for a given org_key")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved organization family",
            content = @Content(schema = @Schema(implementation = OrganizationFamily.class)))
    @ApiResponse(responseCode = "404", description = "Organization family not found")
    public ResponseEntity<OrganizationFamily> getOrganizationFamily(
            @Parameter(description = "The org_key to lookup", required = true)
            @PathVariable String orgKey) {
        log.info("Received request to get organization family for org_key: {}", orgKey);
        try {
            OrganizationFamily familyDTO = organizationFamilyService.getOrganizationFamilyByOrgKey(orgKey);
            return ResponseEntity.ok(familyDTO);
        } catch (CaisNotFoundException e) {
            log.error("Organization family not found for org_key: {}", orgKey, e);
            return ResponseEntity.notFound().build();
        }
    }

    // generate code to get all org_key with family
    @GetMapping
    @Operation(summary = "Get all organization families",
            description = "Retrieves all org_key, parent_org_key, and org_family entries")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved organization families",
            content = @Content(schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<OrganizationFamily>> getAllOrganizationFamilies(
            @Parameter(description = "Pageable parameters")
            @PageableDefault(size = 200000) Pageable pageable) {
        log.info("Received request to get all organization families");
        Page<OrganizationFamily> families = organizationFamilyService.getAllOrganizationFamilies(pageable);
        return ResponseEntity.ok(families);
    }
}
