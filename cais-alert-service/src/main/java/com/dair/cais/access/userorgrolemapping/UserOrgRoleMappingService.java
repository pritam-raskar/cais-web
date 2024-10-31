package com.dair.cais.access.userorgrolemapping;

import com.dair.cais.access.Role.RoleEntity;
import com.dair.cais.access.Role.RoleRepository;
import com.dair.cais.access.UserBasedPermission.UserPermissionService;
import com.dair.cais.access.organizationUnit.OrgUnitEntity;
import com.dair.cais.access.user.UserEntity;
import com.dair.cais.access.user.UserRepository;
import com.dair.cais.access.userOrgRole.UserOrgRoleMappingEntity;
import com.dair.cais.access.userOrgRole.UserOrgRoleMappingRepository;
import com.dair.cais.organization.OrganizationUnit;
import com.dair.cais.organization.OrganizationUnitService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrgRoleMappingService {
    private final UserOrgRoleMappingRepository mappingRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationUnitService organizationUnitService;
    private final UserPermissionService userPermissionService;

    @Transactional
    public List<UserOrgRoleMappingDto> updateUserMappings(UserOrgRoleMappingRequest request) {
        log.debug("Updating user mappings for user ID: {}", request.getUserId());

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + request.getUserId()));

        // Validate user is active
        if (Boolean.FALSE.equals(user.getUserIsActive())) {
            throw new IllegalStateException("Cannot update mappings for inactive user: " + request.getUserId());
        }

        // Get existing mappings for the user
        List<UserOrgRoleMappingEntity> existingMappings = mappingRepository.findByUserUserId(user.getUserId());
        Map<String, UserOrgRoleMappingEntity> existingMappingsMap = existingMappings.stream()
                .collect(Collectors.toMap(
                        mapping -> generateKey(mapping.getOrgUnit().getOrgId(), mapping.getRole().getRoleId()),
                        mapping -> mapping
                ));

        List<UserOrgRoleMappingEntity> mappingsToSave = new ArrayList<>();

        // Process each new mapping
        for (UserOrgRoleMappingRequest.OrgRoleMapping mapping : request.getMappings()) {
            String key = generateKey(mapping.getOrgId(), mapping.getRoleId());
            UserOrgRoleMappingEntity existingMapping = existingMappingsMap.get(key);

            if (existingMapping != null) {
                // Mapping already exists, verify org unit is still active
                OrganizationUnit orgUnit = organizationUnitService.getOrganizationUnitById(mapping.getOrgId());
                if (Boolean.TRUE.equals(orgUnit.getIsActive())) {
                    mappingsToSave.add(existingMapping);
                    existingMappingsMap.remove(key);
                } else {
                    log.warn("Skipping mapping for inactive organization unit: {}", mapping.getOrgId());
                }
            } else {
                // Create new mapping
                UserOrgRoleMappingEntity newMapping = createNewMapping(user, mapping);
                mappingsToSave.add(newMapping);
            }
        }

        // Delete mappings that are no longer needed
        for (UserOrgRoleMappingEntity obsoleteMapping : existingMappingsMap.values()) {
            log.debug("Deleting obsolete mapping for user {} with org {} and role {}",
                    user.getUserId(),
                    obsoleteMapping.getOrgUnit().getOrgId(),
                    obsoleteMapping.getRole().getRoleId());
            mappingRepository.delete(obsoleteMapping);
        }

        // Save all mappings
        List<UserOrgRoleMappingEntity> savedMappings = mappingRepository.saveAll(mappingsToSave);
        log.info("Saved {} mappings for user {}", savedMappings.size(), user.getUserId());

        // Refresh user permissions cache
        userPermissionService.refreshUserPermissions(user.getUserId());

        // Convert to DTOs and return
        return savedMappings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private String generateKey(Integer orgId, Integer roleId) {
        return orgId + ":" + roleId;
    }

    private UserOrgRoleMappingEntity createNewMapping(UserEntity user, UserOrgRoleMappingRequest.OrgRoleMapping mapping) {
        // Validate and get role
        RoleEntity role = roleRepository.findById(mapping.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + mapping.getRoleId()));

        // Get and validate organization unit using the service
        OrganizationUnit orgUnit = organizationUnitService.getOrganizationUnitById(mapping.getOrgId());
        if (!Boolean.TRUE.equals(orgUnit.getIsActive())) {
            throw new IllegalStateException("Cannot create mapping with inactive organization unit: " + mapping.getOrgId());
        }

        // Convert OrganizationUnit to Entity
        OrgUnitEntity orgUnitEntity = new OrgUnitEntity();
        orgUnitEntity.setOrgId(orgUnit.getOrgId());
        orgUnitEntity.setType(orgUnit.getType());


        UserOrgRoleMappingEntity newMapping = new UserOrgRoleMappingEntity();
        newMapping.setUser(user);
        newMapping.setRole(role);
        newMapping.setOrgUnit(orgUnitEntity);

        log.debug("Created new mapping for user {} with org {} and role {}",
                user.getUserId(), mapping.getOrgId(), mapping.getRoleId());

        return newMapping;
    }

    @Transactional(readOnly = true)
    public List<UserOrgRoleMappingDto> getUserMappings(String userId) {
        log.debug("Fetching mappings for user ID: {}", userId);
        return mappingRepository.findByUserUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private UserOrgRoleMappingDto convertToDto(UserOrgRoleMappingEntity entity) {
        UserOrgRoleMappingDto dto = new UserOrgRoleMappingDto();
        dto.setUserId(entity.getUser().getUserId());
        dto.setMappingId(entity.getMappingId());
        dto.setOrgId(entity.getOrgUnit().getOrgId());
        dto.setRoleId(entity.getRole().getRoleId());
        return dto;
    }
}