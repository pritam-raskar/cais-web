package com.dair.cais.access.Role;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoleUpdateEvent extends ApplicationEvent {
    private final Integer roleId;

    public RoleUpdateEvent(Object source, Integer roleId) {
        super(source);
        this.roleId = roleId;
    }
}
