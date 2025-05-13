package com.frederikhandberg.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

        @NotBlank
        private String usernameOrEmail;

        @NotBlank
        private String password;
}
