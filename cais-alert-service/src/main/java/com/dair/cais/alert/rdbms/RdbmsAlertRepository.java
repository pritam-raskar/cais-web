package com.dair.cais.alert.rdbms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RdbmsAlertRepository extends JpaRepository<RdbmsAlertEntity, Long> {
    Optional<RdbmsAlertEntity> findByAlertId(String alertId);
    void deleteByAlertId(String alertId);
}