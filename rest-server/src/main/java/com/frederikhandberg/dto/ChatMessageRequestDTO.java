package com.frederikhandberg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageRequestDTO {

    private Long receiverId;

    private Long groupChatId;

    @NotBlank
    private String content;
}
