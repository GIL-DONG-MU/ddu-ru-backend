package com.dduru.gildongmu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SCHEME_NAME = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(getInfo())
                .servers(getServers())
                .addSecurityItem(getSecurityRequirement())
                .components(getComponents());
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

    private List<Server> getServers() {
        return List.of(
                new Server().url("http://localhost:8080").description("개발 환경"),
                new Server().url("https://dduru.app").description("운영 환경")
        );
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
