package com.dair.cais.access.Role;

import com.dair.cais.access.permission.AsyncPermissionRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleUpdateEventListener {

    private final AsyncPermissionRefreshService asyncPermissionRefreshService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoleUpdate(RoleUpdateEvent event) {
        log.info("Received role update event for role ID: {}", event.getRoleId());
        asyncPermissionRefreshService.refreshPermissionsForRole(event.getRoleId());
    }
}