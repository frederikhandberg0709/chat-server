package com.frederikhandberg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// This is an old DTO and should be removed when appropriate.

@Data
public class ChatMessageRequestDTO {

    private Long receiverId;

    private Long groupChatId;

    @NotBlank
    private String content;
}
