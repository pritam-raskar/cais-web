// File: com/dair/cais/filter/event/FilterAuditEvent.java
package com.dair.cais.filter.event;

import com.dair.cais.filter.domain.UserSavedFilter;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FilterAuditEvent extends ApplicationEvent {
    private final String action;
    private final UserSavedFilter filter;
    private final String userId;
    private final String details;

    public FilterAuditEvent(Object source, String action, UserSavedFilter filter, String userId, String details) {
        super(source);
        this.action = action;
        this.filter = filter;
        this.userId = userId;
        this.details = details;
    }
}



