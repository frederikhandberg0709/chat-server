package com.frederikhandberg.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 1, max = 50) String firstName,
        @NotBlank @Size(min = 1, max = 50) String lastName,
        @NotBlank @Size(min = 6, max = 100) String password) {
}
