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
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.example.aigument.common.enums.UserRole.USER;
import static com.example.aigument.common.exception.ErrorCode.*;
import static com.example.aigument.common.enums.ExpirationTime.*;
import static com.example.aigument.common.infra.redis.RedisKeys.smsAuth;

@Slf4j
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

        // 인증 코드 검증
        validateVerificationCode(request.getPhoneNumber(), request.getInputCode());

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

        log.info("[AuthService] 회원가입 성공 - UserId: {}, Nickname: {}", newUser.getId(), newUser.getNickName());

        return SignupResponse.from(userResponse, tokenResponse);
    }


    @Transactional
    public TokenResponse login(LoginRequest request) {

        User foundUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("[AuthService] 로그인 실패 - 존재하지 않는 이메일: {}", request.getEmail());
                    return new CustomException(LOGIN_FAILED);
                }); // 이메일 검증 실패

        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            log.error("[AuthService] 로그인 실패 - 비밀번호 불일치, UserId: {}", foundUser.getId());
            throw new CustomException(LOGIN_FAILED); // 비밀번호 검증 실패
        }

        // 엑세스 토큰 발급
        String accessToken = jwtProvider.generateToken(foundUser.getId(), foundUser.getNickName(), foundUser.getEmail(), foundUser.getRole(), foundUser.getProvider(), ACCESS_TOKEN_EXPIRATION_TIME.getExpirationTime());

        // 리프레쉬 토큰 발급
        String refreshToken = jwtProvider.generateToken(foundUser.getId(), foundUser.getNickName(), foundUser.getEmail(), foundUser.getRole(), foundUser.getProvider(), REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime());

        log.info("[AuthService] 로그인 성공 - UserId: {}", foundUser.getId());

        return TokenResponse.from(accessToken, refreshToken);
    }


    @Transactional
    public String logout(String accessToken) {

        Claims claims = jwtProvider.getClaims(accessToken);
        String provider = claims.get("provider", String.class);
        long expiration = claims.getExpiration().getTime();

        long now = System.currentTimeMillis();

        // jwt 현재 남은 만료시간
        long redisExpire = expiration - now;

        // redis에 logout 값 저장
        if (redisExpire > 0) {
            redisTemplate.opsForValue().set(accessToken, "logout", redisExpire, TimeUnit.MILLISECONDS);
        }

        log.info("[AuthService] 로그아웃 처리 완료 - Provider: {}", provider);

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

    private void validateVerificationCode(String phoneNumber, String inputCode) {

        String redisKey = smsAuth(phoneNumber);
        String verificationCode = redisTemplate.opsForValue().get(redisKey);

        // 인증코드를 받은 휴대폰 번호로 인증코드를 입력하지 않았을 경우 (verificationCode -> null)
        Optional.ofNullable(verificationCode)
                .orElseThrow(() -> new CustomException(INVALID_REGISTRATION_STEP));

        // 입력한 코드와 인증 코드가 다를 때
        if (!inputCode.equals(verificationCode)) {
            throw new CustomException(AUTH_CODE_MISMATCH);
        }
    }
}
