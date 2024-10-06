package com.dair.cais.member;

import java.util.Date;
import java.util.List;

import com.dair.model.CaisBaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Member extends CaisBaseModel {

    private String username;
    private String email;
    private boolean active;
    private List<String> roles;

}
