package com.example.aigument.domain.user.service;

import com.example.aigument.common.exception.CustomException;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.user.dto.request.UpdateUserRequest;
import com.example.aigument.domain.user.dto.response.UpdateUserResponse;
import com.example.aigument.domain.user.entity.User;
import com.example.aigument.domain.user.dto.response.GetUserResponse;
import com.example.aigument.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.aigument.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public GetUserResponse getUser(AuthUser authUser) {

        User foundUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        return GetUserResponse.from(foundUser);
    }

    @Transactional
    public UpdateUserResponse updateUser(AuthUser authUser, UpdateUserRequest updateUserRequest) {

        User foundUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        // 로컬 계정이 아닌 경우
        notLocalAccountCheck(foundUser.getProvider());

        // 비밀번호 확인 절차
        verifyPassword(updateUserRequest.getAuthPassword(), foundUser.getPassword());

        // 중복 검증
        duplicationCheck(updateUserRequest.getNickName(), updateUserRequest.getEmail());

        // 사용자 정보 부분 수정
        foundUser.patchUpdate(updateUserRequest);

        return UpdateUserResponse.from(foundUser);
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

    private void duplicationCheck(String requestNickName, String requestEmail) {

        // 요청 닉네임 인자가 null이 아닐 때만 조회 실행
        Optional.ofNullable(requestNickName)
                .ifPresent(nickName -> {
                    if (userRepository.existsByNickName(nickName)) {
                        throw new CustomException(NICKNAME_ALREADY_EXISTS);
                    }
                });

        // 요청 이메일 인자가 null이 아닐 때만 조회 실행
        Optional.ofNullable(requestEmail)
                .ifPresent(email -> {
                    if (userRepository.existsByEmail(email)) {
                        throw new CustomException(EMAIL_ALREADY_EXISTS);
                    }
                });
    }
}
