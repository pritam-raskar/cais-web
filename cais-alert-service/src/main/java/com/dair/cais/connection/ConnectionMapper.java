package com.dair.cais.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ConnectionMapper {

    public Connection toModel(ConnectionEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Connection model = new Connection();
            model.setConnectionId(entity.getConnectionId());
            model.setConnectionName(entity.getConnectionName());
            model.setConnectionType(entity.getConnectionType());
            model.setIv(entity.getIv());
            model.setEncryptedData(entity.getEncryptedData());
            model.setCreatedAt(entity.getCreatedAt());
            model.setUpdatedAt(entity.getUpdatedAt());
            model.setCreatedBy(entity.getCreatedBy());
            model.setUpdatedBy(entity.getUpdatedBy());

            return model;
        } catch (Exception e) {
            log.error("Error mapping ConnectionEntity to Connection model: {}", e.getMessage(), e);
            throw new RuntimeException("Error mapping connection data", e);
        }
    }

    public ConnectionEntity toEntity(Connection model) {
        if (model == null) {
            return null;
        }

        try {
            ConnectionEntity entity = new ConnectionEntity();
            entity.setConnectionId(model.getConnectionId());
            entity.setConnectionName(model.getConnectionName());
            entity.setConnectionType(model.getConnectionType());
            entity.setIv(model.getIv());
            entity.setEncryptedData(model.getEncryptedData());
            entity.setCreatedBy(model.getCreatedBy());
            entity.setUpdatedBy(model.getUpdatedBy());

            return entity;
        } catch (Exception e) {
            log.error("Error mapping Connection model to ConnectionEntity: {}", e.getMessage(), e);
            throw new RuntimeException("Error mapping connection data", e);
        }
    }

    public void updateEntity(ConnectionEntity entity, Connection model) {
        if (entity == null || model == null) {
            return;
        }

        try {
            if (model.getConnectionName() != null) {
                entity.setConnectionName(model.getConnectionName());
            }
            if (model.getConnectionType() != null) {
                entity.setConnectionType(model.getConnectionType());
            }
            if (model.getIv() != null) {
                entity.setIv(model.getIv());
            }
            if (model.getEncryptedData() != null) {
                entity.setEncryptedData(model.getEncryptedData());
            }
            if (model.getUpdatedBy() != null) {
                entity.setUpdatedBy(model.getUpdatedBy());
            }
        } catch (Exception e) {
            log.error("Error updating ConnectionEntity from Connection model: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating connection data", e);
        }
    }

    public List<Connection> toModelList(List<ConnectionEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        try {
            return entities.stream()
                    .filter(Objects::nonNull)
                    .map(this::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error mapping list of ConnectionEntity to Connection models: {}", e.getMessage(), e);
            throw new RuntimeException("Error mapping connection data list", e);
        }
    }
}