package com.dair.cais.access.user.service;

import com.dair.cais.access.user.*;
import com.dair.cais.access.user.dto.UserCreateRequest;
import com.dair.cais.access.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        log.debug("Creating new user with login name: {}", request.getUserLoginName());

        if (userRepository.existsByUserLoginName(request.getUserLoginName())) {
            log.error("User with login name {} already exists", request.getUserLoginName());
            throw new IllegalArgumentException("User with this login name already exists");
        }

        UserEntity userEntity = userMapper.createEntityFromRequest(request);
        userEntity = userRepository.save(userEntity);
        log.info("Successfully created user with ID: {}", userEntity.getUserId());

        return userMapper.toDto(userEntity);
    }


    @Transactional
    public UserDTO updateUser(String userId, UserUpdateRequest request) {
        log.debug("Updating user with ID: {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        userMapper.updateEntityFromRequest(userEntity, request);

        if (request.getNewPassword() != null) {
            userEntity.setUserLoginPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userEntity = userRepository.save(userEntity);
        log.info("Successfully updated user with ID: {}", userId);

        return userMapper.toDto(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(String userId) {
        log.debug("Fetching user with ID: {}", userId);

        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new EntityNotFoundException("User not found");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        log.debug("Deleting user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new EntityNotFoundException("User not found");
        }

        userRepository.deleteById(userId);
        log.info("Successfully deleted user with ID: {}", userId);
    }

    @Override
    @Transactional
    public void updateUserStatus(String userId, boolean active) {
        log.debug("Updating status for user ID: {} to {}", userId, active);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        userEntity.setUserIsActive(active);
        userRepository.save(userEntity);
        log.info("Successfully updated status for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void updateUserPassword(String userId, String newPassword) {
        log.debug("Updating password for user ID: {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        userEntity.setUserLoginPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
        log.info("Successfully updated password for user ID: {}", userId);
    }

    @Override
    public boolean existsByLoginName(String loginName) {
        return userRepository.existsByUserLoginName(loginName);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRoleId(Integer roleId) {
        log.debug("Fetching users for role ID: {}", roleId);
        // Implementation depends on your role-user relationship
        // We'll need to implement this based on your database schema
        throw new UnsupportedOperationException("Not implemented yet");
    }
}