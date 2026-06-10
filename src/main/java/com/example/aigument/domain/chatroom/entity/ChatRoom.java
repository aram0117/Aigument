package com.example.aigument.domain.chatroom.entity;

import com.example.aigument.common.annotation.UserRoleValidAnnotation;
import com.example.aigument.common.enums.CategoryType;
import com.example.aigument.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @UserRoleValidAnnotation
    private CategoryType category;

    @Column(name = "content")
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private User guest;

    public ChatRoom(String title, CategoryType category, String content, User host, User guest) {
        this.title = title;
        this.category = category;
        this.content = content;
        this.host = host;
        this.guest = guest;
    }

    public void enterGuestChatRoom(User guest) {
        this.guest = guest;
    }

    public boolean isGuest(Long authUserId) {
        return this.guest != null && this.guest.getId().equals(authUserId);
    }
}
