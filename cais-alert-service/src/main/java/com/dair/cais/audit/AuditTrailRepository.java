package com.dair.cais.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrailEntity, Long> {
    @Query("SELECT NEW com.dair.cais.audit.AuditTrailDetailsDTO(a, act.actionName, CONCAT(u.userFirstName, ' ', COALESCE(u.userMiddleName, ''), ' ', u.userLastName)) " +
            "FROM AuditTrailEntity a " +
            "LEFT JOIN com.dair.cais.access.Actions.ActionEntity act ON a.action.actionId = act.actionId " +
            "LEFT JOIN com.dair.cais.access.user.UserEntity u ON CAST(a.userId as string) = u.userId " +
            "WHERE a.userId = :userId AND a.actionTimestamp BETWEEN :startDate AND :endDate")
    List<AuditTrailDetailsDTO> findByUserIdAndActionTimestampBetween(
            @Param("userId") Long userId,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

    @Query("SELECT NEW com.dair.cais.audit.AuditTrailDetailsDTO(a, act.actionName, CONCAT(u.userFirstName, ' ', COALESCE(u.userMiddleName, ''), ' ', u.userLastName)) " +
            "FROM AuditTrailEntity a " +
            "LEFT JOIN com.dair.cais.access.Actions.ActionEntity act ON a.action.actionId = act.actionId " +
            "LEFT JOIN com.dair.cais.access.user.UserEntity u ON CAST(a.userId as string) = u.userId " +
            "WHERE a.affectedItemType = :itemType AND a.affectedItemId = :itemId")
    List<AuditTrailDetailsDTO> findByAffectedItemTypeAndAffectedItemId(
            @Param("itemType") String itemType,
            @Param("itemId") String itemId);

    @Query("SELECT NEW com.dair.cais.audit.AuditTrailDetailsDTO(a, act.actionName, CONCAT(u.userFirstName, ' ', COALESCE(u.userMiddleName, ''), ' ', u.userLastName)) " +
            "FROM AuditTrailEntity a " +
            "LEFT JOIN com.dair.cais.access.Actions.ActionEntity act ON a.action.actionId = act.actionId " +
            "LEFT JOIN com.dair.cais.access.user.UserEntity u ON CAST(a.userId as string) = u.userId " +
            "WHERE a.action.actionId = :actionId AND a.affectedItemId = :affectedItemId " +
            "ORDER BY a.actionTimestamp DESC")
    List<AuditTrailDetailsDTO> findAuditTrailStepHistory(
            @Param("affectedItemId") String affectedItemId,
            @Param("actionId") Integer actionId);
}


//package com.dair.cais.audit;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//
//@Repository
//public interface AuditTrailRepository extends JpaRepository<AuditTrailEntity, Long> {
//    List<AuditTrailEntity> findByUserIdAndActionTimestampBetween(Long userId, ZonedDateTime start, ZonedDateTime end);
//    List<AuditTrailEntity> findByAffectedItemTypeAndAffectedItemId(String affectedItemType, String affectedItemId);
//
//
//    @Query("SELECT a FROM AuditTrailEntity a WHERE a.action.actionId = :actionId AND a.affectedItemId = :affectedItemId ORDER BY a.actionTimestamp DESC")
//    List<AuditTrailEntity> findAuditTrailStepHistory(@Param("affectedItemId") String affectedItemId,
//                                                     @Param("actionId") Integer action);
//
//
//}