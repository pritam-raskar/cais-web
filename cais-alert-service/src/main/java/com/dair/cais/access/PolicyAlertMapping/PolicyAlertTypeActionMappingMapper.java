package com.dair.cais.access.PolicyAlertMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.alertType.alertTypeEntity;
import com.dair.cais.access.policy.PolicyEntity;
import org.springframework.stereotype.Component;

@Component
public class PolicyAlertTypeActionMappingMapper {

    public PolicyAlertTypeActionMapping toModel(PolicyAlertTypeActionMappingEntity entity) {
        PolicyAlertTypeActionMapping model = new PolicyAlertTypeActionMapping();
        model.setPataId(entity.getPataId());
        model.setPolicyId(entity.getPolicy().getPolicyId());
        model.setAlertTypeId(entity.getAlertType().getAlertTypeId());
        model.setActionId(entity.getAction().getActionId());  // Changed this line
        model.setCondition(entity.getCondition());
        return model;
    }

    public PolicyAlertTypeActionMappingEntity toEntity(PolicyAlertTypeActionMapping model, PolicyEntity policy, alertTypeEntity alertType, ActionEntity action) {
        PolicyAlertTypeActionMappingEntity entity = new PolicyAlertTypeActionMappingEntity();
        entity.setPataId(model.getPataId());
        entity.setPolicy(policy);
        entity.setAlertType(alertType);
        entity.setAction(action);
        entity.setCondition(model.getCondition());
        return entity;
    }
}