package com.dair.cais.access.user;

import lombok.Data;

@Data
public class UserDTO {
    private String userId;
    private String userLoginName;
    private String userFirstName;
    private String userMiddleName;
    private String userLastName;
    private Boolean userIsActive;
    private String email;

    // Static factory method to create DTO from entity
    public static UserDTO fromEntity(UserEntity entity) {
        UserDTO dto = new UserDTO();
        dto.setUserId(entity.getUserId());
        dto.setUserLoginName(entity.getUserLoginName());
        dto.setUserFirstName(entity.getUserFirstName());
        dto.setUserMiddleName(entity.getUserMiddleName());
        dto.setUserLastName(entity.getUserLastName());
        dto.setUserIsActive(entity.getUserIsActive());
        dto.setEmail(entity.getEmail());
        return dto;
    }
}