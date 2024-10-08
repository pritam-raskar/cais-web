package com.dair.cais.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationFamily {
    private String orgKey;
    private String parentOrgKey;
    private String orgFamily;
}