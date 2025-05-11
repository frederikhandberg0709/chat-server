package com.frederikhandberg.dto;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName) {
}
