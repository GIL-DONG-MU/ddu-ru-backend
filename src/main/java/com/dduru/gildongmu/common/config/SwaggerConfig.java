package com.dduru.gildongmu.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private static final String SCHEME_NAME = "JWT";
    private final Environment environment;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(getInfo())
                .servers(getServers())
                .addSecurityItem(getSecurityRequirement())
                .components(getComponents());
    }

    private List<Server> getServers() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("로컬 개발 서버");

        Server prodServer = new Server()
                .url("https://api.dduru.app")
                .description("프로덕션 서버");

        // 현재 활성 프로파일에 따라 기본 서버 설정
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isProd = Arrays.asList(activeProfiles).contains("prod");

        if (isProd) {
            return List.of(prodServer, localServer);
        } else {
            return List.of(localServer, prodServer);
        }
    }

    private Info getInfo() {
        return new Info()
                .title("Ddu-ru Backend API")
                .description("Ddu-ru REST API 문서")
                .version("1.0.0")
                .contact(new Contact()
                        .name("길동무 팀")
                        .url("https://github.com/GIL-DONG-MU")
                        .email("gildongmu.team@gmail.com"));
    }

    private SecurityRequirement getSecurityRequirement() {
        return new SecurityRequirement().addList(SCHEME_NAME);
    }

    private Components getComponents() {
        return new Components()
                .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                        .name(SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat(SCHEME_NAME)
                        .in(SecurityScheme.In.HEADER)
                        .description("Access Token (ex. Bearer ...)"));
    }
}
