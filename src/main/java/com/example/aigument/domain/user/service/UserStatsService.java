package com.example.aigument.domain.user.service;

import com.example.aigument.common.exception.CustomException;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.user.dto.response.GetUserStatsResponse;
import com.example.aigument.domain.user.entity.User;
import com.example.aigument.domain.user.entity.UserStats;
import com.example.aigument.domain.user.repository.UserRepository;
import com.example.aigument.domain.user.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.aigument.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.aigument.common.exception.ErrorCode.NOT_FOUND_USER_STATS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    @Transactional
    public void incrementStatsCount(String winner, String loser) {

        // 문자열중 유저id 값만 파싱
        Long winnerId = Long.parseLong(winner.replaceAll("[^0-9]", ""));
        Long loserId = Long.parseLong(loser.replaceAll("[^0-9]", ""));

        /*
        유저의 승패 결과 반영
         */
        UserStats winnerStats = getOrCreateUserStats(winnerId);
        UserStats loserStats = getOrCreateUserStats(loserId);

        winnerStats.incrementWinCount();
        loserStats.incrementLossCount();

        log.info("[UserStatsService] AI 판정 전적 반영 완료 - WinnerId: {}, LoserId: {}", winnerId, loserId);
    }

    @Transactional(readOnly = true)
    public GetUserStatsResponse getUserStats(AuthUser authUser) {

        UserStats foundUserStats = userStatsRepository.findByUserId(authUser.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER_STATS));

        return GetUserStatsResponse.from(foundUserStats);
    }


    /**
     * user_stats 속성이 존재하면 가져오고 존재하지 않으면 생성
     */
    public UserStats getOrCreateUserStats(Long userId) {

        return userStatsRepository.findByUserId(userId)
                .orElseGet(() -> {

                    User founduser = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

                    UserStats newUserStats = new UserStats(founduser);
                    return userStatsRepository.save(newUserStats);
                });
    }
}
