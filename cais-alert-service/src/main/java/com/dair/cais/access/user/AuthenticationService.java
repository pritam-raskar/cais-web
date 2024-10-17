package com.dair.cais.access.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<String> authenticate(String userLoginName, String password) {
        return userRepository.findByUserLoginName(userLoginName)
                .filter(user -> passwordEncoder.matches(password, user.getUserLoginPassword()))
                .map(UserEntity::getUserId);
    }

    public UserEntity registerUser(UserEntity user) {
        user.setUserLoginPassword(passwordEncoder.encode(user.getUserLoginPassword()));
        user.setUserIsActive(true); // Set the user as active upon registration
        return userRepository.save(user);
    }
}