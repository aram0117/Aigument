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
        // @AuthenticationPrincipal이 붙은 파라미터(AuthUser)는 요청 바디로 문서화되지 않도록 무시
        SpringDocUtils.getConfig().addAnnotationsToIgnore(org.springframework.security.core.annotation.AuthenticationPrincipal.class);
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