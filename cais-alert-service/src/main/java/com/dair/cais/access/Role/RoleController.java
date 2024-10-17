package com.dair.cais.access.Role;

import com.dair.cais.access.user.UserEntity;
import com.dair.cais.exception.RoleDeleteException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("create")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String roleName) {
        Role role = roleService.getRoleByName(roleName);
        return ResponseEntity.ok(role);
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("allroledetails")
    public ResponseEntity<List<Role>> getAllRolesWithUserCount() {
        List<Role> roles = roleService.getAllRolesWithUserCount();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleName}/users")
    public ResponseEntity<List<UserEntity>> getUsersByRoleName(@PathVariable String roleName) {
        List<UserEntity> users = roleService.getUsersByRoleName(roleName);
        return ResponseEntity.ok(users);
    }
    @DeleteMapping("/delete/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable Integer roleId) {
        try {
            roleService.deleteRole(roleId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RoleDeleteException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("userCount", e.getUserCount());
            return ResponseEntity.status(409).body(response);
        }
    }
}
