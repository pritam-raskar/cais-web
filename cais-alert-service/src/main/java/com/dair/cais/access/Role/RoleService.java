package com.dair.cais.access.Role;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.user.UserEntity;
import com.dair.cais.access.userOrgRole.UserOrgRoleMappingRepository;
import com.dair.cais.exception.RoleDeleteException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserOrgRoleMappingRepository userOrgRoleMappingRepository;
    private final RoleMapper roleMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ActionRepository actionRepository;
    private final MongoTemplate mongoTemplate;

    public RoleService(RoleRepository roleRepository,
                       UserOrgRoleMappingRepository userOrgRoleMappingRepository,
                       RoleMapper roleMapper,
                       ApplicationEventPublisher eventPublisher, ActionRepository actionRepository, MongoTemplate mongoTemplate) {
        this.roleRepository = roleRepository;
        this.userOrgRoleMappingRepository = userOrgRoleMappingRepository;
        this.roleMapper = roleMapper;
        this.eventPublisher = eventPublisher;
        this.actionRepository = actionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public Role updateRole(Integer roleId, Role roleUpdate) {
        log.info("Updating role with ID: {}", roleId);
        RoleEntity entity = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        // Preserve the ID when mapping
        roleUpdate.setRoleId(roleId);
        RoleEntity updatedEntity = roleMapper.toEntity(roleUpdate);
        updatedEntity = roleRepository.save(updatedEntity);

        // Publish event for async processing
        try {
            eventPublisher.publishEvent(new RoleUpdateEvent(this, roleId));
            log.info("Published role update event for roleId: {}", roleId);
        } catch (Exception e) {
            log.error("Failed to publish role update event for roleId: {}", roleId, e);
            // Continue with the update even if event publishing fails
        }

        return roleMapper.toModel(updatedEntity);
    }

    @Transactional
    public Role createRole(Role role) {
        log.info("Creating new role: {}", role.getRoleName());
        RoleEntity entity = roleMapper.toEntity(role);
        entity = roleRepository.save(entity);
        return roleMapper.toModel(entity);
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String roleName) {
        log.info("Fetching role by name: {}", roleName);
        RoleEntity entity = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + roleName));
        return roleMapper.toModel(entity);
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Integer roleId) {
        log.info("Fetching role by Id: {}", roleId);
        RoleEntity entity = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with Id: " + roleId));
        return roleMapper.toModel(entity);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        log.info("Fetching all roles");
        List<RoleEntity> entities = roleRepository.findAll();
        return entities.stream()
                .map(roleMapper::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRole(Integer roleId) {
        log.info("Attempting to delete role with ID: {}", roleId);
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        long userCount = userOrgRoleMappingRepository.countByRole(role);
        if (userCount > 0) {
            log.warn("Cannot delete role {} as it is associated with {} users", roleId, userCount);
            throw new RoleDeleteException("Cannot delete role as it is associated with users", userCount);
        }

        roleRepository.delete(role);
        log.info("Successfully deleted role with ID: {}", roleId);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRolesWithUserCount() {
        log.info("Fetching all roles with user count");
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

    @Transactional(readOnly = true)
    public List<UserEntity> getUsersByRoleName(String roleName) {
        log.info("Fetching users for role: {}", roleName);
        RoleEntity role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + roleName));
        return userOrgRoleMappingRepository.findUsersByRole(role);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRoleTemplate() {
        log.debug("Getting role template");
        Map<String, Object> template = new HashMap<>();

        try {
            // Get Alert Type actions
            List<ActionEntity> actions = actionRepository.findByActionCategory("Alert_type");
            List<Map<String, Object>> actionsList = actions.stream()
                    .map(action -> {
                        Map<String, Object> actionMap = new HashMap<>();
                        actionMap.put("action_id", action.getActionId());
                        actionMap.put("action_name", action.getActionName());
                        return actionMap;
                    })
                    .collect(Collectors.toList());

            // Get alert types from MongoDB using find query with isActive criteria
            Query query = new Query();
            query.addCriteria(Criteria.where("isActive").is(true));
            List<Document> alertTypes = mongoTemplate.find(query, Document.class, "alertTypes");

            List<Map<String, Object>> dataList = alertTypes.stream()
                    .map(alertType -> {
                        Map<String, Object> typeMap = new HashMap<>();
                        typeMap.put("id", alertType.get("_id").toString());
                        typeMap.put("name", alertType.get("typeName"));
                        typeMap.put("actions", new ArrayList<Integer>(actions.size()));
                        return typeMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> alertTypeMap = new HashMap<>();
            alertTypeMap.put("actions", actionsList);
            alertTypeMap.put("data", dataList);

            template.put("Alert_type", alertTypeMap);

            log.info("Successfully generated role template with {} alert types", dataList.size());
            return template;
        } catch (Exception e) {
            log.error("Error generating role template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate role template", e);
        }
    }
}