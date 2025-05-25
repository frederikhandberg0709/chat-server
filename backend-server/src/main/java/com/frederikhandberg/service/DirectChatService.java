package com.frederikhandberg.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.frederikhandberg.adapter.UserDetailsImpl;
import com.frederikhandberg.dto.ChatMessageResponseDTO;
import com.frederikhandberg.dto.DirectChatDTO;
import com.frederikhandberg.dto.DirectChatMessageRequestDTO;
import com.frederikhandberg.exception.AccessDeniedException;
import com.frederikhandberg.exception.ConflictException;
import com.frederikhandberg.exception.ResourceNotFoundException;
import com.frederikhandberg.model.ChatMessage;
import com.frederikhandberg.model.DirectChat;
import com.frederikhandberg.model.User;
import com.frederikhandberg.repository.ChatMessageRepository;
import com.frederikhandberg.repository.DirectChatRepository;
import com.frederikhandberg.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectChatService {

    private final DirectChatRepository directChatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public List<DirectChatDTO> getUserDirectChats(User currentUser) {
        List<DirectChat> directChats = directChatRepository
                .findAllByUserOrderByLastMessageTimeDesc(currentUser);

        return directChats.stream()
                .map(chat -> buildDirectChatDTO(chat, currentUser))
                .collect(Collectors.toList());
    }

    public DirectChatDTO getDirectChat(Long chatId, User currentUser) {
        DirectChat directChat = findDirectChatAndValidateAccess(chatId, currentUser);
        return buildDirectChatDTO(directChat, currentUser);
    }

    public DirectChatDTO getDirectChatWithUser(User currentUser, Long otherUserId) {
        User otherUser = findUser(otherUserId);

        DirectChat directChat = directChatRepository
                .findBetweenUsers(currentUser, otherUser)
                .orElseThrow(() -> new ResourceNotFoundException("No existing chat found between users"));

        return buildDirectChatDTO(directChat, currentUser);
    }

    public DirectChatDTO createDirectChatWithUser(User currentUser, Long otherUserId) {
        validateNotSelfChat(currentUser.getId(), otherUserId);
        User otherUser = findUser(otherUserId);

        if (directChatRepository.findBetweenUsers(currentUser, otherUser).isPresent()) {
            throw new ConflictException("Chat already exists between these users");
        }

        log.info("Creating new direct chat between users {} and {}",
                currentUser.getId(), otherUserId);

        DirectChat newChat = createNewDirectChat(currentUser, otherUser);
        return new DirectChatDTO(newChat, currentUser, null, 0);
    }

    public DirectChatDTO findOrCreateChatWithUser(User currentUser, Long otherUserId) {
        validateNotSelfChat(currentUser.getId(), otherUserId);
        User otherUser = findUser(otherUserId);

        DirectChat directChat = directChatRepository
                .findBetweenUsers(currentUser, otherUser)
                .orElseGet(() -> {
                    log.info("Creating new direct chat between users {} and {}",
                            currentUser.getId(), otherUserId);
                    return createNewDirectChat(currentUser, otherUser);
                });

        return buildDirectChatDTO(directChat, currentUser);
    }

    @Transactional
    public void deleteDirectChat(Long chatId, User currentUser) {
        DirectChat directChat = findDirectChatAndValidateAccess(chatId, currentUser);

        chatMessageRepository.deleteByDirectChat(directChat);

        directChatRepository.delete(directChat);
    }

    public List<DirectChatDTO> getChatsWithUnreadMessages(User currentUser) {
        List<DirectChat> chatsWithUnread = directChatRepository
                .findAllWithUnreadMessagesForUser(currentUser);

        return chatsWithUnread.stream()
                .map(chat -> buildDirectChatDTO(chat, currentUser))
                .collect(Collectors.toList());
    }

    public Integer getTotalUnreadMessageCount(User currentUser) {
        return directChatRepository.countTotalUnreadMessagesForUser(currentUser);
    }

    // DIRECT MESSAGING

    // public ChatMessageResponseDTO sendDirectMessage(User currentUser,
    // DirectChatMessageRequestDTO messageRequest) {
    public ChatMessageResponseDTO sendDirectMessage(User currentUser, DirectChatMessageRequestDTO messageRequest) {
        DirectChat targetChat = resolveTargetChat(currentUser, messageRequest);

        ChatMessage message = new ChatMessage();
        message.setContent(messageRequest.getContent());
        message.setSender(currentUser);
        message.setDirectChat(targetChat);

        log.info("Sending direct chat message by user ID {} to receiver ID {}",
                currentUser.getId(), messageRequest.getReceiverId());

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return new ChatMessageResponseDTO(savedMessage, currentUser);
    }

    public List<ChatMessageResponseDTO> getDirectChatMessages(Long directChatId, User currentUser, int page, int size) {
        DirectChat directChat = findDirectChatAndValidateAccess(directChatId, currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ChatMessage> messagesPage = chatMessageRepository
                .findByDirectChatOrderByCreatedAtDesc(directChat, pageable);

        return messagesPage.getContent().stream()
                .map(msg -> new ChatMessageResponseDTO(msg, currentUser))
                .collect(Collectors.toList());
    }

    public void markAllMessagesAsRead(Long chatId, User currentUser) {
        DirectChat directChat = findDirectChatAndValidateAccess(chatId, currentUser);

        List<ChatMessage> allMessages = chatMessageRepository
                .findByDirectChatOrderByCreatedAtDesc(directChat, Pageable.unpaged())
                .getContent();

        List<ChatMessage> unreadMessages = allMessages.stream()
                .filter(msg -> !msg.getSender().equals(currentUser))
                .filter(msg -> !msg.isReadBy(currentUser))
                .collect(Collectors.toList());

        unreadMessages.forEach(message -> message.markAsReadBy(currentUser));

        if (!unreadMessages.isEmpty()) {
            chatMessageRepository.saveAll(unreadMessages);
        }
    }

    // UTILITY METHODS

    public boolean isUserInChat(Long chatId, User user) {
        return directChatRepository.findById(chatId)
                .map(chat -> chat.hasUser(user))
                .orElse(false);
    }

    public User getOtherUserInChat(Long chatId, User currentUser) {
        DirectChat chat = findDirectChatAndValidateAccess(chatId, currentUser);
        return chat.getOtherUser(currentUser);
    }

    // PRIVATE HELPER METHODS - eliminates code duplication

    private DirectChat resolveTargetChat(User currentUser, DirectChatMessageRequestDTO messageRequest) {
        if (messageRequest.getDirectChatId() != null) {
            return findDirectChatAndValidateAccess(messageRequest.getDirectChatId(), currentUser);
        } else {
            DirectChatDTO chatDTO = findOrCreateChatWithUser(currentUser, messageRequest.getReceiverId());
            return convertDTOToEntity(chatDTO);
        }
    }

    private DirectChat findDirectChatAndValidateAccess(Long chatId, User currentUser) {
        DirectChat directChat = directChatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Direct chat not found"));

        if (!directChat.hasUser(currentUser)) {
            throw new AccessDeniedException("You are not a participant in this chat");
        }

        return directChat;
    }

    private DirectChatDTO buildDirectChatDTO(DirectChat chat, User currentUser) {
        Optional<ChatMessage> lastMessageOpt = chatMessageRepository
                .findTopByDirectChatOrderByCreatedAtDesc(chat);

        Integer unreadCount = chatMessageRepository
                .countUnreadDirectMessages(chat, currentUser);

        ChatMessageResponseDTO lastMessageDTO = lastMessageOpt
                .map(msg -> new ChatMessageResponseDTO(msg, currentUser))
                .orElse(null);

        return new DirectChatDTO(chat, currentUser, lastMessageDTO, unreadCount);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateNotSelfChat(Long currentUserId, Long otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("Cannot create a chat with yourself");
        }
    }

    private DirectChat createNewDirectChat(User user1, User user2) {
        DirectChat newChat = new DirectChat();
        newChat.setUser1(user1);
        newChat.setUser2(user2);
        return directChatRepository.save(newChat);
    }

    private DirectChat convertDTOToEntity(DirectChatDTO dto) {
        return directChatRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Direct chat not found"));
    }
}
