package com.dair.cais.access.PolicyEntityMapping;

import com.dair.cais.access.policy.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyEntityMappingRepository extends JpaRepository<PolicyEntityMappingEntity, Integer> {

    //List<PolicyEntityMappingEntity> findByPolicyPolicyId(Integer policyId);
    @Query("SELECT DISTINCT pem FROM PolicyEntityMappingEntity pem " +
            "LEFT JOIN FETCH pem.policy p " +
            "LEFT JOIN FETCH pem.action a " +
            "WHERE pem.policy.policyId = :policyId")
    List<PolicyEntityMappingEntity> findByPolicyPolicyId(@Param("policyId") Integer policyId);

    List<PolicyEntityMappingEntity> findByEntityType(String entityType);

    List<PolicyEntityMappingEntity> findByEntityTypeAndEntityId(String entityType, String entityId);

    Optional<PolicyEntityMappingEntity> findByPolicyPolicyIdAndEntityTypeAndEntityIdAndActionActionId(
            Integer policyId, String entityType, String entityId, Integer actionId);

    List<PolicyEntityMappingEntity> findByPolicy(PolicyEntity policy);

    List<PolicyEntityMappingEntity> findByPolicyIn(List<PolicyEntity> policies);

    @Query("SELECT DISTINCT pem.entityType FROM PolicyEntityMappingEntity pem")
    List<String> findDistinctEntityTypes();

    void deleteByPolicyPolicyId(Integer policyId);

    void deleteByPolicyPolicyIdAndEntityType(Integer policyId, String entityType);

    @Modifying
    @Query("DELETE FROM PolicyEntityMappingEntity pem WHERE " +
            "pem.policy.policyId = :policyId AND " +
            "pem.entityType = :entityType AND " +
            "pem.entityId = :entityId AND " +
            "pem.action.actionId = :actionId")
    int deleteByPolicyIdAndEntityTypeAndEntityIdAndActionId(
            @Param("policyId") Integer policyId,
            @Param("entityType") String entityType,
            @Param("entityId") String entityId,
            @Param("actionId") Integer actionId
    );
}