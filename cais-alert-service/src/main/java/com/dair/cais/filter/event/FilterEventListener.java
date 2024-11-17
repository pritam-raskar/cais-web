package com.dair.cais.filter.event;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterEventListener {

    @Async
    @EventListener
    public void handleFilterAuditEvent(FilterAuditEvent event) {
        log.info("Filter Audit Event - Action: {}, User: {}, Filter ID: {}, Details: {}",
                event.getAction(),
                event.getUserId(),
                event.getFilter().getFilterId(),
                event.getDetails()
        );

        // Here you can add additional audit logging logic
        // For example, saving to an audit table or sending to an audit service
    }
}
