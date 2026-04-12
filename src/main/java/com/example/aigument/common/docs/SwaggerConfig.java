package com.example.aigument.common.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    static {
        // 방법 A: @AuthenticationPrincipal 어노테이션 자체를 무시하도록 설정
        SpringDocUtils.getConfig().addAnnotationsToIgnore(org.springframework.security.core.annotation.AuthenticationPrincipal.class);

        // 방법 B: 특정 커스텀 유저 클래스를 무시하도록 설정 (AuthUser 자리에 본인 클래스명 입력)
        // SpringDocUtils.getConfig().addRequestWrapperToIgnore(AuthUser.class);
    }

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );

        return new OpenAPI()
                .components(components)
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .addServersItem(new Server().url("http://localhost").description("Docker Server"));
    }

    private Info apiInfo() {
        return new Info()
                .title("Aigument API Document")
                .description("Spring Boot 3.4.0 기반 프로젝트 API 명세서")
                .version("1.0.0");
    }
}