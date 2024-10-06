package com.dair.cais.member;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/members")
@Tag(name = "members")

public class MemberController {

   @Autowired
   private MemberService memberService;

   @PostMapping
   @Operation(summary = "Create a member")
   public ResponseEntity<Member> createMember(@RequestBody Member member) {
      Member createdMember = memberService.createMember(member);
      return ResponseEntity.ok().body(createdMember);
   }

   @PatchMapping("{memberId}")
   @Operation(summary = "Update a member")
   public ResponseEntity<Member> patchMember(@PathVariable final String memberId, @RequestBody Member member) {
      Member updatedMember = memberService.patchMember(memberId, member);
      return ResponseEntity.ok().body(updatedMember);
   }

   @PostMapping("/bulk")
   @Operation(summary = "Create bulk members")
   public ResponseEntity<List<Member>> createMembers(@RequestBody List<Member> members) {
      List<Member> createdMembers = memberService.createMembers(members);
      return ResponseEntity.ok().body(createdMembers);
   }

   @GetMapping("{memberId}")
   @Operation(summary = "Get a member by its id")
   public ResponseEntity<Member> getMemberById(@PathVariable final String memberId) {
      Member memberById = memberService.getMemberById(memberId);
      return ResponseEntity.ok().body(memberById);
   }

   @DeleteMapping("{memberId}")
   @Operation(summary = "Delete a member by its id")
   public ResponseEntity<Member> deleteMemberById(@PathVariable final String memberId) {
      Member memberById = memberService.deleteMemberById(memberId);
      return ResponseEntity.ok().body(memberById);
   }

   @GetMapping("")
   @Operation(summary = "Get all members; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllMembers(
         @RequestParam(required = false) String name,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @Valid @RequestParam(defaultValue = "0") int offset) {
      return ResponseEntity.ok()
            .body(memberService.getAllMembers(name, limit, offset));
   }
}