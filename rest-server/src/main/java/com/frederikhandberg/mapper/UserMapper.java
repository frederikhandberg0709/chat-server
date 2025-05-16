package com.frederikhandberg.mapper;

import com.frederikhandberg.dto.RegisterRequestDTO;
import com.frederikhandberg.dto.UserResponseDTO;
import com.frederikhandberg.model.User;

public class UserMapper {
    public static UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName());
    }

    public static User toEntity(RegisterRequestDTO dto, String encodedPassword) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPassword(encodedPassword);
        return user;
    }
}
