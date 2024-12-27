package com.dair.cais.access.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByUserLoginName(String userLoginName);

    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.userLoginName = :loginName")
    boolean existsByUserLoginName(@Param("loginName") String loginName);

    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u WHERE u.userLoginName = :loginName AND u.userIsActive = true")
    Optional<UserEntity> findActiveUserByLoginName(@Param("loginName") String loginName);

    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.email = :email AND u.userId != :userId")
    boolean existsByEmailAndUserIdNot(@Param("email") String email, @Param("userId") String userId);
}