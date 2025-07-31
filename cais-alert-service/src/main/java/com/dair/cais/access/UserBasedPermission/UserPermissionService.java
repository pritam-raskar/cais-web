package com.dair.cais.access.UserBasedPermission;

import com.dair.cais.access.PolicyEntityMapping.PolicyEntityMappingEntity;
import com.dair.cais.access.PolicyEntityMapping.PolicyEntityMappingRepository;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingEntity;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingRepository;
import com.dair.cais.access.modules.ModuleEntity;
import com.dair.cais.access.modules.ModuleRepository;
import com.dair.cais.access.user.UserEntity;
import com.dair.cais.access.user.UserRepository;
import com.dair.cais.access.userOrgRole.UserOrgRoleMappingEntity;
import com.dair.cais.access.userOrgRole.UserOrgRoleMappingRepository;
import com.dair.cais.common.config.CaisAlertConstants;
import com.dair.cais.common.config.CustomCacheable;
import com.dair.cais.organization.OrganizationUnitEntity;
import com.dair.cais.organization.OrganizationUnitRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class UserPermissionService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserPermissionService.class);
    private final UserRepository userRepository;
    private final UserOrgRoleMappingRepository userOrgRoleMappingRepository;
    private final RolesPolicyMappingRepository rolesPolicyMappingRepository;
    private final PolicyEntityMappingRepository policyEntityMappingRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final CacheManager cacheManager;
    private final ModuleRepository moduleRepository;

    @Transactional
    public void refreshUserPermissions(String userId) {
        log.info("Refreshing permissions for user: {}", userId);
        UserPermissionDto newPermissions = generateStructuredDataForUser(userId);
        saveUserPermissionToMongo(userId, newPermissions);
        evictUserCaches(userId);
        log.info("Successfully refreshed permissions for user: {}", userId);
    }

    private void evictUserCaches(String userId) {
        log.debug("Evicting caches for user: {}", userId);
        cacheManager.getCache("userPermissions").evict(userId);
        cacheManager.getCache("userOrgUnits").evict(userId);
        cacheManager.getCache("userOrgKeys").evict(userId);
    }

    @Transactional(readOnly = true)
    public UserPermissionDto generateStructuredDataForUser(String userId) {
        log.info("Generating structured data for user with ID: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserPermissionDto userPermissionDto = new UserPermissionDto();
        userPermissionDto.setUserId(user.getUserId());
        userPermissionDto.setUser(new UserInfo(user.getUserId(),
                user.getUserFirstName() + " " + user.getUserLastName()));

        // Initialize permission wrapper
        PermissionWrapper permissionWrapper = new PermissionWrapper();

        // Initialize metadata sets
        Set<String> uniqueAlertTypesOrgId = new HashSet<>();
        Set<String> uniqueOrgId = new HashSet<>();
        Set<String> distinctOrgKeys = new HashSet<>();

        // Process permissions
        processUserPermissions(userId, permissionWrapper, uniqueAlertTypesOrgId,
                uniqueOrgId, distinctOrgKeys);

        userPermissionDto.setPermission(permissionWrapper);

        // Set metadata
        Metadata metadata = new Metadata();
        metadata.setUniqueAlertTypesOrgId(uniqueAlertTypesOrgId);
        metadata.setUniqueOrgId(uniqueOrgId);
        metadata.setDistinctOrgKeys(distinctOrgKeys);
        userPermissionDto.setMetadata(metadata);

        return userPermissionDto;
    }

    private void processRolePermissions(
            Integer roleId,
            String orgId,
            Map<String, Map<String, List<ActionCondition>>> generalPermissions,
            Map<String, Map<String, OrgActionsNew>> alertTypePermissions,
            Set<String> uniqueAlertTypesOrgId) {

        List<RolesPolicyMappingEntity> rolePolicyMappings =
                rolesPolicyMappingRepository.findByRoleRoleId(roleId);

        for (RolesPolicyMappingEntity rpm : rolePolicyMappings) {
            List<PolicyEntityMappingEntity> policyEntityMappings =
                    policyEntityMappingRepository.findByPolicyPolicyId(rpm.getPolicy().getPolicyId());

            for (PolicyEntityMappingEntity pem : policyEntityMappings) {
                String entityType = pem.getEntityType().toLowerCase();
                if ("alert-types".equals(entityType)) {
                    processOrgBasedPermission(pem, orgId, alertTypePermissions, uniqueAlertTypesOrgId);
                } else {
                    processGeneralPermission(pem, entityType, generalPermissions);
                }
            }
        }
    }

    private void processEntityPermission(
            PolicyEntityMappingEntity pem,
            String orgId,
            Map<String, Map<String, List<ActionCondition>>> generalPermissions,
            Map<String, Map<String, OrgActionsNew>> alertTypePermissions,
            Set<String> uniqueAlertTypesOrgId) {

        try {
            String entityType = pem.getEntityType().toLowerCase();
            log.debug("Processing entity permission for type: {}, id: {}", entityType, pem.getEntityId());

            if ("alert-types".equals(entityType)) {
                processOrgBasedPermission(pem, orgId, alertTypePermissions, uniqueAlertTypesOrgId);
            } else {
                processGeneralPermission(pem, entityType, generalPermissions);
            }
        } catch (Exception e) {
            log.error("Error processing entity permission for entity type: {}, id: {}: {}",
                    pem.getEntityType(), pem.getEntityId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process entity permission", e);
        }
    }

    private void processOrgBasedPermission_OLD(
            PolicyEntityMappingEntity pem,
            String orgId,
            Map<String, AlertTypeOrgPermissions> orgBasedPermissions,
            Set<String> uniqueAlertTypesOrgId) {

        String entityId = pem.getEntityId();
        AlertTypeOrgPermissions permissions = orgBasedPermissions.computeIfAbsent(
                entityId, k -> new AlertTypeOrgPermissions());

        Map<String, OrgActions> orgIdMap = permissions.getOrgId();
        if (orgIdMap == null) {
            orgIdMap = new HashMap<>();
            permissions.setOrgId(orgIdMap);
        }

        OrgActions orgActions = orgIdMap.computeIfAbsent(orgId, k -> {
            OrgActions actions = new OrgActions();
            actions.setActions(new ArrayList<>());
            return actions;
        });

        ActionCondition actionCondition = new ActionCondition(
                pem.getAction().getActionName(),
                pem.getCondition() != null ? pem.getCondition() : ""
        );

        if (!orgActions.getActions().contains(actionCondition)) {
            orgActions.getActions().add(actionCondition);
        }

        uniqueAlertTypesOrgId.add(entityId + ":" + orgId);
    }

    private void processGeneralPermission(
            PolicyEntityMappingEntity pem,
            String entityType,
            Map<String, Map<String, List<ActionCondition>>> generalPermissions) {

        try {
            if ("modules".equals(entityType)) {
                processModulePermission(pem, generalPermissions);
            } else {
                processOtherEntityPermission(pem, entityType, generalPermissions);
            }
        } catch (Exception e) {
            log.error("Error processing general permission for entity type: {}, id: {}: {}",
                    entityType, pem.getEntityId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process general permission", e);
        }
    }

    private void processModulePermission(
            PolicyEntityMappingEntity pem,
            Map<String, Map<String, List<ActionCondition>>> generalPermissions) {

        try {
            // Get module from repository using entityId
            Optional<ModuleEntity> moduleOpt = moduleRepository.findById(Integer.parseInt(pem.getEntityId()));
            if (moduleOpt.isPresent()) {
                ModuleEntity module = moduleOpt.get();
                String moduleName = module.getModuleName();

                Map<String, List<ActionCondition>> modulePermissions = generalPermissions
                        .computeIfAbsent("modules", k -> new HashMap<>());

                List<ActionCondition> actions = modulePermissions
                        .computeIfAbsent(moduleName, k -> new ArrayList<>());

                ActionCondition actionCondition = new ActionCondition(
                        pem.getAction().getActionName(),
                        pem.getCondition() != null ? pem.getCondition() : ""
                );

                if (!actions.contains(actionCondition)) {
                    actions.add(actionCondition);
                }

                log.debug("Successfully processed module permission for module: {}", moduleName);
            } else {
                log.warn("Module not found for ID: {}", pem.getEntityId());
            }
        } catch (NumberFormatException e) {
            log.error("Invalid module ID format: {}", pem.getEntityId(), e);
            throw new RuntimeException("Invalid module ID format", e);
        } catch (Exception e) {
            log.error("Error processing module permission for module ID: {}: {}",
                    pem.getEntityId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process module permission", e);
        }
    }

    private void processOtherEntityPermission(
            PolicyEntityMappingEntity pem,
            String entityType,
            Map<String, Map<String, List<ActionCondition>>> generalPermissions) {

        try {
            String entityId = pem.getEntityId();
            Map<String, List<ActionCondition>> entityTypePermissions = generalPermissions
                    .computeIfAbsent(entityType, k -> new HashMap<>());

            List<ActionCondition> actions = entityTypePermissions
                    .computeIfAbsent(entityId, k -> new ArrayList<>());

            ActionCondition actionCondition = new ActionCondition(
                    pem.getAction().getActionName(),
                    pem.getCondition() != null ? pem.getCondition() : ""
            );

            if (!actions.contains(actionCondition)) {
                actions.add(actionCondition);
            }

            log.debug("Successfully processed entity permission for type: {}, id: {}",
                    entityType, entityId);
        } catch (Exception e) {
            log.error("Error processing entity permission for type: {}, id: {}: {}",
                    entityType, pem.getEntityId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process entity permission", e);
        }
    }

    private void processOrgBasedPermission(
            PolicyEntityMappingEntity pem,
            String orgId,
            Map<String, Map<String, OrgActionsNew>> alertTypePermissions,
            Set<String> uniqueAlertTypesOrgId) {

        try {
            String entityId = pem.getEntityId();
            Map<String, OrgActionsNew> orgMap = alertTypePermissions.computeIfAbsent(entityId, k -> new HashMap<>());

            OrgActionsNew orgActions = orgMap.computeIfAbsent(orgId, k -> {
                OrgActionsNew actions = new OrgActionsNew();
                actions.setActions(new HashMap<>());
                return actions;
            });

            ActionFormat actionFormat = new ActionFormat();
            actionFormat.setCondition(pem.getCondition() != null ? pem.getCondition() : "");

            orgActions.getActions().put(pem.getAction().getActionName(), actionFormat);
            uniqueAlertTypesOrgId.add(entityId + ":" + orgId);

            log.debug("Successfully processed org-based permission for entity: {}, orgId: {}",
                    entityId, orgId);
        } catch (Exception e) {
            log.error("Error processing org-based permission for entity: {}, orgId: {}: {}",
                    pem.getEntityId(), orgId, e.getMessage(), e);
            throw new RuntimeException("Failed to process org-based permission", e);
        }
    }

    // Update the method signature in processUserPermissions
    private void processUserPermissions(
            String userId,
            PermissionWrapper permissionWrapper,
            Set<String> uniqueAlertTypesOrgId,
            Set<String> uniqueOrgId,
            Set<String> distinctOrgKeys) {

        Map<String, Map<String, OrgActionsNew>> alertTypePermissions = new HashMap<>();
        Map<String, Map<String, List<ActionCondition>>> generalPermissions = new HashMap<>();

        List<UserOrgRoleMappingEntity> userOrgRoleMappings =
                userOrgRoleMappingRepository.findByUserUserId(userId);

        for (UserOrgRoleMappingEntity uorm : userOrgRoleMappings) {
            String orgId = uorm.getOrgUnit().getOrgId().toString();
            Integer roleId = uorm.getRole().getRoleId();

            uniqueOrgId.add(orgId);

            // Get org key
            OrganizationUnitEntity orgUnit = organizationUnitRepository.findById(Integer.parseInt(orgId))
                    .orElseThrow(() -> new RuntimeException("Organization unit not found for ID: " + orgId));
            distinctOrgKeys.add(orgUnit.getOrgKey());

            processRolePermissions(roleId, orgId, generalPermissions, alertTypePermissions,
                    uniqueAlertTypesOrgId);
        }

        // Transform and set alert type permissions
        Map<String, AlertTypeOrgPermissionsNew> formattedAlertTypePermissions = transformAlertTypePermissions(alertTypePermissions);
        permissionWrapper.setPermissionsByType("alert-types", formattedAlertTypePermissions);

        // Set other permissions
        if (generalPermissions.containsKey("modules")) {
            permissionWrapper.setModules(generalPermissions.get("modules"));
        }
        if (generalPermissions.containsKey("reports")) {
            permissionWrapper.setReports(generalPermissions.get("reports"));
        }
    }

    private Map<String, AlertTypeOrgPermissionsNew> transformAlertTypePermissions(
            Map<String, Map<String, OrgActionsNew>> alertTypePermissions) {
        Map<String, AlertTypeOrgPermissionsNew> result = new HashMap<>();

        alertTypePermissions.forEach((alertType, orgMap) -> {
            AlertTypeOrgPermissionsNew permissions = new AlertTypeOrgPermissionsNew();
            permissions.setOrgId(new HashMap<>());

            orgMap.forEach((orgId, actions) -> {
                permissions.getOrgId().put(orgId, actions);
            });

            result.put(alertType, permissions);
        });

        return result;
    }

    @Transactional
    public void saveUserPermissionToMongo(String userId, UserPermissionDto userPermissionDto) {
        Query query = new Query(Criteria.where("userId").is(userId));
        FindAndReplaceOptions options = new FindAndReplaceOptions().upsert();

        UserPermissionDto result = mongoTemplate.findAndReplace(
                query,
                userPermissionDto,
                options,
                UserPermissionDto.class,
                CaisAlertConstants.USER_PERMISSION_DATA,
                UserPermissionDto.class
        );

        evictUserCaches(userId);

        if (result != null) {
            log.info("Updated existing user permissions in MongoDB for user ID: {}", userId);
        } else {
            log.info("Inserted new user permissions in MongoDB for user ID: {}", userId);
        }
    }

    @CustomCacheable(cacheNames = "userPermissions", key = "#userId")
    public ObjectNode getUserPermissionFromMongo(String userId) {
        log.info("Fetching user permissions from MongoDB for user ID: {}", userId);
        Query query = new Query(Criteria.where("_id").is(userId));

        Map<String, Object> userPermissionMap = mongoTemplate.findOne(
                query, Map.class, CaisAlertConstants.USER_PERMISSION_DATA);

        if (userPermissionMap != null) {
            log.info("Successfully fetched user permissions from MongoDB for user: {}", userId);
            return objectMapper.valueToTree(userPermissionMap);
        } else {
            log.warn("No user permissions found in MongoDB for user: {}", userId);
            return null;
        }
    }

    @CustomCacheable(cacheNames = "userOrgUnits", key = "#userId")
    public List<String> getDistinctOrgIdsForUser(String userId) {
        log.info("Fetching org units for user {} from MongoDB", userId);
        ObjectNode userPermissions = getUserPermissionFromMongo(userId);
        if (userPermissions != null && userPermissions.has("metadata")) {
            JsonNode metadata = userPermissions.get("metadata");
            if (metadata.has("uniqueOrgId")) {
                return StreamSupport.stream(metadata.get("uniqueOrgId").spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    @CustomCacheable(cacheNames = "userOrgKeys", key = "#userId")
    public List<String> getDistinctOrgKeysForUser(String userId) {
        log.info("Fetching org units for user {} from MongoDB", userId);
        ObjectNode userPermissions = getUserPermissionFromMongo(userId);
        if (userPermissions != null && userPermissions.has("metadata")) {
            JsonNode metadata = userPermissions.get("metadata");
            if (metadata.has("distinctOrgKeys")) {
                return StreamSupport.stream(metadata.get("distinctOrgKeys").spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}



//package com.dair.cais.access.UserBasedPermission;
//
//import com.dair.cais.access.PolicyAlertMapping.PolicyAlertTypeActionMappingEntity;
//import com.dair.cais.access.PolicyAlertMapping.PolicyAlertTypeActionMappingRepository;
//import com.dair.cais.access.PolicyModuleMapping.PolicyModuleMappingEntity;
//import com.dair.cais.access.PolicyModuleMapping.PolicyModuleMappingRepository;
//import com.dair.cais.access.PolicyReportMapping.PolicyReportActionMappingEntity;
//import com.dair.cais.access.PolicyReportMapping.PolicyReportActionMappingRepository;
//import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingEntity;
//import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingRepository;
//import com.dair.cais.access.policy.PolicyEntity;
//import com.dair.cais.access.user.UserEntity;
//import com.dair.cais.access.user.UserRepository;
//import com.dair.cais.access.userOrgRole.UserOrgRoleMappingEntity;
//import com.dair.cais.access.userOrgRole.UserOrgRoleMappingRepository;
//import com.dair.cais.common.config.CaisAlertConstants;
//import com.dair.cais.common.config.CustomCacheable;
//import com.dair.cais.organization.OrganizationUnitEntity;
//import com.dair.cais.organization.OrganizationUnitRepository;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cache.CacheManager;
//import org.springframework.data.mongodb.core.FindAndReplaceOptions;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import java.util.stream.StreamSupport;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class UserPermissionService {
//
//    private final UserRepository userRepository;
//    private final UserOrgRoleMappingRepository userOrgRoleMappingRepository;
//    private final RolesPolicyMappingRepository rolesPolicyMappingRepository;
//    private final PolicyAlertTypeActionMappingRepository policyAlertTypeActionMappingRepository;
//    private final PolicyModuleMappingRepository policyModuleMappingRepository;
//    private final PolicyReportActionMappingRepository policyReportActionMappingRepository;
//    private final OrganizationUnitRepository organizationUnitRepository;
//    private final ObjectMapper objectMapper;
//    private final MongoTemplate mongoTemplate;
//    private final CacheManager cacheManager;
//
//
//    @Transactional
//    public void refreshUserPermissions(String userId) {
//        log.info("Refreshing permissions for user: {}", userId);
//        // Generate new permissions
//        UserPermissionDto newPermissions = generateStructuredDataForUser(userId);
//
//        // Save to MongoDB
//        saveUserPermissionToMongo(userId, newPermissions);
//
//        // Clear all caches for this user
//        evictUserCaches(userId);
//
//        log.info("Successfully refreshed permissions for user: {}", userId);
//    }
//
//    private void evictUserCaches(String userId) {
//        log.debug("Evicting caches for user: {}", userId);
//        cacheManager.getCache("userPermissions").evict(userId);
//        cacheManager.getCache("userOrgUnits").evict(userId);
//        cacheManager.getCache("userOrgKeys").evict(userId);
//    }
//
//    @Transactional
//    public void saveUserPermissionToMongo(String userId, UserPermissionDto userPermissionDto) {
//        Query query = new Query(Criteria.where("userId").is(userId));
//        FindAndReplaceOptions options = new FindAndReplaceOptions().upsert();
//
//        UserPermissionDto result = mongoTemplate.findAndReplace(
//                query,
//                userPermissionDto,
//                options,
//                UserPermissionDto.class,
//                CaisAlertConstants.USER_PERMISSION_DATA,
//                UserPermissionDto.class
//        );
//
//        // Evict caches after updating MongoDB
//        evictUserCaches(userId);
//
//        if (result != null) {
//            log.info("Updated existing user permissions in MongoDB for user ID: {}", userId);
//        } else {
//            log.info("Inserted new user permissions in MongoDB for user ID: {}", userId);
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public UserPermissionDto generateStructuredDataForUser(String userId) {
//        log.info("Generating structured data for user with ID: {}", userId);
//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> {
//                    log.error("User not found with ID: {}", userId);
//                    return new RuntimeException("User not found with ID: " + userId);
//                });
//
//        UserPermissionDto userPermissionDto = new UserPermissionDto();
//        userPermissionDto.setUserId(user.getUserId());
//        userPermissionDto.setUser(new UserInfo(user.getUserId(), user.getUserFirstName() + " " + user.getUserLastName()));
//
//        Map<String, AlertTypeOrgPermissions> alertTypePermissions = new HashMap<>();
//        Map<String, List<ActionCondition>> modulePermissions = new HashMap<>();
//        Map<String, List<ActionCondition>> reportPermissions = new HashMap<>();
//
//        Set<String> uniqueAlertTypesOrgId = new HashSet<>();
//        Set<String> uniqueOrgId = new HashSet<>();
//        Map<String, String> orgIdToKeyMap = new HashMap<>();
//        Set<String> distinctOrgKeys = new HashSet<>();
//
//        List<UserOrgRoleMappingEntity> userOrgRoleMappings = userOrgRoleMappingRepository.findByUserUserId(userId);
//        log.debug("Found {} org-role mappings for user {}", userOrgRoleMappings.size(), userId);
//
//        for (UserOrgRoleMappingEntity uorm : userOrgRoleMappings) {
//            processUserOrgRoleMapping(uorm, alertTypePermissions, modulePermissions, reportPermissions,
//                    uniqueAlertTypesOrgId, uniqueOrgId, orgIdToKeyMap, distinctOrgKeys);
//        }
//
//        PermissionWrapper permissionWrapper = new PermissionWrapper();
//        permissionWrapper.setAlertType(alertTypePermissions);
//        permissionWrapper.setModules(modulePermissions);
//        permissionWrapper.setReports(reportPermissions);
//        userPermissionDto.setPermission(permissionWrapper);
//
//        Metadata metadata = new Metadata();
//        metadata.setUniqueAlertTypesOrgId(uniqueAlertTypesOrgId);
//        metadata.setUniqueOrgId(uniqueOrgId);
////        metadata.setOrgIdToKeyMap(orgIdToKeyMap);
//        metadata.setDistinctOrgKeys(distinctOrgKeys);
//        userPermissionDto.setMetadata(metadata);
//
//        log.info("Successfully generated structured data for user with ID: {}", userId);
//        return userPermissionDto;
//    }
//
//    @CustomCacheable(cacheNames = "userPermissions", key = "#userId")
//    public ObjectNode getUserPermissionFromMongo(String userId) {
//        log.info("Fetching User permissions from MongoDB for user ID: {}", userId);
//        Query query = new Query(Criteria.where("_id").is(userId));
//
//        Map<String, Object> userPermissionMap = mongoTemplate.findOne(query, Map.class, CaisAlertConstants.USER_PERMISSION_DATA);
//
//        if (userPermissionMap != null) {
//            log.info("Successfully fetched User permissions from MongoDB for user: {}", userId);
//            return objectMapper.valueToTree(userPermissionMap);
//        } else {
//            log.warn("No User permissions found in MongoDB for user: {}", userId);
//            return null;
//        }
//    }
//
//    @CustomCacheable(cacheNames = "userOrgUnits", key = "#userId")
//    public List<String> getDistinctOrgIdsForUser(String userId) {
//        log.info("Fetching org units for user {} from MongoDB", userId);
//        ObjectNode userPermissions = getUserPermissionFromMongo(userId);
//        if (userPermissions != null && userPermissions.has("metadata")) {
//            JsonNode metadata = userPermissions.get("metadata");
//            if (metadata.has("uniqueOrgId")) {
//                return StreamSupport.stream(metadata.get("uniqueOrgId").spliterator(), false)
//                        .map(JsonNode::asText)
//                        .collect(Collectors.toList());
//            }
//        }
//        return Collections.emptyList();
//    }
//
//
//    @CustomCacheable(cacheNames = "userOrgKeys", key = "#userId")
//    public List<String> getDistinctOrgKeysForUser(String userId) {
//        log.info("Fetching org units for user {} from MongoDB", userId);
//        ObjectNode userPermissions = getUserPermissionFromMongo(userId);
//        if (userPermissions != null && userPermissions.has("metadata")) {
//            JsonNode metadata = userPermissions.get("metadata");
//            if (metadata.has("distinctOrgKeys")) {
//                return StreamSupport.stream(metadata.get("distinctOrgKeys").spliterator(), false)
//                        .map(JsonNode::asText)
//                        .collect(Collectors.toList());
//            }
//        }
//        return Collections.emptyList();
//    }
//
//    private void processUserOrgRoleMapping(UserOrgRoleMappingEntity uorm,
//                                           Map<String, AlertTypeOrgPermissions> alertTypePermissions,
//                                           Map<String, List<ActionCondition>> modulePermissions,
//                                           Map<String, List<ActionCondition>> reportPermissions,
//                                           Set<String> uniqueAlertTypesOrgId,
//                                           Set<String> uniqueOrgId,
//                                           Map<String, String> orgIdToKeyMap,
//                                           Set<String> distinctOrgKeys) {
//        String orgId = uorm.getOrgUnit().getOrgId().toString();
//        Integer roleId = uorm.getRole().getRoleId();
//
//        uniqueOrgId.add(orgId);
//
//        // Fetch org_key for the current orgId
//        OrganizationUnitEntity orgUnit = organizationUnitRepository.findById(Integer.parseInt(orgId))
//                .orElseThrow(() -> new RuntimeException("Organization unit not found for ID: " + orgId));
//
//        orgIdToKeyMap.put(orgId, orgUnit.getOrgKey());
//        distinctOrgKeys.add(orgUnit.getOrgKey());
//
//        List<RolesPolicyMappingEntity> rolePolicyMappings = rolesPolicyMappingRepository.findByRoleRoleId(roleId);
//        log.debug("Found {} policy mappings for role {}", rolePolicyMappings.size(), roleId);
//
//        for (RolesPolicyMappingEntity rpm : rolePolicyMappings) {
//            processAlertTypePermissions(rpm.getPolicy(), orgId, alertTypePermissions, uniqueAlertTypesOrgId);
//            processModulePermissions(rpm.getPolicy(), modulePermissions);
//            processReportPermissions(rpm.getPolicy(), reportPermissions);
//        }
//    }
//
//    private void processAlertTypePermissions(PolicyEntity policy, String orgId,
//                                             Map<String, AlertTypeOrgPermissions> alertTypePermissions,
//                                             Set<String> uniqueAlertTypesOrgId) {
//        List<PolicyAlertTypeActionMappingEntity> alertMappings = policyAlertTypeActionMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
//        log.debug("Processing {} alert type mappings for policy {}", alertMappings.size(), policy.getPolicyId());
//
//        for (PolicyAlertTypeActionMappingEntity alertMapping : alertMappings) {
//            String alertTypeId = alertMapping.getAlertType().getAlertTypeId();
//            AlertTypeOrgPermissions atop = alertTypePermissions.computeIfAbsent(alertTypeId, k -> new AlertTypeOrgPermissions());
//
//            Map<String, OrgActions> orgIdMap = atop.getOrgId();
//            if (orgIdMap == null) {
//                orgIdMap = new HashMap<>();
//                atop.setOrgId(orgIdMap);
//            }
//
//            OrgActions actions = orgIdMap.computeIfAbsent(orgId, k -> new OrgActions());
//            if (actions.getActions() == null) {
//                actions.setActions(new ArrayList<>());
//            }
//
//            ActionCondition actionCondition = new ActionCondition();
//            actionCondition.setAction(alertMapping.getAction().getActionName());
//            actionCondition.setCondition(alertMapping.getCondition() != null ? alertMapping.getCondition() : "N/A");
//
//            actions.getActions().add(actionCondition);
//
//            uniqueAlertTypesOrgId.add(alertTypeId + ":" + orgId);
//        }
//    }
//
//    private void processModulePermissions(PolicyEntity policy, Map<String, List<ActionCondition>> modulePermissions) {
//        List<PolicyModuleMappingEntity> moduleMappings = policyModuleMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
//        log.debug("Processing {} module mappings for policy {}", moduleMappings.size(), policy.getPolicyId());
//        processGenericPermissions(modulePermissions, moduleMappings,
//                entity -> entity.getModule().getModuleName(),
//                entity -> new ActionCondition(entity.getAction().getActionName(),
//                        entity.getCondition() != null ? entity.getCondition() : "N/A"));
//    }
//
//    private void processReportPermissions(PolicyEntity policy, Map<String, List<ActionCondition>> reportPermissions) {
//        List<PolicyReportActionMappingEntity> reportMappings = policyReportActionMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
//        log.debug("Processing {} report mappings for policy {}", reportMappings.size(), policy.getPolicyId());
//        processGenericPermissions(reportPermissions, reportMappings,
//                entity -> entity.getReport().getReportName(),
//                entity -> new ActionCondition(entity.getAction().getActionName(),
//                        entity.getCondition() != null ? entity.getCondition() : "N/A"));
//    }
//
//    private <T> void processGenericPermissions(
//            Map<String, List<ActionCondition>> permissions,
//            List<T> mappings,
//            Function<T, String> nameExtractor,
//            Function<T, ActionCondition> actionConditionExtractor) {
//        for (T mapping : mappings) {
//            String name = nameExtractor.apply(mapping);
//            List<ActionCondition> actions = permissions.computeIfAbsent(name, k -> new ArrayList<>());
//
//            ActionCondition actionCondition = actionConditionExtractor.apply(mapping);
//
//            if (!actions.contains(actionCondition)) {
//                actions.add(actionCondition);
//            }
//        }
//    }
//}




