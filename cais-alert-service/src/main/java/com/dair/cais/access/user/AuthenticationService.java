package com.dair.cais.access.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Optional<LoginResponseDto> authenticate(String userLoginName, String password) {
        return userRepository.findByUserLoginName(userLoginName)
                .filter(user -> passwordEncoder.matches(password, user.getUserLoginPassword()))
                .map(user -> {
                    LoginResponseDto response = new LoginResponseDto();
                    response.setUserId(Integer.valueOf(user.getUserId()));
                    response.setToken(jwtService.generateToken(user.getUserId(), user.getUserLoginName()));
                    return response;
                });
    }

    public UserEntity registerUser(UserEntity user) {
        user.setUserLoginPassword(passwordEncoder.encode(user.getUserLoginPassword()));
        user.setUserIsActive(true);
        return userRepository.save(user);
    }
}