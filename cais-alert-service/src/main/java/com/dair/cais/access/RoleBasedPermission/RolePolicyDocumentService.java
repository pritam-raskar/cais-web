package com.dair.cais.access.RoleBasedPermission;

import com.dair.cais.access.PolicyAlertMapping.PolicyAlertTypeActionMappingEntity;
import com.dair.cais.access.PolicyAlertMapping.PolicyAlertTypeActionMappingRepository;
import com.dair.cais.access.PolicyModuleMapping.PolicyModuleMappingEntity;
import com.dair.cais.access.PolicyModuleMapping.PolicyModuleMappingRepository;
import com.dair.cais.access.PolicyReportMapping.PolicyReportActionMappingEntity;
import com.dair.cais.access.PolicyReportMapping.PolicyReportActionMappingRepository;
import com.dair.cais.access.Role.RoleEntity;
import com.dair.cais.access.Role.RoleRepository;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingEntity;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingRepository;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.common.config.CaisAlertConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePolicyDocumentService {

    private final RoleRepository roleRepository;
    private final RolesPolicyMappingRepository rolesPolicyMappingRepository;
    private final PolicyAlertTypeActionMappingRepository policyAlertTypeActionMappingRepository;
    private final PolicyModuleMappingRepository policyModuleMappingRepository;
    private final PolicyReportActionMappingRepository policyReportActionMappingRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    @Transactional(readOnly = true)
    public ObjectNode generateStructuredDataForRole(Integer roleId) {
        log.info("Generating structured data for role with ID: {}", roleId);
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("roleId", roleId);
        rootNode.put("role", role.getRoleName());

        ObjectNode alertTypeNode = rootNode.putObject("alertType");
        ObjectNode modulesNode = rootNode.putObject("modules");
        ObjectNode reportsNode = rootNode.putObject("reports");

        List<RolesPolicyMappingEntity> rolePolicyMappings = rolesPolicyMappingRepository.findByRoleRoleId(roleId);

        for (RolesPolicyMappingEntity rpm : rolePolicyMappings) {
            processAlertTypePermissions(rpm.getPolicy(), alertTypeNode);
            processModulePermissions(rpm.getPolicy(), modulesNode);
            processReportPermissions(rpm.getPolicy(), reportsNode);
        }

        log.info("Successfully generated structured data for role with ID: {}", roleId);
        return rootNode;
    }

    private void processAlertTypePermissions(PolicyEntity policy, ObjectNode alertTypeNode) {
        List<PolicyAlertTypeActionMappingEntity> alertMappings = policyAlertTypeActionMappingRepository.findByPolicyPolicyId(policy.getPolicyId());

        for (PolicyAlertTypeActionMappingEntity alertMapping : alertMappings) {
            String alertTypeId = alertMapping.getAlertType().getAlertTypeId();
            ObjectNode alertTypeActions = alertTypeNode.has(alertTypeId)
                    ? (ObjectNode) alertTypeNode.get(alertTypeId)
                    : alertTypeNode.putObject(alertTypeId);

            if (!alertTypeActions.has("actions")) {
                alertTypeActions.putArray("actions");
            }

            ObjectNode actionNode = alertTypeActions.withArray("actions").addObject();
            actionNode.put("action", alertMapping.getAction().getActionName());
            actionNode.put("condition", alertMapping.getCondition() != null ? alertMapping.getCondition() : "");
        }
    }

    private void processModulePermissions(PolicyEntity policy, ObjectNode modulesNode) {
        List<PolicyModuleMappingEntity> moduleMappings = policyModuleMappingRepository.findByPolicyPolicyId(policy.getPolicyId());

        for (PolicyModuleMappingEntity moduleMapping : moduleMappings) {
            String moduleName = moduleMapping.getModule().getModuleName();
            if (!modulesNode.has(moduleName)) {
                modulesNode.putArray(moduleName);
            }

            ObjectNode actionNode = modulesNode.withArray(moduleName).addObject();
            actionNode.put("action", moduleMapping.getAction().getActionName());
            actionNode.put("condition", moduleMapping.getCondition() != null ? moduleMapping.getCondition() : "N/A");
        }
    }

    private void processReportPermissions(PolicyEntity policy, ObjectNode reportsNode) {
        List<PolicyReportActionMappingEntity> reportMappings = policyReportActionMappingRepository.findByPolicyPolicyId(policy.getPolicyId());

        for (PolicyReportActionMappingEntity reportMapping : reportMappings) {
            String reportName = reportMapping.getReport().getReportName();
            if (!reportsNode.has(reportName)) {
                reportsNode.putArray(reportName);
            }

            ObjectNode actionNode = reportsNode.withArray(reportName).addObject();
            actionNode.put("action", reportMapping.getAction().getActionName());
            actionNode.put("condition", reportMapping.getCondition() != null ? reportMapping.getCondition() : "N/A");
        }
    }

    public void saveRolePermissionToMongo(Integer roleId, ObjectNode rolePermissionData) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        String documentId = roleId + ":" + role.getRoleName();
        Query query = new Query(Criteria.where("_id").is(documentId));

        // Convert ObjectNode to Map for MongoDB storage
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

        Map<String, Object> rolePermissionMap = mongoTemplate.findOne(query, Map.class,CaisAlertConstants.ROLE_PERMISSION_COLLECTION);

        if (rolePermissionMap != null) {
            log.info("Successfully fetched role permissions from MongoDB for role: {}", documentId);
            return objectMapper.valueToTree(rolePermissionMap);
        } else {
            log.warn("No role permissions found in MongoDB for role: {}", documentId);
            return null;
        }
    }



}