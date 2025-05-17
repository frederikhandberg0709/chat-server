package com.frederikhandberg.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.frederikhandberg.model.GroupChat;
import com.frederikhandberg.model.User;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    @Query("SELECT g FROM GroupChat g JOIN g.members m WHERE m = :user")
    List<GroupChat> findAllByMember(@Param("user") User user);

    @Query("SELECT g FROM GroupChat g JOIN g.members m WHERE m = :user")
    Page<GroupChat> findAllByMember(@Param("user") User user, Pageable pageable);

    @Query("SELECT g FROM GroupChat g JOIN g.admins a WHERE a = :user")
    List<GroupChat> findAllByAdmin(@Param("user") User user);

    @Query("SELECT g FROM GroupChat g WHERE g.creator = :user")
    List<GroupChat> findAllByCreator(@Param("user") User user);

    @Query("SELECT g, MAX(m.createdAt) as lastMessageTime " +
            "FROM GroupChat g " +
            "JOIN g.members mem " +
            "LEFT JOIN ChatMessage m ON m.groupChat = g " +
            "WHERE mem = :user " +
            "GROUP BY g " +
            "ORDER BY lastMessageTime DESC NULLS LAST")
    List<GroupChat> findAllByMemberOrderByLastMessageTimeDesc(@Param("user") User user);

    @Query("SELECT g FROM GroupChat g JOIN g.members m " +
            "WHERE m = :user AND LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<GroupChat> searchByNameForUser(
            @Param("searchTerm") String searchTerm,
            @Param("user") User user);

    @Query("SELECT DISTINCT g FROM GroupChat g " +
            "JOIN g.members mem " +
            "JOIN ChatMessage m ON m.groupChat = g " +
            "WHERE mem = :user " +
            "AND m.sender <> :user " +
            "AND :user NOT MEMBER OF m.readBy")
    List<GroupChat> findAllWithUnreadMessagesForUser(@Param("user") User user);

    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "JOIN m.groupChat g " +
            "JOIN g.members mem " +
            "WHERE mem = :user " +
            "AND m.sender <> :user " +
            "AND :user NOT MEMBER OF m.readBy")
    Integer countTotalUnreadMessagesForUser(@Param("user") User user);

    @Query("SELECT COUNT(g) > 0 FROM GroupChat g JOIN g.members m " +
            "WHERE g.id = :groupChatId AND m = :user")
    boolean isUserMember(@Param("groupChatId") Long groupChatId, @Param("user") User user);

    @Query("SELECT COUNT(g) > 0 FROM GroupChat g JOIN g.admins a " +
            "WHERE g.id = :groupChatId AND a = :user")
    boolean isUserAdmin(@Param("groupChatId") Long groupChatId, @Param("user") User user);

    @Query("SELECT COUNT(g) > 0 FROM GroupChat g " +
            "WHERE g.id = :groupChatId AND g.creator = :user")
    boolean isUserCreator(@Param("groupChatId") Long groupChatId, @Param("user") User user);

    @Query("SELECT COUNT(m) FROM GroupChat g JOIN g.members m WHERE g.id = :groupChatId")
    Integer countMembers(@Param("groupChatId") Long groupChatId);
}
