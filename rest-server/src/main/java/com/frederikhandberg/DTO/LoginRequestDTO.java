package com.frederikhandberg.DTO;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String usernameOrEmail,
        @NotBlank String password) {
}
