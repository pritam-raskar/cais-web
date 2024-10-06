package com.dair.cais.access.RoleBasedPermission;

import com.dair.cais.common.config.CaisAlertConstants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = CaisAlertConstants.ROLE_PERMISSION_COLLECTION)
public class RoleBasedPermissionDocument {
    @Id
    private String id;
    private RolePolicyDocument rolePolicyDocument;
}