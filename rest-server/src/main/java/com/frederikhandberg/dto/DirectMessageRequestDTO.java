package com.frederikhandberg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DirectMessageRequestDTO {
    @NotNull
    private Long recipientId;

    @NotBlank
    private String content;
}
