package com.frederikhandberg.dto;

import com.frederikhandberg.model.User;

import lombok.Data;

@Data
public class UserMinimalDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;

    public UserMinimalDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }
}
