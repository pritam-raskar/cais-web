package com.dair.cais.access.policy;

import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
public class PolicyInUseException extends RuntimeException {
    private final Map<Integer, List<String>> rolesByPolicy;

    public PolicyInUseException(String message, Map<Integer, List<String>> rolesByPolicy) {
        super(message);
        this.rolesByPolicy = rolesByPolicy;
    }
}