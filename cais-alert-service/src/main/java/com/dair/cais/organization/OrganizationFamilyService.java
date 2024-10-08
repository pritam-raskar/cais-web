package com.dair.cais.organization;

import com.dair.exception.CaisNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationFamilyService {

    private final OrganizationUnitRepository organizationUnitRepository;
    private final OrganizationFamilyRepository organizationFamilyRepository;

    @Transactional
    public void generateAndSaveOrganizationFamilies() {
        log.info("Starting generation of organization families");
        long startTime = System.currentTimeMillis();

        // Load all org_key and parent_org_key pairs into memory
        List<OrgKeyPair> orgKeyPairs = organizationUnitRepository.findAllOrgKeyPairs();

        // Create maps for quick lookups
        Map<String, String> orgKeyToParentMap = new HashMap<>();
        for (OrgKeyPair pair : orgKeyPairs) {
            if (pair.getOrgKey() != null) {
                orgKeyToParentMap.put(pair.getOrgKey(), pair.getParentOrgKey());
            } else {
                log.warn("Encountered null org_key in OrgKeyPair: {}", pair);
            }
        }

        // Generate org families
        Map<String, String> orgFamilyMap = generateOrgFamilies(orgKeyToParentMap);

        // Fetch existing OrganizationFamilyEntities
        List<OrganizationFamilyEntity> existingEntities = organizationFamilyRepository.findAllByOrgKeyIn(orgKeyToParentMap.keySet());
        Map<String, OrganizationFamilyEntity> existingEntitiesMap = existingEntities.stream()
                .collect(Collectors.toMap(OrganizationFamilyEntity::getOrgKey, e -> e));

        // Update existing entities and create new ones
        List<OrganizationFamilyEntity> entitiesToSave = new ArrayList<>();
        for (Map.Entry<String, String> entry : orgFamilyMap.entrySet()) {
            String orgKey = entry.getKey();
            String orgFamily = entry.getValue();
            String parentOrgKey = orgKeyToParentMap.get(orgKey);

            if (existingEntitiesMap.containsKey(orgKey)) {
                OrganizationFamilyEntity existingEntity = existingEntitiesMap.get(orgKey);
                existingEntity.setParentOrgKey(parentOrgKey);
                existingEntity.setOrgFamily(orgFamily);
                entitiesToSave.add(existingEntity);
            } else {
                entitiesToSave.add(createOrganizationFamilyEntity(orgKey, orgFamily, parentOrgKey));
            }
        }

        // Batch save all entities (this will update existing ones and create new ones)
        organizationFamilyRepository.saveAll(entitiesToSave);

        long endTime = System.currentTimeMillis();
        log.info("Completed generation of organization families. Time taken: {} ms", endTime - startTime);
        log.info("Updated/Created {} organization families out of {} organizations", entitiesToSave.size(), orgKeyPairs.size());
    }

    private Map<String, String> generateOrgFamilies(Map<String, String> orgKeyToParentMap) {
        Map<String, String> orgFamilyMap = new HashMap<>();
        for (String orgKey : orgKeyToParentMap.keySet()) {
            generateOrgFamily(orgKey, orgKeyToParentMap, orgFamilyMap);
        }
        return orgFamilyMap;
    }

    private String generateOrgFamily(String orgKey, Map<String, String> orgKeyToParentMap, Map<String, String> orgFamilyMap) {
        if (orgFamilyMap.containsKey(orgKey)) {
            return orgFamilyMap.get(orgKey);
        }

        String parentOrgKey = orgKeyToParentMap.get(orgKey);
        if (parentOrgKey == null || parentOrgKey.isEmpty()) {
            orgFamilyMap.put(orgKey, orgKey);
            return orgKey;
        }

        String parentFamily = generateOrgFamily(parentOrgKey, orgKeyToParentMap, orgFamilyMap);
        String orgFamily = parentFamily + ":" + orgKey;
        orgFamilyMap.put(orgKey, orgFamily);

        return orgFamily;
    }

    private OrganizationFamilyEntity createOrganizationFamilyEntity(String orgKey, String orgFamily, String parentOrgKey) {
        OrganizationFamilyEntity familyEntity = new OrganizationFamilyEntity();
        familyEntity.setOrgKey(orgKey);
        familyEntity.setOrgFamily(orgFamily);
        familyEntity.setParentOrgKey(parentOrgKey);
        return familyEntity;
    }

    @Transactional(readOnly = true)
    public OrganizationFamily getOrganizationFamilyByOrgKey(String orgKey) {
        log.info("Fetching organization family for org_key: {}", orgKey);
        return organizationFamilyRepository.findByOrgKey(orgKey)
                .map(this::mapToDTO)
                .orElseThrow(() -> {
                    log.error("Organization family not found for org_key: {}", orgKey);
                    return new CaisNotFoundException("Organization family not found for org_key: " + orgKey);
                });
    }

    private OrganizationFamily mapToDTO(OrganizationFamilyEntity entity) {
        return new OrganizationFamily(
                entity.getOrgKey(),
                entity.getParentOrgKey(),
                entity.getOrgFamily()
        );
    }

    @Transactional(readOnly = true)
    public Page<OrganizationFamily> getAllOrganizationFamilies(Pageable pageable) {
        log.info("Fetching all organization families");
        return organizationFamilyRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

}