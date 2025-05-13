package com.frederikhandberg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frederikhandberg.dto.AuthResponseDTO;
import com.frederikhandberg.dto.RegisterRequestDTO;
import com.frederikhandberg.dto.UserResponseDTO;
import com.frederikhandberg.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Creates a user account", description = "Registers a unique user record in the system corresponding to the provided information")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.create(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticates a user and initiates a session", description = "Processes login requests by validating the provided credentials")
    public ResponseEntity<UserResponseDTO> login(@RequestBody String entity) {
        return ResponseEntity.ok().build();
    }
}
