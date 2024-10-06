package com.dair.cais.communication;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommunicationService {

    private final CommunicationRepository communicationRepository;
    private final CommunicationMapper communicationMapper;

    public CommunicationService(CommunicationRepository communicationRepository, CommunicationMapper communicationMapper) {
        this.communicationRepository = communicationRepository;
        this.communicationMapper = communicationMapper;
    }

    @Transactional
    public Communication createCommunication(Communication communication) {
        CommunicationEntity entity = communicationMapper.toEntity(communication);
        // Set createDate to current system date and time
        entity.setCreateDate(LocalDateTime.now());
        entity = communicationRepository.save(entity);
        return communicationMapper.toModel(entity);
    }

    @Transactional(readOnly = true)
    public List<Communication> getCommunicationsByAlertId(String alertId) {
        List<CommunicationEntity> entities = communicationRepository.findByAlertId(alertId);
        return entities.stream()
                .map(communicationMapper::toModel)
                .collect(Collectors.toList());
    }
}