package com.frederikhandberg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {

    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
}
