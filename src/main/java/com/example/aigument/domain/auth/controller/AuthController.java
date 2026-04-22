package com.example.aigument.domain.auth.controller;

import com.example.aigument.common.dto.response.CommonResponse;
import com.example.aigument.common.exception.CustomException;
import com.example.aigument.common.security.provider.RefreshTokenCookie;
import com.example.aigument.domain.auth.dto.response.TokenResponse;
import com.example.aigument.domain.auth.dto.request.LoginRequest;
import com.example.aigument.domain.auth.dto.request.SignupRequest;
import com.example.aigument.domain.auth.dto.response.SignupResponse;
import com.example.aigument.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.example.aigument.common.exception.ErrorCode.UNAUTHORIZED_ACCESS;

@Tag(name = "auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookie refreshTokenCookie;

    @Operation(summary = "회원가입", description = "새로운 유저를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<SignupResponse>> signup(@RequestBody @Valid SignupRequest request, HttpServletResponse servletResponse) {

        SignupResponse response = authService.signUp(request);

        String refreshToken = response.getTokenResponse().getRefreshToken().substring(7).trim();

        refreshTokenCookie.setRefreshTokenCookie(servletResponse, refreshToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success("회원가입을 완료했습니다.", response));
    }


    @Operation(summary = "로그인", description = "인증을 위한 로그인을 진행합니다.")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request, HttpServletResponse servletResponse) {

        TokenResponse response = authService.login(request);

        String refreshToken = response.getRefreshToken().substring(7).trim();

        refreshTokenCookie.setRefreshTokenCookie(servletResponse, refreshToken);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("로그인을 완료했습니다.", response));
    }


    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<String>> logout(@RequestHeader("Authorization") String bearerToken, HttpServletResponse servletResponse) {

        String accessToken = bearerToken.substring(7).trim(); // 순수 토큰 추출

        String redirectUrl = authService.logout(accessToken);

        refreshTokenCookie.deleteRefreshTokenCookie(servletResponse);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("로그아웃을 완료했습니다.", redirectUrl));
    }
}
