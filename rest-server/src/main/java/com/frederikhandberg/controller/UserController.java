package com.frederikhandberg.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frederikhandberg.model.User;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(new User(id, "John", "Doe", "johndoe", "test@mail.com", "password",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()));
    }
}
