package com.example.aigument.common.security.oauth2.converter;

import com.example.aigument.domain.auth.dto.AuthUser;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {

        Long id = Long.valueOf(jwt.getSubject());
        String email = jwt.getClaim("email");
        String username = jwt.getClaim("username");
        String role =  jwt.getClaim("role");

        AuthUser authUser = new AuthUser(id, username, email, role);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        // 인증 객체 반환
        return new UsernamePasswordAuthenticationToken(
                authUser,
                null,
                authorities
        );
    }
}
