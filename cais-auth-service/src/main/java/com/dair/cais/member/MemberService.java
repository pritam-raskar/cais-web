package com.dair.cais.member;

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
public class MemberService {

   @Autowired
   private MemberMapper memberMapper;
   @Autowired
   private MemberRepository memberRepository;

   public Member createMember(Member member) {
      MemberEntity upsertedMember = memberRepository.createUpsertMember(memberMapper.toEntity(member));
      return memberMapper.toModel(upsertedMember);
   }

   public Member patchMember(String alertId, Member member) {
      MemberEntity upsertedMember = memberRepository.patchMember(memberMapper.toEntity(alertId, member));
      return memberMapper.toModel(upsertedMember);
   }

   public Member getMemberById(final String memberId) {
      MemberEntity memberById = memberRepository.getMemberById(memberId);
      if (memberById == null) {
         throw new CaisNotFoundException();
      }
      return memberMapper.toModel(memberById);
   }

   public Member deleteMemberById(String memberId) {
      MemberEntity memberById = memberRepository.deleteMemberById(memberId);
      if (memberById == null) {
         throw new CaisNotFoundException();
      }
      return memberMapper.toModel(memberById);
   }

   public Map<String, Object> getAllMembers(String name, @Valid int limit,
         @Valid int offset) {
      validateRequestParams(name, offset,
            limit);

      try {

         List<MemberEntity> allMemberEntities = memberRepository.getAllMembers(name, offset, limit);

         List<Member> allMembers = allMemberEntities.stream().map(a -> memberMapper.toModel(a))
               .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("members", allMembers);
         response.put("count", allMembers.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving members");
      }
   }

   public List<Member> createMembers(List<Member> members) {
      List<Member> createdMembers = members.stream().map(a -> createMember(a)).collect(Collectors.toList());
      return createdMembers;
   }

   private void validateRequestParams(String name, int offset, int limit) {
      StringBuilder errorMessage = new StringBuilder();

      if (name != null && !name.isEmpty()) {
         if (name.length() > 20) {
            errorMessage.append("name cannot be longer than 20 characters;");
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
