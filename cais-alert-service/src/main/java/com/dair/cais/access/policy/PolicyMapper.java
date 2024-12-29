package com.dair.cais.access.policy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PolicyMapper {

    public Policy toModel(PolicyEntity entity) {
        if (entity == null) {
            return null;
        }

        Policy policy = new Policy();
        policy.setPolicyId(entity.getPolicyId());
        policy.setName(entity.getName());
        policy.setDescription(entity.getDescription());
        policy.setType(entity.getType());
        policy.setIsActive(entity.getIsActive());

        return policy;
    }

    public PolicyEntity toEntity(Policy model) {
        if (model == null) {
            return null;
        }

        PolicyEntity entity = new PolicyEntity();
        entity.setPolicyId(model.getPolicyId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setType(model.getType());
        entity.setIsActive(model.getIsActive());

        return entity;
    }

    public Policy toDto(PolicyEntity entity, List<AssociatedRole> roleDetails) {
        if (entity == null) {
            return null;
        }

        Policy dto = new Policy();
        dto.setPolicyId(entity.getPolicyId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setIsActive(entity.getIsActive());
        dto.setType(entity.getType());

        if (roleDetails != null && !roleDetails.isEmpty()) {
            dto.setAssociatedRoles(roleDetails.stream()
                    .map(role -> {
                        AssociatedRole associatedRole = new AssociatedRole();
                        associatedRole.setRoleId(role.getRoleId());
                        associatedRole.setRoleName(role.getRoleName());
                        return associatedRole;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
