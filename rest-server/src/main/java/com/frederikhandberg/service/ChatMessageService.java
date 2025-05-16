package com.frederikhandberg.service;

import org.springframework.stereotype.Service;

import com.frederikhandberg.exception.AccessDeniedException;
import com.frederikhandberg.exception.ResourceNotFoundException;
import com.frederikhandberg.model.ChatMessage;
import com.frederikhandberg.model.User;
import com.frederikhandberg.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage findMessageById(Long messageId, User currentUser) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!hasAccessToMessage(message, currentUser)) {
            throw new AccessDeniedException("You don't have access to this message");
        }

        if (!message.isReadBy(currentUser)) {
            message.markAsReadBy(currentUser);
            chatMessageRepository.save(message);
        }

        return message;
    }

    private boolean hasAccessToMessage(ChatMessage message, User currentUser) {
        if (message.getSender().getId().equals(currentUser.getId())) {
            return true;
        }

        if (message.getReceiver() != null &&
                message.getReceiver().getId().equals(currentUser.getId())) {
            return true;
        }

        if (message.getGroupChat() != null &&
                message.getGroupChat().getMembers().contains(currentUser)) {
            return true;
        }

        return false;
    }
}
