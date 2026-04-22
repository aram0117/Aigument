package com.example.aigument.domain.auth.service;

import com.example.aigument.common.exception.CustomException;
import com.example.aigument.common.security.provider.JwtProvider;
import com.example.aigument.domain.auth.dto.response.UserResponse;
import com.example.aigument.domain.auth.dto.request.LoginRequest;
import com.example.aigument.domain.auth.dto.request.SignupRequest;
import com.example.aigument.domain.auth.dto.response.TokenResponse;
import com.example.aigument.domain.auth.dto.response.SignupResponse;
import com.example.aigument.domain.user.entity.User;
import com.example.aigument.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.example.aigument.common.enums.UserRole.USER;
import static com.example.aigument.common.exception.ErrorCode.*;
import static com.example.aigument.common.enums.ExpirationTime.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final LogoutRedirectService logoutRedirectService;


    @Transactional
    public SignupResponse signUp(SignupRequest request) {

        // 중복 검증
        duplicationCheck(request.getNickName(), request.getEmail());

        User newUser = User.builder()
                .nickName(request.getNickName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .provider("LOCAL")
                .role(USER)
                .build();

        // 사용자 생성
        userRepository.save(newUser);

        UserResponse userResponse = UserResponse.from(newUser);

        // 엑세스 토큰 발급
        String accessToken = jwtProvider.generateToken(newUser.getId(), newUser.getNickName(), newUser.getEmail(), newUser.getRole(), newUser.getProvider(), ACCESS_TOKEN_EXPIRATION_TIME.getExpirationTime());

        // 리프레쉬 토큰 발급
        String refreshToken = jwtProvider.generateToken(newUser.getId(), newUser.getNickName(), newUser.getEmail(), newUser.getRole(), newUser.getProvider(), REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime());

        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);

        return SignupResponse.from(userResponse, tokenResponse);
    }


    @Transactional
    public TokenResponse login(LoginRequest request) {

        User foundUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(LOGIN_FAILED)); // 이메일 검증 실패

        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            throw new CustomException(LOGIN_FAILED); // 비밀번호 검증 실패
        }

        // 엑세스 토큰 발급
        String accessToken = jwtProvider.generateToken(foundUser.getId(), foundUser.getNickName(), foundUser.getEmail(), foundUser.getRole(), foundUser.getProvider(), ACCESS_TOKEN_EXPIRATION_TIME.getExpirationTime());

        // 리프레쉬 토큰 발급
        String refreshToken = jwtProvider.generateToken(foundUser.getId(), foundUser.getNickName(), foundUser.getEmail(), foundUser.getRole(), foundUser.getProvider(), REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime());

        return TokenResponse.from(accessToken, refreshToken);
    }


    @Transactional
    public String logout(String accessToken) {

        String provider = jwtProvider.getClaims(accessToken).get("provider", String.class);

        long expiration = jwtProvider.getClaims(accessToken).getExpiration().getTime();

        long now = System.currentTimeMillis();

        // jwt 현재 남은 만료시간
        long redisExpire = expiration - now;

        // redis에 logout 값 저장
        if (redisExpire > 0) {
            redisTemplate.opsForValue().set(accessToken, "logout", redisExpire, TimeUnit.MILLISECONDS);
        }

        return logoutRedirectService.getRedirectUrl(provider);
    }


    /**
     * 검증 메서드
     */
    private void duplicationCheck(String nickName, String email) {

        boolean isDuplicateNickname = userRepository.existsByNickName(nickName);

        boolean isDuplicateEmail = userRepository.existsByEmail(email);

        if (isDuplicateNickname) {
            throw new CustomException(NICKNAME_ALREADY_EXISTS);
        }

        if (isDuplicateEmail) {
            throw new CustomException(EMAIL_ALREADY_EXISTS);
        }
    }
}
