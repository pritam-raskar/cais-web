package com.dair.cais.access.Role;

import com.dair.cais.access.user.UserEntity;
import com.dair.cais.access.userOrgRole.UserOrgRoleMappingRepository;
import com.dair.cais.exception.RoleDeleteException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserOrgRoleMappingRepository userOrgRoleMappingRepository;

    @Autowired
    private RoleMapper roleMapper;

    public Role createRole(Role role) {
        RoleEntity entity = roleMapper.toEntity(role);
        entity = roleRepository.save(entity);
        return roleMapper.toModel(entity);
    }

    public Role getRoleByName(String roleName) {
        RoleEntity entity = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return roleMapper.toModel(entity);
    }

    public List<Role> getAllRoles() {
        List<RoleEntity> entities = roleRepository.findAll();
        return entities.stream()
                .map(roleMapper::toModel)
                .collect(Collectors.toList());
    }

    public void deleteRole(Integer roleId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        long userCount = userOrgRoleMappingRepository.countByRole(role);
        if (userCount > 0) {
            throw new RoleDeleteException("Cannot delete role as it is associated with users", userCount);
        }

        roleRepository.delete(role);
    }

    public List<Role> getAllRolesWithUserCount() {
        List<RoleEntity> entities = roleRepository.findAll();
        return entities.stream()
                .map(entity -> {
                    Role role = roleMapper.toModel(entity);
                    long userCount = userOrgRoleMappingRepository.countByRole(entity);
                    role.setUserCount(userCount);
                    return role;
                })
                .collect(Collectors.toList());
    }

    public List<UserEntity> getUsersByRoleName(String roleName) {
        RoleEntity role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return userOrgRoleMappingRepository.findUsersByRole(role);
    }
}