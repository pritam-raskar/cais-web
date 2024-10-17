package com.dair.cais.access.Actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionRepository actionRepository;

    @Transactional(readOnly = true)
    public List<ActionEntity> getActionsByActionType(String actionType) {
        log.debug("Fetching actions for action type: {}", actionType);
        List<ActionEntity> actions = actionRepository.findByActionType(actionType);
        log.info("Found {} actions for action type: {}", actions.size(), actionType);
        return actions;
    }

    @Transactional(readOnly = true)
    public List<ActionEntity> getActionsByActionCategory(String actionCategory) {
        log.debug("Fetching actions for action category: {}", actionCategory);
        List<ActionEntity> actions = actionRepository.findByActionCategory(actionCategory);
        log.info("Found {} actions for action category: {}", actions.size(), actionCategory);
        return actions;
    }

    @Transactional(readOnly = true)
    public List<ActionEntity> getAllActions() {
        log.debug("Fetching all actions");
        List<ActionEntity> actions = actionRepository.findAll();
        log.info("Found {} actions in total", actions.size());
        return actions;
    }
}
