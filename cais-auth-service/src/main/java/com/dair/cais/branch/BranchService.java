package com.dair.cais.branch;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import com.dair.exception.CaisNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BranchService {

   @Autowired
   private BranchMapper branchMapper;
   @Autowired
   private BranchRepository branchRepository;

   public Branch createBranch(Branch branch) {
      BranchEntity upsertedBranch = branchRepository.createUpsertBranch(branchMapper.toEntity(branch));
      return branchMapper.toModel(upsertedBranch);
   }

   public Branch patchBranch(String alertId, Branch branch) {
      BranchEntity upsertedBranch = branchRepository.patchBranch(branchMapper.toEntity(alertId, branch));
      return branchMapper.toModel(upsertedBranch);
   }

   public Branch getBranchById(final String branchId) {
      BranchEntity branchById = branchRepository.getBranchById(branchId);
      if (branchById == null) {
         throw new CaisNotFoundException();
      }
      return branchMapper.toModel(branchById);
   }

   public Branch deleteBranchById(String branchId) {
      BranchEntity branchById = branchRepository.deleteBranchById(branchId);
      if (branchById == null) {
         throw new CaisNotFoundException();
      }
      return branchMapper.toModel(branchById);
   }

   public Map<String, Object> getAllBranchs(String name, Date createdDateFrom, Date createdDateTo, @Valid int limit,
         @Valid int offset) {
      validateRequestParams(name, createdDateFrom, createdDateTo, offset,
            limit);

      try {

         List<BranchEntity> allBranchEntities = branchRepository.getAllBranchs(name,
               createdDateFrom, createdDateTo, offset, limit);

         List<Branch> allBranchs = allBranchEntities.stream().map(a -> branchMapper.toModel(a))
               .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("branches", allBranchs);
         response.put("count", allBranchs.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving branches");
      }
   }

   public List<Branch> createBranchs(List<Branch> branches) {
      List<Branch> createdBranchs = branches.stream().map(a -> createBranch(a)).collect(Collectors.toList());
      return createdBranchs;
   }

   private void validateRequestParams(String name, Date createdDateFrom, Date createdDateTo, int offset, int limit) {
      StringBuilder errorMessage = new StringBuilder();

      if (name != null && !name.isEmpty()) {
         if (name.length() > 20) {
            errorMessage.append("name cannot be longer than 20 characters;");
         }
      }

      if (createdDateFrom != null && createdDateTo != null) {
         if (createdDateFrom.after(createdDateTo)) {
            errorMessage.append("from date cannot be after to date;");
         }
      }

      if (limit < 0) {
         errorMessage.append("limit cannot be negative;");
      }
      if (offset < 0) {
         errorMessage.append("offset cannot be negative;");
      }
      if (errorMessage.isEmpty()) {
         return;
      }

      throw new CaisIllegalArgumentException(errorMessage.toString());
   }

}
