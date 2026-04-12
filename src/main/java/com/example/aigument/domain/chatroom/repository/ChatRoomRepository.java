package com.example.aigument.domain.chatroom.repository;

import com.example.aigument.domain.chatroom.entity.ChatRoom;
import com.example.aigument.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // LEFT OUTER JOIN이며 연관관계 데이터 null 조회 허용
    @EntityGraph(attributePaths = {"host", "guest"})
    Optional<ChatRoom> findById(Long id);

    @EntityGraph(attributePaths = {"host", "guest"})
    List<ChatRoom> findAll();

    boolean existsByHost(User host);

    void deleteByGuest(User guest);
}
