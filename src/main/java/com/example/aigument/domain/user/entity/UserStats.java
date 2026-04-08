package com.example.aigument.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "user_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_win_count")
    private long totalWinCount = 0;

    @Column(name = "total_loss_count")
    private long totalLossCount = 0;


    public UserStats(User user) {
        this.user = user;
    }

    public void incrementWinCount() {
        this.totalWinCount++;
    }

    public void incrementLossCount() {
        this.totalLossCount++;
    }
}
