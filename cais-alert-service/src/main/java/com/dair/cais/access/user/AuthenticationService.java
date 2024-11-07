package com.dair.cais.access.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.dair.cais.access.UserBasedPermission.UserPermissionService;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final UserPermissionService userPermissionService;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService, UserPermissionService userPermissionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userPermissionService = userPermissionService;
    }

    public Optional<LoginResponseDto> authenticate(String userLoginName, String password) {
        return userRepository.findByUserLoginName(userLoginName)
                .filter(user -> passwordEncoder.matches(password, user.getUserLoginPassword()))
                .map(user -> {
                    LoginResponseDto response = new LoginResponseDto();
                    response.setUserId(Integer.valueOf(user.getUserId()));
                    response.setToken(jwtService.generateToken(user.getUserId(), user.getUserLoginName()));
                    return response;
                });
    }

    public UserEntity registerUser(UserEntity user) {
        user.setUserLoginPassword(passwordEncoder.encode(user.getUserLoginPassword()));
        user.setUserIsActive(true);
        return userRepository.save(user);
    }


    @Transactional()
    public List<UserDTO> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<UserDTO> updateUsersStatus(List<String> userIds, boolean activate) {
        log.debug("Updating status for users: {}, activate: {}", userIds, activate);

        List<UserEntity> users = userRepository.findAllById(userIds);
        List<String> notFoundIds = new ArrayList<>(userIds);
        notFoundIds.removeAll(users.stream()
                .map(UserEntity::getUserId)
                .collect(Collectors.toList()));

        if (!notFoundIds.isEmpty()) {
            log.error("Some users were not found: {}", notFoundIds);
            throw new ResourceNotFoundException("Users not found with IDs: " + notFoundIds);
        }

        users.forEach(user -> user.setUserIsActive(activate));
        List<UserEntity> savedUsers = userRepository.saveAll(users);

        log.info("Successfully updated status for {} users", users.size());

        return savedUsers.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public TokenValidationResponse validateToken(String token) {
        TokenValidationResponse response = new TokenValidationResponse();

        JwtService.TokenValidationResult validationResult = jwtService.validateAndExtractClaims(token);
        if (!validationResult.isValid()) {
            return null; // Return null for invalid token to match login behavior
        }

        // Set the same userId format as login response
        response.setUserId(Integer.valueOf(validationResult.getUserId()));

        // Set the same token back
        response.setToken(token);

        // Get user permissions
        try {
            ObjectNode permissions = userPermissionService.getUserPermissionFromMongo(validationResult.getUserId());
            response.setPermissions(permissions);
        } catch (Exception e) {
            log.error("Error fetching permissions for user {}: {}", validationResult.getUserId(), e.getMessage());
            return null;
        }

        return response;
    }
}