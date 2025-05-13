package com.frederikhandberg.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

        @NotBlank
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 1, max = 50)
        private String firstName;

        @NotBlank
        @Size(min = 1, max = 50)
        private String lastName;

        @NotBlank
        @Size(min = 6, max = 100)
        private String password;
}
