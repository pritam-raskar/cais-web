package com.dair.cais.audit;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.Actions.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditTrailService {

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private AuditTrailMapper auditTrailMapper;

    @Transactional
    public AuditTrail createAuditTrail(AuditTrail auditTrail) {
        AuditTrailEntity entity = auditTrailMapper.dtoToEntity(auditTrail);
        if (auditTrail.getActionId() != null) {
            ActionEntity action = actionRepository.findById(auditTrail.getActionId())
                    .orElseThrow(() -> new RuntimeException("Action not found"));
            entity.setAction(action);
        }
        entity = auditTrailRepository.save(entity);
        return auditTrailMapper.entityToDto(entity);
    }

    public List<AuditTrail> getAuditTrailByUser(Long userId, ZonedDateTime start, ZonedDateTime end) {
        List<AuditTrailDetailsDTO> results = auditTrailRepository.findByUserIdAndActionTimestampBetween(userId, start, end);
        return results.stream()
                .map(dto -> {
                    AuditTrail auditTrail = auditTrailMapper.entityToDto(dto.getAuditTrail());
                    auditTrail.setActionName(dto.getActionName());
                    auditTrail.setUserName(dto.getUserName());
                    return auditTrail;
                })
                .collect(Collectors.toList());
    }

    public List<AuditTrail> getAuditTrailByItem(String itemType, String itemId) {
        List<AuditTrailDetailsDTO> results = auditTrailRepository.findByAffectedItemTypeAndAffectedItemId(itemType, itemId);
        return results.stream()
                .map(dto -> {
                    AuditTrail auditTrail = auditTrailMapper.entityToDto(dto.getAuditTrail());
                    auditTrail.setActionName(dto.getActionName());
                    auditTrail.setUserName(dto.getUserName());
                    return auditTrail;
                })
                .collect(Collectors.toList());
    }

    public List<String> getAuditTrailStepHistory(String itemId, Integer actionId) {
        List<AuditTrailDetailsDTO> results = auditTrailRepository.findAuditTrailStepHistory(itemId, actionId);
        return results.stream()
                .map(dto -> dto.getAuditTrail().getNewValue())
                .collect(Collectors.toList());
    }

    @Transactional
    public AuditTrail logAction(Long userId, String userRole, Integer actionId, String description,
                                String category, String affectedItemType, String affectedItemId,
                                String oldValue, String newValue) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setUserId(userId);
        auditTrail.setUserRole(userRole);
        auditTrail.setActionId(actionId);
        auditTrail.setActionTimestamp(ZonedDateTime.now());
        auditTrail.setDescription(description);
        auditTrail.setCategory(category);
        auditTrail.setAffectedItemType(affectedItemType);
        auditTrail.setAffectedItemId(affectedItemId);
        auditTrail.setOldValue(oldValue);
        auditTrail.setNewValue(newValue);

        return createAuditTrail(auditTrail);
    }
}