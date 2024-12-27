package com.dair.cais.access.user;

import com.dair.cais.access.user.dto.UserCreateRequest;
import com.dair.cais.access.user.dto.UserUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.BeanUtils;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO toDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public UserEntity createEntityFromRequest(UserCreateRequest request) {
        if (request == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setUserLoginName(request.getUserLoginName());
        entity.setUserLoginPassword(passwordEncoder.encode(request.getPassword()));
        entity.setUserFirstName(request.getUserFirstName());
        entity.setUserMiddleName(request.getUserMiddleName());
        entity.setUserLastName(request.getUserLastName());
        entity.setEmail(request.getEmail());
        entity.setUserIsActive(true);
        return entity;
    }

    public void updateEntityFromRequest(UserEntity entity, UserUpdateRequest request) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getUserFirstName() != null) {
            entity.setUserFirstName(request.getUserFirstName());
        }
        if (request.getUserMiddleName() != null) {
            entity.setUserMiddleName(request.getUserMiddleName());
        }
        if (request.getUserLastName() != null) {
            entity.setUserLastName(request.getUserLastName());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getUserIsActive() != null) {
            entity.setUserIsActive(request.getUserIsActive());
        }
    }
}