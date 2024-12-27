package com.dair.cais.access.user.controller;

import com.dair.cais.access.user.UserDTO;
import com.dair.cais.access.user.dto.*;
import com.dair.cais.access.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.debug("Received request to create user with login name: {}", request.getUserLoginName());
        UserDTO createdUser = userService.createUser(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        log.debug("Received request to update user with ID: {}", userId);
        UserDTO updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        log.debug("Received request to fetch user with ID: {}", userId);
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        log.debug("Received request to fetch all users");
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        log.debug("Received request to delete user with ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Update user status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable String userId,
            @RequestParam boolean active) {
        log.debug("Received request to update status for user ID: {} to {}", userId, active);
        userService.updateUserStatus(userId, active);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/password")
    @Operation(summary = "Update user password")
    public ResponseEntity<Void> updateUserPassword(
            @PathVariable String userId,
            @RequestBody PasswordUpdateRequest request) {
        log.debug("Received request to update password for user ID: {}", userId);
        userService.updateUserPassword(userId, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "Get users by role ID")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Integer roleId) {
        log.debug("Received request to fetch users for role ID: {}", roleId);
        List<UserDTO> users = userService.getUsersByRoleId(roleId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/check/login-name")
    @Operation(summary = "Check if login name is available")
    public ResponseEntity<Boolean> checkLoginNameAvailability(
            @RequestParam String loginName) {
        log.debug("Checking availability of login name: {}", loginName);
        boolean exists = userService.existsByLoginName(loginName);
        return ResponseEntity.ok(!exists);
    }

    @GetMapping("/check/email")
    @Operation(summary = "Check if email is available")
    public ResponseEntity<Boolean> checkEmailAvailability(
            @RequestParam String email) {
        log.debug("Checking availability of email: {}", email);
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(!exists);
    }
}