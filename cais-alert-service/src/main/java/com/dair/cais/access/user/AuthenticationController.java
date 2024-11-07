package com.dair.cais.access.user;

import com.dair.cais.access.UserBasedPermission.UserPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserPermissionService userPermissionService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authenticationService.authenticate(loginRequest.getUserLoginName(), loginRequest.getPassword())
                .map(loginResponse -> {
                    try {
                        loginResponse.setPermissions(
                                userPermissionService.getUserPermissionFromMongo(
                                        String.valueOf(loginResponse.getUserId())
                                )
                        );
                        return ResponseEntity.ok(loginResponse);
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError()
                                .body("Error processing permissions: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.badRequest().body("Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity user) {
        UserEntity registeredUser = authenticationService.registerUser(user);
        return ResponseEntity.ok(registeredUser);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Fetching all users");
        return ResponseEntity.ok(authenticationService.getAllUsers());
    }

    @PutMapping("/users/status")
    @Operation(summary = "Update user status for one or multiple users")
    public ResponseEntity<List<UserDTO>> updateUsersStatus(
            @RequestBody UserStatusUpdateRequest request) {
        log.info("Updating status for {} users, activate: {}",
                request.getUserIds().size(), request.isActivate());
        List<UserDTO> updatedUsers = authenticationService.updateUsersStatus(
                request.getUserIds(), request.isActivate());
        return ResponseEntity.ok(updatedUsers);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate JWT token and get user information")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials");  // Match login error message
            }

            String token = authHeader.substring(7);
            TokenValidationResponse response = authenticationService.validateToken(token);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials");  // Match login error message
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");  // Match login error message
        }
    }
}