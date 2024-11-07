package com.dair.cais.connection.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.InvalidJsonException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
class ConnectionMetadataService {

    private final ConnectionMetadataRepository repository;
    private final ConnectionMetadataMapper mapper;

    @Autowired
    public ConnectionMetadataService(ConnectionMetadataRepository repository, ConnectionMetadataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional()
    public List<ConnectionMetadata> getAllConnections() {
        log.debug("Retrieving all metadata connections");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional()
    public Optional<ConnectionMetadata> getConnection(String connectionType) {
        log.debug("Retrieving metadata connection for type: {}", connectionType);
        return repository.findById(connectionType)
                .map(mapper::toDto);
    }

    @Transactional
    public ConnectionMetadata createConnection(ConnectionMetadata connection) {
        log.info("Creating new metadata connection of type: {}", connection.getConnectionType());
        validateJsonStructure(connection.getJsonStructure());

        ConnectionMetadataEntity entity = mapper.toEntity(connection);
        ConnectionMetadataEntity savedEntity = repository.save(entity);
        log.debug("Successfully created metadata connection with type: {}", savedEntity.getConnectionType());

        return mapper.toDto(savedEntity);
    }

    @Transactional
    public ConnectionMetadata updateConnection(String connectionType, ConnectionMetadata connection) {
        log.info("Updating metadata connection of type: {}", connectionType);
        validateJsonStructure(connection.getJsonStructure());

        return repository.findById(connectionType)
                .map(existing -> {
                    existing.setJsonStructure(connection.getJsonStructure());
                    ConnectionMetadataEntity updated = repository.save(existing);
                    log.debug("Successfully updated metadata connection with type: {}", connectionType);
                    return mapper.toDto(updated);
                })
                .orElseThrow(() -> new EntityNotFoundException("Connection type not found: " + connectionType));
    }

    @Transactional
    public void deleteConnection(String connectionType) {
        log.info("Deleting metadata connection of type: {}", connectionType);
        repository.findById(connectionType)
                .ifPresentOrElse(
                        connection -> {
                            repository.delete(connection);
                            log.debug("Successfully deleted metadata connection with type: {}", connectionType);
                        },
                        () -> {
                            log.warn("Attempt to delete non-existent connection type: {}", connectionType);
                            throw new EntityNotFoundException("Connection type not found: " + connectionType);
                        }
                );
    }

    private void validateJsonStructure(String jsonStructure) {
        if (jsonStructure != null) {
            try {
                new ObjectMapper().readTree(jsonStructure);
            } catch (JsonProcessingException e) {
                log.error("Invalid JSON structure provided", e);
                throw new InvalidJsonException("Invalid JSON structure provided");
            }
        }
    }
}