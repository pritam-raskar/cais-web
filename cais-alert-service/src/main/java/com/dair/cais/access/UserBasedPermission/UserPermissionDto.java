package com.dair.cais.access.UserBasedPermission;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "UserBasedPermission")
public class UserPermissionDto {
    @Id
    private String userId;
    private UserInfo user;
    private PermissionWrapper permission;
    private Metadata metadata;
}

