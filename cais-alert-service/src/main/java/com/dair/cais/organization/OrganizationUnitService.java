package com.dair.cais.organization;

import com.dair.cais.organization.OrganizationUnit;
import com.dair.cais.organization.OrganizationUnitEntity;
import com.dair.cais.organization.OrganizationUnitRepository;
import jakarta.persistence.EntityNotFoundException;
import com.dair.cais.organization.util.CsvHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationUnitService {

    private final OrganizationUnitRepository repository;
    private final CsvHelper csvHelper;

    @Transactional(readOnly = true)
    public List<OrganizationUnit> getAllOrganizationUnits() {
        log.debug("Fetching all organization units");
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrganizationUnit getOrganizationUnitById(Integer id) {
        log.debug("Fetching organization unit with id: {}", id);
        return repository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Organization unit not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public OrganizationUnit getOrganizationUnitByOrgKey(String org_key) {
        log.debug("Fetching organization unit with id: {}", org_key);
        return repository.findByOrgKey(org_key)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Organization unit not found with key: " + org_key));
    }

    @Transactional
    public OrganizationUnit createOrganizationUnit(OrganizationUnit organizationUnit) {
        log.debug("Creating new organization unit: {}", organizationUnit);
        OrganizationUnitEntity entity = mapToEntity(organizationUnit);
        entity = repository.save(entity);
        return mapToDto(entity);
    }

    @Transactional
    public OrganizationUnit updateOrganizationUnit(String org_key, OrganizationUnit organizationUnit) {
        log.debug("Updating organization unit with org_key: {}", org_key);
        return repository.findByOrgKey(org_key)
                .map(entity -> {
                    updateEntityFromDto(organizationUnit, entity);
                    return mapToDto(repository.save(entity));
                })
                .orElseThrow(() -> new EntityNotFoundException("Organization unit not found with org_key: " + org_key));
    }

    @Transactional
    public void activateOrganizationUnit(Integer id) {
        log.debug("Activating organization unit with id: {}", id);
        OrganizationUnitEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization unit not found with id: " + id));
        entity.setIsActive(true);
        repository.save(entity);
    }

    @Transactional
    public void deactivateOrganizationUnit(Integer id) {
        log.debug("Deactivating organization unit with id: {}", id);
        OrganizationUnitEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization unit not found with id: " + id));
        entity.setIsActive(false);
        repository.save(entity);
    }

    @Transactional
    public void activateOrganizationUnitByOrgKey(String org_key) {
        log.debug("Activating organization unit with id: {}", org_key);
        OrganizationUnitEntity entity = repository.findByOrgKey(org_key)
                .orElseThrow(() -> new EntityNotFoundException("Organization unit not found with org_key: " + org_key));
        entity.setIsActive(true);
        repository.save(entity);
    }

    @Transactional
    public void deactivateOrganizationUnitByOrgKey(String org_key) {
        log.debug("Deactivating organization unit with org_key: {}", org_key);
        OrganizationUnitEntity entity = repository.findByOrgKey(org_key)
                .orElseThrow(() -> new EntityNotFoundException("Organization unit not found with org_key: " + org_key));
        entity.setIsActive(false);
        repository.save(entity);
    }

    private OrganizationUnit mapToDto(OrganizationUnitEntity entity) {
        OrganizationUnit dto = new OrganizationUnit();
        dto.setOrgId(entity.getOrgId());
        dto.setType(entity.getType());
        dto.setOrgKey(entity.getOrgKey());
        dto.setOrgName(entity.getOrgName());
        dto.setOrgDescription(entity.getOrgDescription());
        dto.setParentOrgKey(entity.getParentOrgKey());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreateDate());
        dto.setUpdatedAt(entity.getUpdateDate());
        return dto;
    }

    private OrganizationUnitEntity mapToEntity(OrganizationUnit dto) {
        OrganizationUnitEntity entity = new OrganizationUnitEntity();
        entity.setType(dto.getType());
        entity.setOrgKey(dto.getOrgKey());
        entity.setOrgName(dto.getOrgName());
        entity.setOrgDescription(dto.getOrgDescription());
        entity.setParentOrgKey(dto.getParentOrgKey());
        entity.setIsActive(dto.getIsActive());
        return entity;
    }

    private void updateEntityFromDto(OrganizationUnit dto, OrganizationUnitEntity entity) {
        entity.setType(dto.getType());
        entity.setOrgName(dto.getOrgName());
        entity.setOrgDescription(dto.getOrgDescription());
        entity.setParentOrgKey(dto.getParentOrgKey());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }

    @Transactional
    public void processCsvFile(MultipartFile file) throws IOException {
        log.debug("Processing CSV file");
        List<OrganizationUnit> organizationUnits = csvHelper.csvToOrganizationUnits(file.getInputStream());
        for (OrganizationUnit ou : organizationUnits) {
            log.debug("Processing organization unit: {}", ou);
            repository.findByOrgKey(ou.getOrgKey())
                    .ifPresentOrElse(
                            entity -> {
                                updateEntityFromDto(ou, entity);
                                OrganizationUnitEntity savedEntity = repository.save(entity);
                                log.debug("Updated existing organization unit: {}", savedEntity);
                            },
                            () -> {
                                OrganizationUnitEntity newEntity = mapToEntity(ou);
                                OrganizationUnitEntity savedEntity = repository.save(newEntity);
                                log.debug("Created new organization unit: {}", savedEntity);
                            }
                    );
        }
    }
}
