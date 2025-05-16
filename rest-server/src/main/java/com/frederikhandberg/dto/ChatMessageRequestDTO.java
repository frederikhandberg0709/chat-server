package com.frederikhandberg.dto;

import com.frederikhandberg.types.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequestDTO {

    @NotNull
    private Long sender;

    private Long receiverId;

    private Long groupChatId;

    @NotBlank
    private String content;

    @NotNull
    private MessageType messageType;
}
