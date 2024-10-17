package com.dair.cais.access.user;

import com.dair.cais.access.UserBasedPermission.UserPermissionService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserPermissionService userPermissionService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authenticationService.authenticate(loginRequest.getUserLoginName(), loginRequest.getPassword())
                .map(userId -> {
                    try {
                        ObjectNode permissions = userPermissionService.getUserPermissionFromMongo(userId);
                        LoginResponseDto response = new LoginResponseDto();
                        response.setUserId(Integer.valueOf(userId));
                        response.setPermissions(permissions);
                        return ResponseEntity.ok(response);
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().body("Error processing permissions: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.badRequest().body("Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity user) {
        UserEntity registeredUser = authenticationService.registerUser(user);
        return ResponseEntity.ok(registeredUser);
    }
}