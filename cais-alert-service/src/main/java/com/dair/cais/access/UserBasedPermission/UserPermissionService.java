package com.dair.cais.access.UserBasedPermission;

import com.dair.cais.access.PolicyAlertMapping.PolicyAlertTypeActionMappingEntity;
import com.dair.cais.access.PolicyAlertMapping.PolicyAlertTypeActionMappingRepository;
import com.dair.cais.access.PolicyModuleMapping.PolicyModuleMappingEntity;
import com.dair.cais.access.PolicyModuleMapping.PolicyModuleMappingRepository;
import com.dair.cais.access.PolicyReportMapping.PolicyReportActionMappingEntity;
import com.dair.cais.access.PolicyReportMapping.PolicyReportActionMappingRepository;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingEntity;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingRepository;
import com.dair.cais.access.policy.PolicyEntity;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final UserRepository userRepository;
    private final UserOrgRoleMappingRepository userOrgRoleMappingRepository;
    private final RolesPolicyMappingRepository rolesPolicyMappingRepository;
    private final PolicyAlertTypeActionMappingRepository policyAlertTypeActionMappingRepository;
    private final PolicyModuleMappingRepository policyModuleMappingRepository;
    private final PolicyReportActionMappingRepository policyReportActionMappingRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final CacheManager cacheManager;


    @Transactional
    public void refreshUserPermissions(String userId) {
        log.info("Refreshing permissions for user: {}", userId);
        // Generate new permissions
        UserPermissionDto newPermissions = generateStructuredDataForUser(userId);

        // Save to MongoDB
        saveUserPermissionToMongo(userId, newPermissions);

        // Clear all caches for this user
        evictUserCaches(userId);

        log.info("Successfully refreshed permissions for user: {}", userId);
    }

    private void evictUserCaches(String userId) {
        log.debug("Evicting caches for user: {}", userId);
        cacheManager.getCache("userPermissions").evict(userId);
        cacheManager.getCache("userOrgUnits").evict(userId);
        cacheManager.getCache("userOrgKeys").evict(userId);
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

        // Evict caches after updating MongoDB
        evictUserCaches(userId);

        if (result != null) {
            log.info("Updated existing user permissions in MongoDB for user ID: {}", userId);
        } else {
            log.info("Inserted new user permissions in MongoDB for user ID: {}", userId);
        }
    }

    @Transactional(readOnly = true)
    public UserPermissionDto generateStructuredDataForUser(String userId) {
        log.info("Generating structured data for user with ID: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found with ID: " + userId);
                });

        UserPermissionDto userPermissionDto = new UserPermissionDto();
        userPermissionDto.setUserId(user.getUserId());
        userPermissionDto.setUser(new UserInfo(user.getUserId(), user.getUserFirstName() + " " + user.getUserLastName()));

        Map<String, AlertTypeOrgPermissions> alertTypePermissions = new HashMap<>();
        Map<String, List<ActionCondition>> modulePermissions = new HashMap<>();
        Map<String, List<ActionCondition>> reportPermissions = new HashMap<>();

        Set<String> uniqueAlertTypesOrgId = new HashSet<>();
        Set<String> uniqueOrgId = new HashSet<>();
        Map<String, String> orgIdToKeyMap = new HashMap<>();
        Set<String> distinctOrgKeys = new HashSet<>();

        List<UserOrgRoleMappingEntity> userOrgRoleMappings = userOrgRoleMappingRepository.findByUserUserId(userId);
        log.debug("Found {} org-role mappings for user {}", userOrgRoleMappings.size(), userId);

        for (UserOrgRoleMappingEntity uorm : userOrgRoleMappings) {
            processUserOrgRoleMapping(uorm, alertTypePermissions, modulePermissions, reportPermissions,
                    uniqueAlertTypesOrgId, uniqueOrgId, orgIdToKeyMap, distinctOrgKeys);
        }

        PermissionWrapper permissionWrapper = new PermissionWrapper();
        permissionWrapper.setAlertType(alertTypePermissions);
        permissionWrapper.setModules(modulePermissions);
        permissionWrapper.setReports(reportPermissions);
        userPermissionDto.setPermission(permissionWrapper);

        Metadata metadata = new Metadata();
        metadata.setUniqueAlertTypesOrgId(uniqueAlertTypesOrgId);
        metadata.setUniqueOrgId(uniqueOrgId);
//        metadata.setOrgIdToKeyMap(orgIdToKeyMap);
        metadata.setDistinctOrgKeys(distinctOrgKeys);
        userPermissionDto.setMetadata(metadata);

        log.info("Successfully generated structured data for user with ID: {}", userId);
        return userPermissionDto;
    }

    @CustomCacheable(cacheNames = "userPermissions", key = "#userId")
    public ObjectNode getUserPermissionFromMongo(String userId) {
        log.info("Fetching User permissions from MongoDB for user ID: {}", userId);
        Query query = new Query(Criteria.where("_id").is(userId));

        Map<String, Object> userPermissionMap = mongoTemplate.findOne(query, Map.class, CaisAlertConstants.USER_PERMISSION_DATA);

        if (userPermissionMap != null) {
            log.info("Successfully fetched User permissions from MongoDB for user: {}", userId);
            return objectMapper.valueToTree(userPermissionMap);
        } else {
            log.warn("No User permissions found in MongoDB for user: {}", userId);
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

    private void processUserOrgRoleMapping(UserOrgRoleMappingEntity uorm,
                                           Map<String, AlertTypeOrgPermissions> alertTypePermissions,
                                           Map<String, List<ActionCondition>> modulePermissions,
                                           Map<String, List<ActionCondition>> reportPermissions,
                                           Set<String> uniqueAlertTypesOrgId,
                                           Set<String> uniqueOrgId,
                                           Map<String, String> orgIdToKeyMap,
                                           Set<String> distinctOrgKeys) {
        String orgId = uorm.getOrgUnit().getOrgId().toString();
        Integer roleId = uorm.getRole().getRoleId();

        uniqueOrgId.add(orgId);

        // Fetch org_key for the current orgId
        OrganizationUnitEntity orgUnit = organizationUnitRepository.findById(Integer.parseInt(orgId))
                .orElseThrow(() -> new RuntimeException("Organization unit not found for ID: " + orgId));

        orgIdToKeyMap.put(orgId, orgUnit.getOrgKey());
        distinctOrgKeys.add(orgUnit.getOrgKey());

        List<RolesPolicyMappingEntity> rolePolicyMappings = rolesPolicyMappingRepository.findByRoleRoleId(roleId);
        log.debug("Found {} policy mappings for role {}", rolePolicyMappings.size(), roleId);

        for (RolesPolicyMappingEntity rpm : rolePolicyMappings) {
            processAlertTypePermissions(rpm.getPolicy(), orgId, alertTypePermissions, uniqueAlertTypesOrgId);
            processModulePermissions(rpm.getPolicy(), modulePermissions);
            processReportPermissions(rpm.getPolicy(), reportPermissions);
        }
    }

    private void processAlertTypePermissions(PolicyEntity policy, String orgId,
                                             Map<String, AlertTypeOrgPermissions> alertTypePermissions,
                                             Set<String> uniqueAlertTypesOrgId) {
        List<PolicyAlertTypeActionMappingEntity> alertMappings = policyAlertTypeActionMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
        log.debug("Processing {} alert type mappings for policy {}", alertMappings.size(), policy.getPolicyId());

        for (PolicyAlertTypeActionMappingEntity alertMapping : alertMappings) {
            String alertTypeId = alertMapping.getAlertType().getAlertTypeId();
            AlertTypeOrgPermissions atop = alertTypePermissions.computeIfAbsent(alertTypeId, k -> new AlertTypeOrgPermissions());

            Map<String, OrgActions> orgIdMap = atop.getOrgId();
            if (orgIdMap == null) {
                orgIdMap = new HashMap<>();
                atop.setOrgId(orgIdMap);
            }

            OrgActions actions = orgIdMap.computeIfAbsent(orgId, k -> new OrgActions());
            if (actions.getActions() == null) {
                actions.setActions(new ArrayList<>());
            }

            ActionCondition actionCondition = new ActionCondition();
            actionCondition.setAction(alertMapping.getAction().getActionName());
            actionCondition.setCondition(alertMapping.getCondition() != null ? alertMapping.getCondition() : "N/A");

            actions.getActions().add(actionCondition);

            uniqueAlertTypesOrgId.add(alertTypeId + ":" + orgId);
        }
    }

    private void processModulePermissions(PolicyEntity policy, Map<String, List<ActionCondition>> modulePermissions) {
        List<PolicyModuleMappingEntity> moduleMappings = policyModuleMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
        log.debug("Processing {} module mappings for policy {}", moduleMappings.size(), policy.getPolicyId());
        processGenericPermissions(modulePermissions, moduleMappings,
                entity -> entity.getModule().getModuleName(),
                entity -> new ActionCondition(entity.getAction().getActionName(),
                        entity.getCondition() != null ? entity.getCondition() : "N/A"));
    }

    private void processReportPermissions(PolicyEntity policy, Map<String, List<ActionCondition>> reportPermissions) {
        List<PolicyReportActionMappingEntity> reportMappings = policyReportActionMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
        log.debug("Processing {} report mappings for policy {}", reportMappings.size(), policy.getPolicyId());
        processGenericPermissions(reportPermissions, reportMappings,
                entity -> entity.getReport().getReportName(),
                entity -> new ActionCondition(entity.getAction().getActionName(),
                        entity.getCondition() != null ? entity.getCondition() : "N/A"));
    }

    private <T> void processGenericPermissions(
            Map<String, List<ActionCondition>> permissions,
            List<T> mappings,
            Function<T, String> nameExtractor,
            Function<T, ActionCondition> actionConditionExtractor) {
        for (T mapping : mappings) {
            String name = nameExtractor.apply(mapping);
            List<ActionCondition> actions = permissions.computeIfAbsent(name, k -> new ArrayList<>());

            ActionCondition actionCondition = actionConditionExtractor.apply(mapping);

            if (!actions.contains(actionCondition)) {
                actions.add(actionCondition);
            }
        }
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
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
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
//    private final ObjectMapper objectMapper;
//    private final MongoTemplate mongoTemplate;
//
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
//
//        List<UserOrgRoleMappingEntity> userOrgRoleMappings = userOrgRoleMappingRepository.findByUserUserId(userId);
//        log.debug("Found {} org-role mappings for user {}", userOrgRoleMappings.size(), userId);
//
//        for (UserOrgRoleMappingEntity uorm : userOrgRoleMappings) {
//            processUserOrgRoleMapping(uorm, alertTypePermissions, modulePermissions, reportPermissions, uniqueAlertTypesOrgId, uniqueOrgId);
//        }
//        PermissionWrapper permissionWrapper = new PermissionWrapper();
//        permissionWrapper.setAlertType(alertTypePermissions);
//        permissionWrapper.setModules(modulePermissions);
//        permissionWrapper.setReports(reportPermissions);
//        userPermissionDto.setPermission(permissionWrapper);
//
//        Metadata metadata = new Metadata();
//        metadata.setUniqueAlertTypesOrgId(uniqueAlertTypesOrgId);
//        metadata.setUniqueOrgId(uniqueOrgId);
//        userPermissionDto.setMetadata(metadata);
//
//        log.info("Successfully generated structured data for user with ID: {}", userId);
//        return userPermissionDto;
//    }
//
//    @CustomCacheable(cacheNames = "userOrgUnits", key = "#userId")
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
//    @CustomCacheable(cacheNames = "userPermissions", key = "#userId")
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
//    private void processUserOrgRoleMapping(UserOrgRoleMappingEntity uorm,
//                                           Map<String, AlertTypeOrgPermissions> alertTypePermissions,
//                                           Map<String, List<ActionCondition>> modulePermissions,
//                                           Map<String, List<ActionCondition>> reportPermissions,
//                                           Set<String> uniqueAlertTypesOrgId,
//                                           Set<String> uniqueOrgId) {
//        String orgId = uorm.getOrgUnit().getOrgId().toString();
//        Integer roleId = uorm.getRole().getRoleId();
//
//        uniqueOrgId.add(orgId);
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
