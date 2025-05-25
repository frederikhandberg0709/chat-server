package com.frederikhandberg.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.frederikhandberg.dto.ChatMessageResponseDTO;
import com.frederikhandberg.dto.DirectChatMessageRequestDTO;
import com.frederikhandberg.exception.AccessDeniedException;
import com.frederikhandberg.model.User;
import com.frederikhandberg.service.DirectChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DirectChatWebSocketController {

    private final DirectChatService directChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/direct-message")
    public void sendDirectMessage(@Payload DirectChatMessageRequestDTO messageRequest,
            SimpMessageHeaderAccessor headerAccessor) {

        // ChatMessageResponseDTO savedMessage =
        // directChatService.sendDirectMessage(messageRequest);

        // Long senderId = savedMessage.getSender().getId();

        // Long recipientId = savedMessage.getReceiver().getId();

        // messagingTemplate.convertAndSendToUser(
        // recipientId.toString(),
        // "/queue/direct-messages",
        // savedMessage);

        User currentUser = (User) headerAccessor.getSessionAttributes().get("user");
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        // SECURITY: Validate user has access to this chat
        if (messageRequest.getDirectChatId() != null) {
            if (!directChatService.isUserInChat(messageRequest.getDirectChatId(), currentUser)) {
                throw new AccessDeniedException("You are not a participant in this chat");
            }
        }

        ChatMessageResponseDTO savedMessage = directChatService.sendDirectMessage(currentUser, messageRequest);
        Long recipientId = savedMessage.getReceiver().getId();

        // Send only to the specific recipient (already validated as chat participant)
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/direct-messages",
                savedMessage);
    }
}
