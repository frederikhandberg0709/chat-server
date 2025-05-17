package com.frederikhandberg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.frederikhandberg.model.DirectChat;
import com.frederikhandberg.model.User;

@Repository
public interface DirectChatRepository extends JpaRepository<DirectChat, Long> {

        @Query("SELECT d FROM DirectChat d WHERE d.user1 = :user OR d.user2 = :user")
        List<DirectChat> findAllByUser(@Param("user") User user);

        @Query("SELECT d FROM DirectChat d WHERE " +
                        "(d.user1 = :userA AND d.user2 = :userB) OR " +
                        "(d.user1 = :userB AND d.user2 = :userA)")
        Optional<DirectChat> findBetweenUsers(
                        @Param("userA") User userA,
                        @Param("userB") User userB);

        @Query("SELECT COUNT(d) > 0 FROM DirectChat d WHERE " +
                        "(d.user1 = :userA AND d.user2 = :userB) OR " +
                        "(d.user1 = :userB AND d.user2 = :userA)")
        boolean existsBetweenUsers(
                        @Param("userA") User userA,
                        @Param("userB") User userB);

        @Query("SELECT d, MAX(m.createdAt) as lastMessageTime " +
                        "FROM DirectChat d LEFT JOIN ChatMessage m ON m.directChat = d " +
                        "WHERE d.user1 = :user OR d.user2 = :user " +
                        "GROUP BY d " +
                        "ORDER BY lastMessageTime DESC NULLS LAST")
        List<DirectChat> findAllByUserOrderByLastMessageTimeDesc(@Param("user") User user);

        @Query("SELECT DISTINCT d FROM DirectChat d " +
                        "JOIN ChatMessage m ON m.directChat = d " +
                        "WHERE (d.user1 = :user OR d.user2 = :user) " +
                        "AND m.sender <> :user " +
                        "AND :user NOT MEMBER OF m.readBy")
        List<DirectChat> findAllWithUnreadMessagesForUser(@Param("user") User user);

        @Query("SELECT COUNT(m) FROM ChatMessage m " +
                        "JOIN m.directChat d " +
                        "WHERE (d.user1 = :user OR d.user2 = :user) " +
                        "AND m.sender <> :user " +
                        "AND :user NOT MEMBER OF m.readBy")
        Integer countTotalUnreadMessagesForUser(@Param("user") User user);

        @Query("DELETE FROM DirectChat d WHERE " +
                        "(d.user1 = :userA AND d.user2 = :userB) OR " +
                        "(d.user1 = :userB AND d.user2 = :userA)")
        void deleteBetweenUsers(
                        @Param("userA") User userA,
                        @Param("userB") User userB);
}
