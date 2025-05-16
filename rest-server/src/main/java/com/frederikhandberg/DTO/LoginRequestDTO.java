package com.frederikhandberg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

        @NotBlank
        private String usernameOrEmail;

        @NotBlank
        private String password;
}
