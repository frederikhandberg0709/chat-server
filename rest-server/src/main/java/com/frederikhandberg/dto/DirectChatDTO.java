package com.frederikhandberg.dto;

import java.time.LocalDateTime;

import com.frederikhandberg.model.DirectChat;
import com.frederikhandberg.model.User;

import lombok.Data;

@Data
public class DirectChatDTO {
    private Long id;
    private UserMinimalDTO user;
    private ChatMessageResponseDTO lastMessage;
    private Integer unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DirectChatDTO(DirectChat directChat, User currentUser) {
        this.id = directChat.getId();

        User otherUser = directChat.getOtherUser(currentUser);
        this.user = new UserMinimalDTO(otherUser);

        this.createdAt = directChat.getCreatedAt();
        this.updatedAt = directChat.getUpdatedAt();

        this.lastMessage = null;
        this.unreadCount = 0;
    }

    public DirectChatDTO(DirectChat directChat, User currentUser,
            ChatMessageResponseDTO lastMessage, Integer unreadCount) {
        this(directChat, currentUser);
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }
}
