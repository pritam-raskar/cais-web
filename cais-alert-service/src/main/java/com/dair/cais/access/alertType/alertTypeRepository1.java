package com.dair.cais.access.alertType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface alertTypeRepository1 extends JpaRepository<alertTypeEntity, Integer> {
    Optional<alertTypeEntity> findByAlertTypeId(String alertTypeId);

}