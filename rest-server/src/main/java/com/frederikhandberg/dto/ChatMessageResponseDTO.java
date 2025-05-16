package com.frederikhandberg.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.frederikhandberg.model.ChatMessage;
import com.frederikhandberg.model.User;
import com.frederikhandberg.types.MessageType;

import lombok.Data;

@Data
public class ChatMessageResponseDTO {
    private Long id;
    private UserDTO sender;
    private UserDTO receiver;
    private GroupChatDTO groupChat;
    private String content;
    private MessageType messageType;
    private Set<UserDTO> readBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean readByCurrentUser;

    public ChatMessageResponseDTO(ChatMessage message, User currentUser) {
        this.id = message.getId();
        this.sender = new UserDTO(message.getSender());
        this.receiver = message.getReceiver() != null ? new UserDTO(message.getReceiver()) : null;
        this.groupChat = message.getGroupChat() != null ? new GroupChatDTO(message.getGroupChat()) : null;
        this.content = message.getContent();
        this.messageType = message.getMessageType();
        this.readBy = message.getReadBy().stream()
                .map(UserDTO::new)
                .collect(Collectors.toSet());
        this.createdAt = message.getCreatedAt();
        this.updatedAt = message.getUpdatedAt();
        this.readByCurrentUser = message.isReadBy(currentUser);
    }
}
