package com.dair.cais.branch;

import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public Branch toModel(BranchEntity entity) {
        Branch branch = new Branch();
        branch.setId(String.valueOf(entity.getId()));
        branch.setName(entity.getName());
        branch.setDescription(entity.getDescription());

        branch.setCreatedDate(entity.getCreatedDate());
        branch.setUpdatedDate(entity.getUpdatedDate());

        return branch;
    }

    public BranchEntity toEntity(Branch branch) {
        BranchEntity branchEntity = new BranchEntity();
        branchEntity.setId(branch.getId());
        mapBranchToEntity(branch, branchEntity);

        return branchEntity;
    }

    public BranchEntity toEntity(String branchId, Branch branch) {
        BranchEntity branchEntity = new BranchEntity();
        branchEntity.setId(branchId);
        mapBranchToEntity(branch, branchEntity);

        return branchEntity;

    }

    private void mapBranchToEntity(Branch branch, BranchEntity entity) {
        entity.setName(branch.getName());
        entity.setDescription(branch.getDescription());

        entity.setCreatedDate(branch.getCreatedDate());
        entity.setUpdatedDate(branch.getUpdatedDate());
    }

}