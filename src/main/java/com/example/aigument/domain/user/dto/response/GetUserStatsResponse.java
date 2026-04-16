package com.example.aigument.domain.user.dto.response;

import com.example.aigument.domain.user.entity.UserStats;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetUserStatsResponse {

    private final Long userId;
    private final long totalWinCount;
    private final long totalLoseCount;

    public static GetUserStatsResponse from(UserStats userStats) {

        return new GetUserStatsResponse(
                userStats.getUser().getId(),
                userStats.getTotalWinCount(),
                userStats.getTotalLossCount()
        );
    }
}
