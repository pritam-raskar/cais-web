package com.dair.cais.access.user.service;

import com.dair.cais.access.user.UserDTO;
import com.dair.cais.access.user.dto.UserCreateRequest;
import com.dair.cais.access.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserService {
    UserDTO createUser(UserCreateRequest request);
    UserDTO updateUser(String userId, UserUpdateRequest request);
    UserDTO getUserById(String userId);
    Page<UserDTO> getAllUsers(Pageable pageable);
    void deleteUser(String userId);
    void updateUserStatus(String userId, boolean active);
    void updateUserPassword(String userId, String newPassword);
    boolean existsByLoginName(String loginName);
    boolean existsByEmail(String email);
    List<UserDTO> getUsersByRoleId(Integer roleId);
}