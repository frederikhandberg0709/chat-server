package com.frederikhandberg.controller;

import com.frederikhandberg.adapter.UserDetailsImpl;
import com.frederikhandberg.dto.UserResponseDTO;
import com.frederikhandberg.mapper.UserMapper;
import com.frederikhandberg.model.User;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userDetails.getUser();
        UserResponseDTO userResponse = UserMapper.toDTO(currentUser);

        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(
            new User(
                id,
                "John",
                "Doe",
                "johndoe",
                "test@mail.com",
                "password",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
            )
        );
    }
}
