package com.frederikhandberg.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.frederikhandberg.dto.AuthResponseDTO;
import com.frederikhandberg.dto.LoginRequestDTO;
import com.frederikhandberg.dto.RegisterRequestDTO;
import com.frederikhandberg.dto.ResetPasswordRequestDTO;
import com.frederikhandberg.dto.UserResponseDTO;
import com.frederikhandberg.exception.InvalidCredentialsException;
import com.frederikhandberg.exception.UserAlreadyExistsException;
import com.frederikhandberg.model.User;
import com.frederikhandberg.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO create(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        if (request.getPassword().length() < 8) {
            throw new InvalidCredentialsException("Password must be at least 8 characters long");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponseDTO(savedUser))
                .build();
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsernameOrEmail(
                request.getUsernameOrEmail(),
                request.getUsernameOrEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid login credentials provided."));

        boolean isCorrectPassword = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword());

        if (!isCorrectPassword) {
            throw new InvalidCredentialsException("Invalid login credentials provided.");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponseDTO(user))
                .build();
    }

    public AuthResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new InvalidCredentialsException("User does not exist."));

        if (request.getNewPassword().length() < 8) {
            throw new InvalidCredentialsException("Password must be at least 8 characters long");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordLastChanged(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponseDTO(savedUser))
                .build();
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName());
    }
}
