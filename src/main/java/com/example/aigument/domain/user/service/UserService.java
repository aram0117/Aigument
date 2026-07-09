package com.example.aigument.domain.user.service;

import com.example.aigument.common.annotation.MeasureLatency;
import com.example.aigument.common.exception.CustomException;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.auth.dto.request.InputCodeRequest;
import com.example.aigument.domain.user.dto.request.UpdateUserRequest;
import com.example.aigument.domain.user.dto.response.UpdateUserResponse;
import com.example.aigument.domain.user.entity.User;
import com.example.aigument.domain.user.dto.response.GetUserResponse;
import com.example.aigument.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static com.example.aigument.common.exception.ErrorCode.*;
import static com.example.aigument.common.infra.redis.RedisKeys.smsAuth;

@Slf4j
@Service
@RequiredArgsConstructor
@MeasureLatency
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Transactional(readOnly = true)
    public GetUserResponse getUser(AuthUser authUser) {

        User foundUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        return GetUserResponse.from(foundUser);
    }

    @Transactional
    public UpdateUserResponse updateUser(AuthUser authUser, UpdateUserRequest request) {

        User foundUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 로컬 계정이 아닌 경우
        notLocalAccountCheck(foundUser.getProvider());

        // 비밀번호 확인 절차
        verifyPassword(request.getAuthPassword(), foundUser.getPassword());

        // 중복 검증 (본인 제외)
        duplicationCheck(foundUser.getId(), request.getNickName(), request.getEmail());

        // 사용자 정보 부분 수정
        foundUser.patchUpdate(request);

        log.info("[UserService] 사용자 정보 수정 완료 - UserId: {}", foundUser.getId());

        return UpdateUserResponse.from(foundUser);
    }

    public void deleteUser(AuthUser authUser, InputCodeRequest request) {

        User foundUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        validateVerificationCode(foundUser.getPhoneNumber(), request.getInputCode());

        userRepository.deleteById(foundUser.getId());

        log.info("[UserService] 회원 탈퇴 처리 완료 - UserId: {}", foundUser.getId());
    }


    /**
     * 검증 메서드
     */
    private void notLocalAccountCheck(String provider) {

        if (!"LOCAL".equals(provider)) {
            throw new CustomException(SOCIAL_ACCOUNT_CANNOT_USER_MODIFY);
        }
    }

    private void verifyPassword(String rawPassword, String encodedPassword) {

        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CustomException(WRONG_PASSWORD_CONFIRM);
        }
    }

    private void duplicationCheck(Long userId, String requestNickName, String requestEmail) {

        if (StringUtils.hasText(requestNickName)
                && userRepository.existsByNickNameAndIdNot(requestNickName, userId)) {
            throw new CustomException(NICKNAME_ALREADY_EXISTS);
        }

        if (StringUtils.hasText(requestEmail)
                && userRepository.existsByEmailAndIdNot(requestEmail, userId)) {
            throw new CustomException(EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateVerificationCode(String phoneNumber, String inputCode) {

        String redisKey = smsAuth(phoneNumber);
        String verificationCode = redisTemplate.opsForValue().get(redisKey);

        // 계정에 등록된 휴대폰 번호로 인증 코드 요청을 하지 않을 때 (verificationCode -> null)
        Optional.ofNullable(verificationCode)
                .orElseThrow(() -> new CustomException(UNVERIFIED_PHONE_NUMBER));

        // 입력한 코드와 인증 코드가 다를 때
        if (!inputCode.equals(verificationCode)) {
            throw new CustomException(AUTH_CODE_MISMATCH);
        }
    }
}
