package com.dair.cais.organization;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrgKeyPair {
    private String orgKey;
    private String parentOrgKey;

    public OrgKeyPair(String orgKey, String parentOrgKey) {
        this.orgKey = orgKey != null ? orgKey : "";
        this.parentOrgKey = parentOrgKey != null ? parentOrgKey : "";
    }
}