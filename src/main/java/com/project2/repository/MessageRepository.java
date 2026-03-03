package com.project2.repository;

import com.project2.model.Message;
import com.project2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender = :u1 AND m.receiver = :u2) OR (m.sender = :u2 AND m.receiver = :u1) ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(User u1, User u2);

    List<Message> findByReceiverAndIsReadFalse(User receiver);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :receiver AND m.isRead = false")
    long countUnread(User receiver);
}
