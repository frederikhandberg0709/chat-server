package com.frederikhandberg.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.frederikhandberg.model.GroupChat;

import lombok.Data;

@Data
public class GroupChatDTO {
    private Long id;
    private String name;
    private String description;
    private UserMinimalDTO creator;
    private Set<UserMinimalDTO> members;
    private Set<UserMinimalDTO> admins;
    private LocalDateTime createdAt;

    public GroupChatDTO(GroupChat groupChat) {
        this.id = groupChat.getId();
        this.name = groupChat.getName();
        this.description = groupChat.getDescription();
        this.creator = new UserMinimalDTO(groupChat.getCreator());
        this.members = groupChat.getMembers().stream()
                .map(UserMinimalDTO::new)
                .collect(Collectors.toSet());
        this.admins = groupChat.getAdmins().stream()
                .map(UserMinimalDTO::new)
                .collect(Collectors.toSet());
        this.createdAt = groupChat.getCreatedAt();
    }
}
