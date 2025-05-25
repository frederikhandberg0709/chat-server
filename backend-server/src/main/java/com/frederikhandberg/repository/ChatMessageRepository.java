package com.frederikhandberg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.frederikhandberg.model.ChatMessage;
import com.frederikhandberg.model.DirectChat;
import com.frederikhandberg.model.GroupChat;
import com.frederikhandberg.model.User;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  Page<ChatMessage> findByDirectChatOrderByCreatedAtDesc(DirectChat directChat, Pageable pageable);

  Page<ChatMessage> findByGroupChatOrderByCreatedAtDesc(GroupChat groupChat, Pageable pageable);

  Optional<ChatMessage> findTopByDirectChatOrderByCreatedAtDesc(DirectChat directChat);

  Optional<ChatMessage> findTopByGroupChatOrderByCreatedAtDesc(GroupChat groupChat);

  @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.directChat = :directChat AND m.sender <> :user AND :user NOT MEMBER OF m.readBy")
  Integer countUnreadDirectMessages(@Param("directChat") DirectChat directChat, @Param("user") User user);

  @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.groupChat = :groupChat AND m.sender <> :user AND :user NOT MEMBER OF m.readBy")
  Integer countUnreadGroupMessages(@Param("groupChat") GroupChat groupChat, @Param("user") User user);

  @Query("SELECT m FROM ChatMessage m WHERE " +
      "(m.directChat IS NOT NULL OR m.groupChat IS NOT NULL) AND " +
      "m.sender <> :user AND :user NOT MEMBER OF m.readBy " +
      "ORDER BY m.createdAt DESC")
  List<ChatMessage> findAllUnreadMessages(@Param("user") User user, Pageable pageable);

  @Query("SELECT m FROM ChatMessage m " +
      "JOIN m.directChat d " +
      "WHERE (d.user1 = :user OR d.user2 = :user) " +
      "ORDER BY m.createdAt DESC")
  Page<ChatMessage> findAllDirectMessagesForUser(@Param("user") User user, Pageable pageable);

  @Query("SELECT m FROM ChatMessage m " +
      "JOIN m.groupChat g " +
      "JOIN g.members mem " +
      "WHERE mem = :user " +
      "ORDER BY m.createdAt DESC")
  Page<ChatMessage> findAllGroupMessagesForUser(@Param("user") User user, Pageable pageable);

  void deleteByDirectChat(DirectChat directChat);

  void deleteByGroupChat(GroupChat groupChat);
}
