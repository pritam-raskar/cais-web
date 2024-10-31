package com.dair.cais.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrailEntity, Long> {
    List<AuditTrailEntity> findByUserIdAndActionTimestampBetween(Long userId, ZonedDateTime start, ZonedDateTime end);
    List<AuditTrailEntity> findByAffectedItemTypeAndAffectedItemId(String affectedItemType, String affectedItemId);


    @Query("SELECT a FROM AuditTrailEntity a WHERE a.action.actionId = :actionId AND a.affectedItemId = :affectedItemId ORDER BY a.actionTimestamp DESC")
    List<AuditTrailEntity> findAuditTrailStepHistory(@Param("affectedItemId") String affectedItemId,
                                                     @Param("actionId") Integer action);


}