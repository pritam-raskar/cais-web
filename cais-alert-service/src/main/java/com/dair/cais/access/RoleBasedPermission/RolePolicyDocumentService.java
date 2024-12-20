package com.dair.cais.access.RoleBasedPermission;

import com.dair.cais.access.PolicyEntityMapping.PolicyEntityMappingEntity;
import com.dair.cais.access.PolicyEntityMapping.PolicyEntityMappingRepository;
import com.dair.cais.access.Role.RoleEntity;
import com.dair.cais.access.Role.RoleRepository;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingEntity;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingRepository;
import com.dair.cais.access.entity.SystemEntityService;
import com.dair.cais.access.modules.ModuleEntity;
import com.dair.cais.access.modules.ModuleRepository;
import com.dair.cais.common.config.CaisAlertConstants;
import com.dair.cais.reports.ReportsEntity;
import com.dair.cais.reports.repository.ReportsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePolicyDocumentService {

    private final RoleRepository roleRepository;
    private final RolesPolicyMappingRepository rolesPolicyMappingRepository;
    private final PolicyEntityMappingRepository policyEntityMappingRepository;
    private final SystemEntityService systemEntityService;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final ModuleRepository moduleRepository;
    private final ReportsRepository reportRepository;


    @Transactional()
    public ObjectNode generateStructuredDataForRole(Integer roleId) {
        log.info("Generating structured data for role with ID: {}", roleId);
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("roleId", roleId);
        rootNode.put("role", role.getRoleName());

        ObjectNode alertTypesNode = rootNode.putObject("alert-types");
        ObjectNode modulesNode = rootNode.putObject("modules");
        ObjectNode reportsNode = rootNode.putObject("reports");

        List<RolesPolicyMappingEntity> rolePolicyMappings =
                rolesPolicyMappingRepository.findByRoleRoleId(roleId);

        for (RolesPolicyMappingEntity rpm : rolePolicyMappings) {
            List<PolicyEntityMappingEntity> policyEntityMappings =
                    policyEntityMappingRepository.findByPolicyPolicyId(rpm.getPolicy().getPolicyId());

            Map<String, List<PolicyEntityMappingEntity>> mappingsByEntityType = policyEntityMappings.stream()
                    .collect(Collectors.groupingBy(PolicyEntityMappingEntity::getEntityType));

            processEntityMappings(mappingsByEntityType.get("alert-types"), alertTypesNode);
            processEntityMappings(mappingsByEntityType.get("modules"), modulesNode);
            processEntityMappings(mappingsByEntityType.get("reports"), reportsNode);
        }

        return rootNode;
    }

//    @Transactional(readOnly = true)
//    public ObjectNode generateStructuredDataForRole(Integer roleId) {
//        log.info("Generating structured data for role with ID: {}", roleId);
//
//        // Get role
//        RoleEntity role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
//
//        // Initialize root node
//        ObjectNode rootNode = objectMapper.createObjectNode();
//        rootNode.put("roleId", roleId);
//        rootNode.put("role", role.getRoleName());
//
//        // Get all policies mapped to this role
//        List<RolesPolicyMappingEntity> rolePolicyMappings = rolesPolicyMappingRepository.findByRoleRoleIdWithPolicyAndRole(roleId);
//
//        // Create nodes for each entity type
//        ObjectNode alertTypesNode = rootNode.putObject("alert-types");
//        ObjectNode modulesNode = rootNode.putObject("modules");
//        ObjectNode reportsNode = rootNode.putObject("reports");
//
//        // Process each policy's entity mappings
//        for (RolesPolicyMappingEntity rolePolicyMapping : rolePolicyMappings) {
//            PolicyEntity policy = rolePolicyMapping.getPolicy();
//            List<PolicyEntityMappingEntity> policyEntityMappings =
//                    policyEntityMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
//
//            // Group entity mappings by entity type
//            Map<String, List<PolicyEntityMappingEntity>> mappingsByEntityType = policyEntityMappings.stream()
//                    .collect(Collectors.groupingBy(PolicyEntityMappingEntity::getEntityType));
//
//            // Process alert types
//            processEntityMappings(mappingsByEntityType.get("alert-types"), alertTypesNode);
//
//            // Process modules
//            processEntityMappings(mappingsByEntityType.get("modules"), modulesNode);
//
//            // Process reports
//            processEntityMappings(mappingsByEntityType.get("reports"), reportsNode);
//        }
//
//        return rootNode;
//    }

//    private void processEntityMappings(List<PolicyEntityMappingEntity> mappings, ObjectNode entityNode) {
//        if (mappings == null || mappings.isEmpty()) {
//            return;
//        }
//
//        // Group mappings by entity ID
//        Map<String, List<PolicyEntityMappingEntity>> mappingsByEntityId = mappings.stream()
//                .collect(Collectors.groupingBy(PolicyEntityMappingEntity::getEntityId));
//
//        // Process each entity
//        mappingsByEntityId.forEach((entityId, entityMappings) -> {
//            ObjectNode entityActions = entityNode.has(entityId)
//                    ? (ObjectNode) entityNode.get(entityId)
//                    : entityNode.putObject(entityId);
//
//            if (!entityActions.has("actions")) {
//                entityActions.putArray("actions");
//            }
//
//            ArrayNode actionsArray = (ArrayNode) entityActions.get("actions");
//
//            // Add each action
//            for (PolicyEntityMappingEntity mapping : entityMappings) {
//                ObjectNode actionNode = actionsArray.addObject();
//                actionNode.put("action", mapping.getAction().getActionName());
//                actionNode.put("condition", mapping.getCondition() != null ? mapping.getCondition() : "");
//                actionNode.put("actionCategory", mapping.getAction().getActionCategory());
//                actionNode.put("actionType", mapping.getAction().getActionType());
//            }
//        });
//    }

    private void processEntityMappings(List<PolicyEntityMappingEntity> mappings, ObjectNode entityNode) {
        if (mappings == null || mappings.isEmpty()) {
            return;
        }

        // Group mappings by entity ID
        Map<String, List<PolicyEntityMappingEntity>> mappingsByEntityId = mappings.stream()
                .collect(Collectors.groupingBy(PolicyEntityMappingEntity::getEntityId));

        // Process each entity
        mappingsByEntityId.forEach((entityId, entityMappings) -> {
            PolicyEntityMappingEntity firstMapping = entityMappings.get(0);
            String displayKey = getDisplayKey(entityId, firstMapping.getEntityType());

            ObjectNode entityActions = entityNode.has(displayKey)
                    ? (ObjectNode) entityNode.get(displayKey)
                    : entityNode.putObject(displayKey);

            if (!entityActions.has("actions")) {
                entityActions.putArray("actions");
            }

            ArrayNode actionsArray = (ArrayNode) entityActions.get("actions");

            // Add each action
            for (PolicyEntityMappingEntity mapping : entityMappings) {
                ObjectNode actionNode = actionsArray.addObject();
                actionNode.put("action", mapping.getAction().getActionName());
                actionNode.put("condition", mapping.getCondition() != null ? mapping.getCondition() : "");
                actionNode.put("actionCategory", mapping.getAction().getActionCategory());
                actionNode.put("actionType", mapping.getAction().getActionType());
            }
        });
    }


    private String getDisplayKey(String entityId, String entityType) {
        String displayKey = entityId; // Default to ID if name cannot be found

        try {
            if ("modules".equals(entityType)) {
                Integer moduleId = Integer.parseInt(entityId);
                Optional<ModuleEntity> moduleOpt = moduleRepository.findById(moduleId);
                if (moduleOpt.isPresent()) {
                    displayKey = moduleOpt.get().getModuleName();
                    log.debug("Found module name: {} for id: {}", displayKey, moduleId);
                } else {
                    log.warn("Module not found for id: {}", moduleId);
                }
            } else if ("reports".equals(entityType)) {
                Integer reportId = Integer.parseInt(entityId);
                Optional<ReportsEntity> reportOpt = reportRepository.findByReportId(reportId);  // Updated entity type
                if (reportOpt.isPresent()) {
                    displayKey = reportOpt.get().getReportName();  // Using correct getter
                    log.debug("Found report name: {} for id: {}", displayKey, reportId);
                } else {
                    log.warn("Report not found for id: {}", reportOpt);
                }
            }
        } catch (Exception e) {
            log.error("Error converting ID to name for entityType: {}, entityId: {}",
                    entityType, entityId, e);
        }

        return displayKey;
    }
    private void logReportDetails(Integer reportId) {
        try {
            log.info("Attempting to fetch report with ID: {}", reportId);
            reportRepository.findById(reportId).ifPresentOrElse(
                    report -> log.info("Found report: ID={}, Name={}", report.getReportId(), report.getReportName()),
                    () -> log.warn("No report found with ID: {}", reportId)
            );
        } catch (Exception e) {
            log.error("Error fetching report details for ID: {}", reportId, e);
        }
    }


    public void saveRolePermissionToMongo(Integer roleId, ObjectNode rolePermissionData) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        String documentId = roleId + ":" + role.getRoleName();
        Query query = new Query(Criteria.where("_id").is(documentId));

        Map<String, Object> rolePermissionMap = objectMapper.convertValue(rolePermissionData, Map.class);
        rolePermissionMap.put("_id", documentId);

        mongoTemplate.save(rolePermissionMap, CaisAlertConstants.ROLE_PERMISSION_COLLECTION);
        log.info("Saved role permissions to MongoDB for role: {}", documentId);
    }

    public ObjectNode getRolePermissionFromMongo(Integer roleId) {
        log.info("Fetching role permissions from MongoDB for role ID: {}", roleId);
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        String documentId = roleId + ":" + role.getRoleName();
        Query query = new Query(Criteria.where("_id").is(documentId));

        Map<String, Object> rolePermissionMap = mongoTemplate.findOne(query, Map.class,
                CaisAlertConstants.ROLE_PERMISSION_COLLECTION);

        if (rolePermissionMap != null) {
            log.info("Successfully fetched role permissions from MongoDB for role: {}", documentId);
            return objectMapper.valueToTree(rolePermissionMap);
        } else {
            log.warn("No role permissions found in MongoDB for role: {}", documentId);
            return null;
        }
    }
}


//package com.dair.cais.access.RoleBasedPermission;
//
//import com.dair.cais.access.PolicyAlertMapping.PolicyAlertTypeActionMappingEntity;
//import com.dair.cais.access.PolicyAlertMapping.PolicyAlertTypeActionMappingRepository;
//import com.dair.cais.access.PolicyModuleMapping.PolicyModuleMappingEntity;
//import com.dair.cais.access.PolicyModuleMapping.PolicyModuleMappingRepository;
//import com.dair.cais.access.PolicyReportMapping.PolicyReportActionMappingEntity;
//import com.dair.cais.access.PolicyReportMapping.PolicyReportActionMappingRepository;
//import com.dair.cais.access.Role.RoleEntity;
//import com.dair.cais.access.Role.RoleRepository;
//import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingEntity;
//import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingRepository;
//import com.dair.cais.access.policy.PolicyEntity;
//import com.dair.cais.common.config.CaisAlertConstants;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class RolePolicyDocumentService {
//
//    private final RoleRepository roleRepository;
//    private final RolesPolicyMappingRepository rolesPolicyMappingRepository;
//    private final PolicyAlertTypeActionMappingRepository policyAlertTypeActionMappingRepository;
//    private final PolicyModuleMappingRepository policyModuleMappingRepository;
//    private final PolicyReportActionMappingRepository policyReportActionMappingRepository;
//    private final MongoTemplate mongoTemplate;
//    private final ObjectMapper objectMapper;
//    @Transactional(readOnly = true)
//    public ObjectNode generateStructuredDataForRole(Integer roleId) {
//        log.info("Generating structured data for role with ID: {}", roleId);
//        RoleEntity role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
//
//        ObjectNode rootNode = objectMapper.createObjectNode();
//        rootNode.put("roleId", roleId);
//        rootNode.put("role", role.getRoleName());
//
//        ObjectNode alertTypeNode = rootNode.putObject("alertType");
//        ObjectNode modulesNode = rootNode.putObject("modules");
//        ObjectNode reportsNode = rootNode.putObject("reports");
//
//        List<RolesPolicyMappingEntity> rolePolicyMappings = rolesPolicyMappingRepository.findByRoleRoleId(roleId);
//
//        for (RolesPolicyMappingEntity rpm : rolePolicyMappings) {
//            processAlertTypePermissions(rpm.getPolicy(), alertTypeNode);
//            processModulePermissions(rpm.getPolicy(), modulesNode);
//            processReportPermissions(rpm.getPolicy(), reportsNode);
//        }
//
//        log.info("Successfully generated structured data for role with ID: {}", roleId);
//        return rootNode;
//    }
//
//    private void processAlertTypePermissions(PolicyEntity policy, ObjectNode alertTypeNode) {
//        List<PolicyAlertTypeActionMappingEntity> alertMappings = policyAlertTypeActionMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
//
//        for (PolicyAlertTypeActionMappingEntity alertMapping : alertMappings) {
//            String alertTypeId = alertMapping.getAlertType().getAlertTypeId();
//            ObjectNode alertTypeActions = alertTypeNode.has(alertTypeId)
//                    ? (ObjectNode) alertTypeNode.get(alertTypeId)
//                    : alertTypeNode.putObject(alertTypeId);
//
//            if (!alertTypeActions.has("actions")) {
//                alertTypeActions.putArray("actions");
//            }
//
//            ObjectNode actionNode = alertTypeActions.withArray("actions").addObject();
//            actionNode.put("action", alertMapping.getAction().getActionName());
//            actionNode.put("condition", alertMapping.getCondition() != null ? alertMapping.getCondition() : "");
//        }
//    }
//
//    private void processModulePermissions(PolicyEntity policy, ObjectNode modulesNode) {
//        List<PolicyModuleMappingEntity> moduleMappings = policyModuleMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
//
//        for (PolicyModuleMappingEntity moduleMapping : moduleMappings) {
//            String moduleName = moduleMapping.getModule().getModuleName();
//            if (!modulesNode.has(moduleName)) {
//                modulesNode.putArray(moduleName);
//            }
//
//            ObjectNode actionNode = modulesNode.withArray(moduleName).addObject();
//            actionNode.put("action", moduleMapping.getAction().getActionName());
//            actionNode.put("condition", moduleMapping.getCondition() != null ? moduleMapping.getCondition() : "N/A");
//        }
//    }
//
//    private void processReportPermissions(PolicyEntity policy, ObjectNode reportsNode) {
//        List<PolicyReportActionMappingEntity> reportMappings = policyReportActionMappingRepository.findByPolicyPolicyId(policy.getPolicyId());
//
//        for (PolicyReportActionMappingEntity reportMapping : reportMappings) {
//            String reportName = reportMapping.getReport().getReportName();
//            if (!reportsNode.has(reportName)) {
//                reportsNode.putArray(reportName);
//            }
//
//            ObjectNode actionNode = reportsNode.withArray(reportName).addObject();
//            actionNode.put("action", reportMapping.getAction().getActionName());
//            actionNode.put("condition", reportMapping.getCondition() != null ? reportMapping.getCondition() : "N/A");
//        }
//    }
//
//    public void saveRolePermissionToMongo(Integer roleId, ObjectNode rolePermissionData) {
//        RoleEntity role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
//
//        String documentId = roleId + ":" + role.getRoleName();
//        Query query = new Query(Criteria.where("_id").is(documentId));
//
//        // Convert ObjectNode to Map for MongoDB storage
//        Map<String, Object> rolePermissionMap = objectMapper.convertValue(rolePermissionData, Map.class);
//        rolePermissionMap.put("_id", documentId);
//
//        mongoTemplate.save(rolePermissionMap, CaisAlertConstants.ROLE_PERMISSION_COLLECTION);
//        log.info("Saved role permissions to MongoDB for role: {}", documentId);
//    }
//
//    public ObjectNode getRolePermissionFromMongo(Integer roleId) {
//        log.info("Fetching role permissions from MongoDB for role ID: {}", roleId);
//        RoleEntity role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
//
//        String documentId = roleId + ":" + role.getRoleName();
//        Query query = new Query(Criteria.where("_id").is(documentId));
//
//        Map<String, Object> rolePermissionMap = mongoTemplate.findOne(query, Map.class,CaisAlertConstants.ROLE_PERMISSION_COLLECTION);
//
//        if (rolePermissionMap != null) {
//            log.info("Successfully fetched role permissions from MongoDB for role: {}", documentId);
//            return objectMapper.valueToTree(rolePermissionMap);
//        } else {
//            log.warn("No role permissions found in MongoDB for role: {}", documentId);
//            return null;
//        }
//    }
//
//
//
//}