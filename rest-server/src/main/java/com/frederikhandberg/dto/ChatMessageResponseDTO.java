package com.frederikhandberg.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.frederikhandberg.model.ChatMessage;
import com.frederikhandberg.model.User;

import lombok.Data;

// This is an old DTO and should be removed when appropriate.

@Data
public class ChatMessageResponseDTO {
    private Long id;
    private UserDTO sender;
    private UserDTO receiver;
    private GroupChatDTO groupChat;
    private String content;
    private Set<UserDTO> readBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean readByCurrentUser;

    public ChatMessageResponseDTO(ChatMessage message, User currentUser) {
        this.id = message.getId();
        this.sender = new UserDTO(message.getSender());
        if (message.getDirectChat() != null) {
            User otherUser = message.getDirectChat().getOtherUser(currentUser);
            this.receiver = otherUser != null ? new UserDTO(otherUser) : null;
            this.groupChat = null;
        } else if (message.getGroupChat() != null) {
            this.receiver = null;
            this.groupChat = new GroupChatDTO(message.getGroupChat());
        }
        this.content = message.getContent();
        this.readBy = message.getReadBy().stream()
                .map(UserDTO::new)
                .collect(Collectors.toSet());
        this.createdAt = message.getCreatedAt();
        this.updatedAt = message.getUpdatedAt();
        this.readByCurrentUser = message.isReadBy(currentUser);
    }
}
