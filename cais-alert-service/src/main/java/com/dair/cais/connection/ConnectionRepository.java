package com.dair.cais.connection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<ConnectionEntity, Long> {
    Optional<ConnectionEntity> findByConnectionName(String connectionName);
    boolean existsByConnectionName(String connectionName);
}