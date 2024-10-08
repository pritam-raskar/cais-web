package com.dair.cais.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrailEntity, Long> {
    List<AuditTrailEntity> findByUserIdAndActionTimestampBetween(Long userId, ZonedDateTime start, ZonedDateTime end);
    List<AuditTrailEntity> findByAffectedItemTypeAndAffectedItemId(String itemType, String itemId);
}