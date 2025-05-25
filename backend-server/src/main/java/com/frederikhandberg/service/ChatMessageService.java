package com.frederikhandberg.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.frederikhandberg.adapter.UserDetailsImpl;
import com.frederikhandberg.dto.ChatMessageRequestDTO;
import com.frederikhandberg.dto.ChatMessageResponseDTO;
import com.frederikhandberg.dto.DirectChatDTO;
import com.frederikhandberg.dto.DirectChatMessageRequestDTO;
import com.frederikhandberg.exception.AccessDeniedException;
import com.frederikhandberg.exception.ResourceNotFoundException;
import com.frederikhandberg.model.ChatMessage;
import com.frederikhandberg.model.DirectChat;
import com.frederikhandberg.model.GroupChat;
import com.frederikhandberg.model.User;
import com.frederikhandberg.repository.ChatMessageRepository;
import com.frederikhandberg.repository.DirectChatRepository;
import com.frederikhandberg.repository.GroupChatRepository;
import com.frederikhandberg.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final DirectChatRepository directChatRepository;
    private final GroupChatRepository groupChatRepository;
    private final UserRepository userRepository;

    public ChatMessage createDirectMessage(String content, User sender, DirectChat directChat) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setSender(sender);
        message.setCreatedAt(LocalDateTime.now());

        return chatMessageRepository.save(message);
    }

    public List<ChatMessageResponseDTO> getDirectChatMessages(Long directChatId, User currentUser, int page, int size) {
        DirectChat directChat = new DirectChat(); // This should come from DirectChatService validation
        directChat.setId(directChatId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ChatMessage> messagesPage = chatMessageRepository
                .findByDirectChatOrderByCreatedAtDesc(directChat, pageable);

        return messagesPage.getContent().stream()
                .map(msg -> new ChatMessageResponseDTO(msg, currentUser))
                .collect(Collectors.toList());
    }

    public void markDirectChatMessagesAsRead(DirectChat directChat, User currentUser) {
        List<ChatMessage> allMessages = chatMessageRepository
                .findByDirectChatOrderByCreatedAtDesc(directChat, Pageable.unpaged())
                .getContent();

        List<ChatMessage> unreadMessages = allMessages.stream()
                .filter(msg -> !msg.getSender().equals(currentUser)) // Not sent by current user
                .filter(msg -> !msg.isReadBy(currentUser)) // Not read by current user
                .collect(Collectors.toList());

        // Mark each message as read by this user
        unreadMessages.forEach(message -> message.markAsReadBy(currentUser));

        // Save all updated messages
        if (!unreadMessages.isEmpty()) {
            chatMessageRepository.saveAll(unreadMessages);
        }
    }

    public void deleteAllMessagesInDirectChat(DirectChat directChat) {
        chatMessageRepository.deleteByDirectChat(directChat);
    }

    /**
     * Get messages for a group chat with pagination
     */
    @Transactional
    public List<ChatMessageResponseDTO> getGroupChatMessages(Long groupChatId, User currentUser, int page, int size) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));

        // Verify the current user is a member of this group
        if (!groupChat.getMembers().contains(currentUser)) {
            throw new AccessDeniedException("You are not a member of this group chat");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChatMessage> messages = chatMessageRepository.findByGroupChatOrderByCreatedAtDesc(groupChat, pageable);

        // Mark messages as read
        messages.forEach(message -> {
            if (!message.isReadBy(currentUser) && !message.getSender().equals(currentUser)) {
                message.markAsReadBy(currentUser);
                chatMessageRepository.save(message);
                log.debug("Marked message {} as read by user {}", message.getId(), currentUser.getId());
            }
        });

        return messages.map(message -> new ChatMessageResponseDTO(message, currentUser))
                .getContent();
    }

    /**
     * Send a message in a direct chat
     */
    // @Transactional
    // public ChatMessageResponseDTO sendDirectMessage(User currentUser,
    // DirectChatMessageRequestDTO messageRequest) {
    // DirectChat targetChat;

    // if (messageRequest.getDirectChatId() != null) {
    // // Sending to existing chat
    // targetChat = getExistingDirectChat(messageRequest.getDirectChatId(),
    // currentUser);
    // } else {
    // // Sending to user (find or create chat)
    // DirectChatDTO chatDTO =
    // directChatService.findOrCreateChatWithUser(currentUser,
    // messageRequest.getReceiverId());
    // targetChat = convertDirectChatDTOToEntity(chatDTO);
    // }

    // return createAndSaveDirectMessage(currentUser, messageRequest.getContent(),
    // targetChat);

    // DirectChat directChat = directChatRepository.findById(chatId)
    // .orElseThrow(() -> new ResourceNotFoundException("Direct chat not found"));

    // // Verify the sender is part of this chat
    // if (!directChat.hasUser(sender)) {
    // throw new AccessDeniedException("You are not a participant in this chat");
    // }

    // ChatMessage message = new ChatMessage();
    // message.setSender(sender);
    // message.setDirectChat(directChat);
    // message.setGroupChat(null);
    // message.setContent(messageRequest.getContent());

    // // Mark as read by the sender
    // message.markAsReadBy(sender);

    // ChatMessage savedMessage = chatMessageRepository.save(message);
    // log.info("Direct message sent by user {} in chat {}", sender.getId(),
    // chatId);

    // return new ChatMessageResponseDTO(savedMessage, sender);
    // }

    // @Transactional
    // public ChatMessageResponseDTO sendDirectMessageToUser(UserDetailsImpl
    // userDetails,
    // DirectMessageRequestDTO messageRequest) {
    // User sender = userDetails.getUser();

    // User recipient = userRepository.findById(messageRequest.getRecipientId())
    // .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

    // if (sender.getId().equals(recipient.getId())) {
    // throw new IllegalArgumentException("Cannot send a message to yourself");
    // }

    // // Find existing chat or create a new one
    // DirectChat directChat = directChatRepository
    // .findBetweenUsers(sender, recipient)
    // .orElseGet(() -> {
    // log.info("Creating new direct chat between users {} and {}",
    // sender.getId(), recipient.getId());

    // DirectChat newChat = new DirectChat();
    // newChat.setUser1(sender);
    // newChat.setUser2(recipient);
    // return directChatRepository.save(newChat);
    // });

    // // Create and save the message
    // ChatMessage message = new ChatMessage();
    // message.setSender(sender);
    // message.setDirectChat(directChat);
    // message.setGroupChat(null);
    // message.setContent(messageRequest.getContent());

    // // Mark as read by the sender
    // message.markAsReadBy(sender);

    // ChatMessage savedMessage = chatMessageRepository.save(message);
    // log.info("Direct message sent by user {} to user {} in chat {}",
    // sender.getId(), recipient.getId(), directChat.getId());

    // return new ChatMessageResponseDTO(savedMessage, sender);
    // }

    /**
     * Send a message in a group chat
     */
    @Transactional
    public ChatMessageResponseDTO sendGroupMessage(Long groupChatId, User sender,
            ChatMessageRequestDTO messageRequest) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));

        // Verify the sender is a member of this group
        if (!groupChat.getMembers().contains(sender)) {
            throw new AccessDeniedException("You are not a member of this group chat");
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setDirectChat(null);
        message.setGroupChat(groupChat);
        message.setContent(messageRequest.getContent());

        // Mark as read by the sender
        message.markAsReadBy(sender);

        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("Group message sent by user {} in group {}", sender.getId(), groupChatId);

        return new ChatMessageResponseDTO(savedMessage, sender);
    }

    /**
     * Mark a specific message as read
     */
    @Transactional
    public void markMessageAsRead(Long messageId, User user) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Verify user has access to this message
        if ((message.getDirectChat() != null && !message.getDirectChat().hasUser(user)) ||
                (message.getGroupChat() != null && !message.getGroupChat().getMembers().contains(user))) {
            throw new AccessDeniedException("You do not have access to this message");
        }

        if (!message.isReadBy(user)) {
            message.markAsReadBy(user);
            chatMessageRepository.save(message);
            log.debug("Marked message {} as read by user {}", messageId, user.getId());
        }
    }

    /**
     * Mark all messages in a direct chat as read
     */
    @Transactional
    public void markAllDirectChatMessagesAsRead(Long chatId, User user) {
        DirectChat directChat = directChatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Direct chat not found"));

        // Verify user is part of this chat
        if (!directChat.hasUser(user)) {
            throw new AccessDeniedException("You are not a participant in this chat");
        }

        // Get all unread messages not sent by the current user
        Pageable pageable = PageRequest.of(0, 1000); // Reasonable limit
        Page<ChatMessage> unreadMessages;
        int page = 0;

        do {
            pageable = PageRequest.of(page, 100);
            unreadMessages = chatMessageRepository.findByDirectChatOrderByCreatedAtDesc(directChat, pageable);

            for (ChatMessage message : unreadMessages) {
                if (!message.isReadBy(user) && !message.getSender().equals(user)) {
                    message.markAsReadBy(user);
                    chatMessageRepository.save(message);
                }
            }

            page++;
        } while (unreadMessages.hasNext());

        log.info("Marked all messages in direct chat {} as read by user {}", chatId, user.getId());
    }

    /**
     * Mark all messages in a group chat as read
     */
    @Transactional
    public void markAllGroupChatMessagesAsRead(Long groupChatId, User user) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));

        // Verify user is a member of this group
        if (!groupChat.getMembers().contains(user)) {
            throw new AccessDeniedException("You are not a member of this group chat");
        }

        // Get all unread messages not sent by the current user
        Pageable pageable = PageRequest.of(0, 1000); // Reasonable limit
        Page<ChatMessage> unreadMessages;
        int page = 0;

        do {
            pageable = PageRequest.of(page, 100);
            unreadMessages = chatMessageRepository.findByGroupChatOrderByCreatedAtDesc(groupChat, pageable);

            for (ChatMessage message : unreadMessages) {
                if (!message.isReadBy(user) && !message.getSender().equals(user)) {
                    message.markAsReadBy(user);
                    chatMessageRepository.save(message);
                }
            }

            page++;
        } while (unreadMessages.hasNext());

        log.info("Marked all messages in group chat {} as read by user {}", groupChatId, user.getId());
    }

    /**
     * Delete a message (only by sender or admin)
     */
    @Transactional
    public void deleteMessage(Long messageId, User user) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Check if user is the sender
        boolean isSender = message.getSender().getId().equals(user.getId());

        // Check if user is a group admin (for group messages)
        boolean isGroupAdmin = message.getGroupChat() != null &&
                message.getGroupChat().getAdmins().contains(user);

        if (!isSender && !isGroupAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this message");
        }

        chatMessageRepository.delete(message);
        log.info("Message {} deleted by user {}", messageId, user.getId());
    }

    /**
     * Get all unread messages for a user
     */
    public List<ChatMessageResponseDTO> getUnreadMessages(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ChatMessage> unreadMessages = chatMessageRepository.findAllUnreadMessages(user, pageable);

        return unreadMessages.stream()
                .map(message -> new ChatMessageResponseDTO(message, user))
                .collect(Collectors.toList());
    }
}
