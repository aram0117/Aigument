package com.example.aigument.common.security.provider;

import com.example.aigument.common.enums.UserRole;
import com.example.aigument.common.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
@Getter
@RequiredArgsConstructor
public class JwtProvider {

    private final static String TOKEN_PREFIX = "Bearer ";

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {

        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long id, String username, String email, UserRole role, String provider,long expirationTime) {

        Date now = new Date();

        return TOKEN_PREFIX + Jwts.builder()
                .subject(id.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("role", role.toString())
                .claim("provider", provider)
                .signWith(secretKey, Jwts.SIG.HS256)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .compact();
    }

    public Claims getClaims(String accessToken) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(accessToken) // 넘겨 받은 토큰 정보 추출
                .getPayload();
    }
}
