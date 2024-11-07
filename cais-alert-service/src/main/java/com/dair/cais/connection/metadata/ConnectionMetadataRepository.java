package com.dair.cais.connection.metadata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ConnectionMetadataRepository extends JpaRepository<ConnectionMetadataEntity, String> {
    // Custom queries if needed
}