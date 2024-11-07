package com.dair.cais.access.user;

import lombok.Data;

import java.util.List;

@Data
public class UserStatusUpdateRequest {
    private List<String> userIds;
    private boolean activate;
}