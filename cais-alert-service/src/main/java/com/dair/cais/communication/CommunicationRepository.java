package com.dair.cais.communication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommunicationRepository extends JpaRepository<CommunicationEntity, Long> {
    List<CommunicationEntity> findByAlertId(String alertId);
}