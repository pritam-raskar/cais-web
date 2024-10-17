package com.dair.cais.access.user;

import lombok.Data;

@Data
public class LoginRequest {
    private String userLoginName;
    private String password;
}