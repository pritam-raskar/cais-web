package com.dair.cais.access.userorgrolemapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-org-role-mappings")
@RequiredArgsConstructor
@Slf4j
public class UserOrgRoleMappingController {
    private final UserOrgRoleMappingService mappingService;

    @PutMapping("/update")
    public ResponseEntity<List<UserOrgRoleMappingDto>> updateUserMappings(
            @RequestBody UserOrgRoleMappingRequest request) {
        log.debug("Received request to update mappings for user: {}", request.getUserId());
        List<UserOrgRoleMappingDto> updatedMappings = mappingService.updateUserMappings(request);
        return ResponseEntity.ok(updatedMappings);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserOrgRoleMappingDto>> getUserMappings(
            @PathVariable String userId) {
        log.debug("Received request to get mappings for user: {}", userId);
        List<UserOrgRoleMappingDto> mappings = mappingService.getUserMappings(userId);
        return ResponseEntity.ok(mappings);
    }
}