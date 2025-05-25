package com.frederikhandberg.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectChatMessageRequestDTO {

    private Long receiverId;

    private Long directChatId;

    @NotBlank(message = "Message content cannot be blank")
    private String content;

    @AssertTrue(message = "Must specify either receiverId or directChatId, but not both")
    public boolean isValidRequest() {
        return (receiverId != null && directChatId == null) ||
                (receiverId == null && directChatId != null);
    }
}
