package com.dair.cais.access.UserBasedPermission;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class Metadata {
    private Set<String> uniqueAlertTypesOrgId;
    private Set<String> uniqueOrgId;
}
