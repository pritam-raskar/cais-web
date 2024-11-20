package com.dair.cais.access.user;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserDetailDTO {
    private String userId;
    private String userLoginName;
    private String userFirstName;
    private String userMiddleName;
    private String userLastName;
    private Boolean userIsActive;
    private String email;

    public static UserDetailDTO fromEntity(UserEntity user) {
        return UserDetailDTO.builder()
                .userId(user.getUserId())
                .userLoginName(user.getUserLoginName())
                .userFirstName(user.getUserFirstName())
                .userMiddleName(user.getUserMiddleName())
                .userLastName(user.getUserLastName())
                .userIsActive(user.getUserIsActive())
                .email(user.getEmail())
                .build();
    }
}