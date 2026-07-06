package com.example.aigument.domain.auth.service;

import com.example.aigument.domain.auth.dto.SocialUser;
import com.example.aigument.domain.user.entity.User;
import com.example.aigument.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.aigument.common.enums.UserRole.USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialService {

    private final UserRepository userRepository;


    @Transactional
    public User getSocialUser(SocialUser socialUser) {

        // 소셜 유저가 db에 존재하면 최신화, 존재하지 않으면 소셜 회원가입
        return userRepository.findByProviderId(socialUser.getProviderId())
                .map(existingUser -> {
                    log.info("[SocialService] 소셜 유저 정보 최신화 - UserId: {}, Provider: {}", existingUser.getId(), socialUser.getProvider());
                    return existingUser.updateSocialInfo(socialUser.getUsername(), socialUser.getEmail());
                })
                .orElseGet(() -> {
                    User newSocialUser = userRepository.save(
                            User.builder()
                                    .nickName(socialUser.getUsername())
                                    .email(socialUser.getEmail())
                                    .role(USER)
                                    .providerId(socialUser.getProviderId())
                                    .provider(socialUser.getProvider())
                                    .build()
                    );
                    log.info("[SocialService] 신규 소셜 회원가입 - UserId: {}, Provider: {}", newSocialUser.getId(), socialUser.getProvider());
                    return newSocialUser;
                });
    }
}
