package com.frederikhandberg.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frederikhandberg.adapter.UserDetailsImpl;
import com.frederikhandberg.dto.ChatMessageResponseDTO;
import com.frederikhandberg.dto.DirectChatDTO;
import com.frederikhandberg.dto.DirectChatMessageRequestDTO;
import com.frederikhandberg.model.User;
import com.frederikhandberg.service.DirectChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/direct-chats")
@Tag(name = "Direct Chats", description = "Endpoints for managing direct chats")
public class DirectChatController {

    private final DirectChatService directChatService;

    @Operation(summary = "Get direct chats for logged in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chats found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })

    @GetMapping
    public ResponseEntity<List<DirectChatDTO>> getUserDirectChats(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(directChatService.getUserDirectChats(currentUser));
    }

    @Operation(summary = "Get existing direct chat with a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat found"),
            @ApiResponse(responseCode = "404", description = "No existing chat found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<DirectChatDTO> getDirectChatWithUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        return ResponseEntity.ok(directChatService.getDirectChatWithUser(currentUser, userId));
    }

    @Operation(summary = "Create a new direct chat with a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chat created"),
            @ApiResponse(responseCode = "409", description = "Chat already exists"),
            @ApiResponse(responseCode = "400", description = "Cannot create chat with yourself"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/users/{userId}")
    public ResponseEntity<DirectChatDTO> createDirectChatWithUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long userId) {

        User currentUser = userDetails.getUser();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(directChatService.createDirectChatWithUser(currentUser, userId));
    }

    @Operation(summary = "Get a specific direct chat by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat found"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{chatId}")
    public ResponseEntity<DirectChatDTO> getDirectChat(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId) {
        return ResponseEntity.ok(directChatService.getDirectChat(chatId,
                currentUser));
    }

    @GetMapping("/{directChatId}/messages")
    public ResponseEntity<List<ChatMessageResponseDTO>> getDirectChatMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long directChatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(directChatService.getDirectChatMessages(directChatId, currentUser, page, size));
    }

    @Operation(summary = "Send a direct message", description = "Send a message to an existing chat (using directChatId) or to a user (using receiverId)")
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponseDTO> sendDirectMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody DirectChatMessageRequestDTO messageRequest) {

        User currentUser = userDetails.getUser();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(directChatService.sendDirectMessage(currentUser, messageRequest));
    }

    @PutMapping("/{chatId}/read")
    public ResponseEntity<Void> markAllMessagesAsRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId) {
        directChatService.markAllMessagesAsRead(chatId, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteDirectChat(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId) {
        directChatService.deleteDirectChat(chatId, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread")
    public ResponseEntity<List<DirectChatDTO>> getChatsWithUnreadMessages(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(directChatService.getChatsWithUnreadMessages(currentUser));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Integer> getUnreadMessageCount(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(directChatService.getTotalUnreadMessageCount(currentUser));
    }
}
