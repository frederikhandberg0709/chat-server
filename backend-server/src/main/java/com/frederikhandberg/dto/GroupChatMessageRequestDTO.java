package com.frederikhandberg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupChatMessageRequestDTO {

    @NotNull(message = "Group chat ID is required")
    private Long groupChatId;

    @NotBlank(message = "Message content cannot be blank")
    private String content;
}
