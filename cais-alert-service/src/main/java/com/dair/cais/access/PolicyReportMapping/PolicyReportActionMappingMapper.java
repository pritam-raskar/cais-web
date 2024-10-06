package com.dair.cais.access.PolicyReportMapping;

import org.springframework.stereotype.Component;

@Component
public class PolicyReportActionMappingMapper {

    public PolicyReportActionMapping toDto(PolicyReportActionMappingEntity entity) {
        if (entity == null) {
            return null;
        }

        PolicyReportActionMapping dto = new PolicyReportActionMapping();
        dto.setPraId(entity.getPraId());
        
        if (entity.getPolicy() != null) {
            dto.setPolicyId(entity.getPolicy().getPolicyId());
            dto.setPolicyName(entity.getPolicy().getName());
        }
        
        if (entity.getReport() != null) {
            dto.setReportId(entity.getReport().getReportId());
            dto.setReportName(entity.getReport().getReportName());
        }
        
        if (entity.getAction() != null) {
            dto.setActionId(entity.getAction().getActionId());
            dto.setActionName(entity.getAction().getActionName());
        }
        
        dto.setCondition(entity.getCondition());
        
        return dto;
    }

    public PolicyReportActionMappingEntity toEntity(PolicyReportActionMapping dto) {
        if (dto == null) {
            return null;
        }

        PolicyReportActionMappingEntity entity = new PolicyReportActionMappingEntity();
        entity.setPraId(dto.getPraId());
        entity.setCondition(dto.getCondition());
        
        // Note: Policy, Report, and Action entities should be set in the service layer
        
        return entity;
    }
}
