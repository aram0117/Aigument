package com.example.aigument.domain.user.entity;

import com.example.aigument.common.enums.annotation.UserRoleValidAnnotation;
import com.example.aigument.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", nullable = false, length = 30, unique = true)
    private String nickName;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "provider", length = 10)
    private String provider;

    @Column(name = "provider_id", unique = true)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    @UserRoleValidAnnotation
    private UserRole role;

    // 회원가입, 탈퇴 시 저장과 삭제 전파
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "user_stats_id")
    private UserStats userStats;

    @Builder
    public User(String nickName, String email, String password, String phoneNumber, String provider, String providerId, UserRole role,  UserStats userStats) {
        this.nickName = nickName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
        this.userStats = new UserStats(this);
    }

    public User updateSocialInfo(String nickName, String email) {
        this.nickName = nickName;
        this.email = email;

        return this;
    }
}
