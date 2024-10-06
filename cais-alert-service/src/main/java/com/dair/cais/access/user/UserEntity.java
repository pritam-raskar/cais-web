package com.dair.cais.access.user;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "cm_users", schema = "info_alert")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "info_alert.cm_users_user__id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_login_name")
    private String userLoginName;

    @Column(name = "user_login_password")
    private String userLoginPassword;

    @Column(name = "user_first_name")
    private String userFirstName;

    @Column(name = "user_middle_name")
    private String userMiddleName;

    @Column(name = "user_last_name")
    private String userLastName;

    @Column(name = "user_is_active")
    private Boolean userIsActive;

    @Column(name = "email")
    private String email;

    // Getters and setters
}