package com.dair.cais.access.permission;

import com.dair.cais.access.UserBasedPermission.UserPermissionService;
import com.dair.cais.access.userOrgRole.UserOrgRoleMappingEntity;
import com.dair.cais.access.userOrgRole.UserOrgRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncPermissionRefreshService {

    private final UserOrgRoleMappingRepository userOrgRoleMappingRepository;
    private final UserPermissionService userPermissionService;

    @Async("permissionRefreshExecutor")
    public void refreshPermissionsForRole(Integer roleId) {
        log.info("Starting async permission refresh for role ID: {}", roleId);
        try {
            List<UserOrgRoleMappingEntity> mappings = userOrgRoleMappingRepository.findByRoleRoleId(roleId);
            Set<String> userIds = mappings.stream()
                    .map(mapping -> mapping.getUser().getUserId())
                    .collect(Collectors.toSet());

            log.info("Found {} users to refresh for role ID: {}", userIds.size(), roleId);

            for (String userId : userIds) {
                try {
                    userPermissionService.refreshUserPermissions(userId);
                    log.debug("Successfully refreshed permissions for user: {}", userId);
                } catch (Exception e) {
                    log.error("Failed to refresh permissions for user: {}", userId, e);
                    // Continue with other users even if one fails
                }
            }
            log.info("Completed async permission refresh for role ID: {}", roleId);
        } catch (Exception e) {
            log.error("Error during async permission refresh for role ID: {}", roleId, e);
        }
    }
}