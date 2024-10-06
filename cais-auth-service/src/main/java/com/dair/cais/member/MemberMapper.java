package com.dair.cais.member;

import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public Member toModel(MemberEntity entity) {
        Member member = new Member();
        member.setId(String.valueOf(entity.getId()));
        member.setName(entity.getName());
        member.setDescription(entity.getDescription());
        member.setUsername(entity.getUsername());
        member.setEmail(entity.getEmail());
        member.setActive(entity.isActive());
        member.setRoles(entity.getRoles());

        member.setCreatedDate(entity.getCreatedDate());
        member.setUpdatedDate(entity.getUpdatedDate());

        return member;
    }

    public MemberEntity toEntity(Member member) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(member.getId());
        mapMemberToEntity(member, memberEntity);

        return memberEntity;
    }

    public MemberEntity toEntity(String memberId, Member member) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberId);
        mapMemberToEntity(member, memberEntity);

        return memberEntity;

    }

    private void mapMemberToEntity(Member member, MemberEntity entity) {
        entity.setName(member.getName());
        entity.setDescription(member.getDescription());
        entity.setUsername(member.getUsername());
        entity.setEmail(member.getEmail());
        entity.setActive(member.isActive());
        entity.setRoles(member.getRoles());

        entity.setCreatedDate(member.getCreatedDate());
        entity.setUpdatedDate(member.getUpdatedDate());
    }

}