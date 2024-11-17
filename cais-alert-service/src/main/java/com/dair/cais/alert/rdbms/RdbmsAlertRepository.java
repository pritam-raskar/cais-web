package com.dair.cais.alert.rdbms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RdbmsAlertRepository extends JpaRepository<RdbmsAlertEntity, Long> {
    Optional<RdbmsAlertEntity> findByAlertId(String alertId);
    void deleteByAlertId(String alertId);

    @Modifying
    @Query("UPDATE RdbmsAlertEntity r SET r.ownerId = :ownerId, r.ownerName = :ownerName, r.lastUpdateDate = :lastUpdateDate WHERE r.alertId = :alertId")
    int updateOwnerIdAndName(@Param("alertId") String alertId,
                             @Param("ownerId") String ownerId,
                             @Param("ownerName") String ownerName,
                             @Param("lastUpdateDate") LocalDateTime lastUpdateDate);
}