package com.frederikhandberg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDTO {
        private final Long id;
        private final String username;
        private final String email;
        private final String firstName;
        private final String lastName;
}
