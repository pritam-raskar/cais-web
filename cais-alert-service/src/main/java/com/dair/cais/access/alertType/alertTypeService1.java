package com.dair.cais.access.alertType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class alertTypeService1 {

    @Autowired
    private alertTypeRepository1 alertTypeRepository;

    @Autowired
    private alertTypeMapper1 alertTypeMapper;

    public List<alertType> getAllAlertTypes() {
        List<alertTypeEntity> alertTypeEntities = alertTypeRepository.findAll();
        return alertTypeEntities.stream()
                .map(alertTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    public alertType getAlertTypeById(String alertTypeId) {
        Optional<alertTypeEntity> alertTypeEntity = alertTypeRepository.findByAlertTypeId(alertTypeId);
        return alertTypeEntity.map(alertTypeMapper::toDto)
                .orElseThrow(() -> new RuntimeException("AlertType not found with id: " + alertTypeId));
    }

    // Create a new Alert Type
    public alertType createAlertType(alertType alertType) {
        alertTypeEntity alertTypeEntity = alertTypeMapper.toEntity(alertType);
        alertTypeEntity savedEntity = alertTypeRepository.save(alertTypeEntity);
        return alertTypeMapper.toDto(savedEntity);
    }

    // Delete an Alert Type by ID
    public void deleteAlertType(String alertTypeId) {
        Optional<alertTypeEntity> alertTypeEntity = alertTypeRepository.findByAlertTypeId(alertTypeId);
        if (alertTypeEntity.isPresent()) {
            alertTypeRepository.delete(alertTypeEntity.get());
        } else {
            throw new RuntimeException("AlertType not found with id: " + alertTypeId);
        }
    }

    // Update an existing alert type by ID
    public alertType updateAlertType(String alertTypeId, alertType updatedAlertType) {
        Optional<alertTypeEntity> existingAlertTypeEntity = alertTypeRepository.findByAlertTypeId(alertTypeId);
        if (existingAlertTypeEntity.isPresent()) {
            alertTypeEntity alertTypeEntity = existingAlertTypeEntity.get();
            // Update the fields
            alertTypeEntity.setTypeName(updatedAlertType.getTypeName());
            alertTypeEntity.setTypeSlug(updatedAlertType.getTypeSlug());
            alertTypeEntity.setAlertTypeId(updatedAlertType.getAlertTypeId());
            alertTypeEntity.setDescription(updatedAlertType.getDescription());

            // Save the updated entity
            alertTypeEntity savedEntity = alertTypeRepository.save(alertTypeEntity);
            return alertTypeMapper.toDto(savedEntity);
        } else {
            throw new RuntimeException("AlertType not found with id: " + alertTypeId);
        }
    }
}
