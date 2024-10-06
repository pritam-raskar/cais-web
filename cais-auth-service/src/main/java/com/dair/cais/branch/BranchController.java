package com.dair.cais.branch;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Dair
 * @since 2022
 */

@RestController
@RequestMapping("/branches")
@Tag(name = "branches")

public class BranchController {

   @Autowired
   private BranchService branchService;

   @PostMapping
   @Operation(summary = "Create a branch")
   public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
      Branch createdBranch = branchService.createBranch(branch);
      return ResponseEntity.ok().body(createdBranch);
   }

   @PatchMapping("{branchId}")
   @Operation(summary = "Update a branch")
   public ResponseEntity<Branch> patchBranch(@PathVariable final String branchId, @RequestBody Branch branch) {
      Branch updatedBranch = branchService.patchBranch(branchId, branch);
      return ResponseEntity.ok().body(updatedBranch);
   }

   @PostMapping("/bulk")
   @Operation(summary = "Create bulk branches")
   public ResponseEntity<List<Branch>> createBranchs(@RequestBody List<Branch> branches) {
      List<Branch> createdBranchs = branchService.createBranchs(branches);
      return ResponseEntity.ok().body(createdBranchs);
   }

   @GetMapping("{branchId}")
   @Operation(summary = "Get a branch by its id")
   public ResponseEntity<Branch> getBranchById(@PathVariable final String branchId) {
      Branch branchById = branchService.getBranchById(branchId);
      return ResponseEntity.ok().body(branchById);
   }

   @DeleteMapping("{branchId}")
   @Operation(summary = "Delete a branch by its id")
   public ResponseEntity<Branch> deleteBranchById(@PathVariable final String branchId) {
      Branch branchById = branchService.deleteBranchById(branchId);
      return ResponseEntity.ok().body(branchById);
   }

   @GetMapping("")
   @Operation(summary = "Get all branches; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllBranchs(
         @RequestParam(required = false) String name,
         @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
         @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @Valid @RequestParam(defaultValue = "0") int offset) {
      return ResponseEntity.ok()
            .body(branchService.getAllBranchs(name,
                  createdDateFrom, createdDateTo, limit, offset));
   }
}