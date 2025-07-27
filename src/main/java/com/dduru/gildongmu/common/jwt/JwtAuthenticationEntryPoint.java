package com.dduru.gildongmu.common.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = (String)request.getAttribute("exceptionMessage");
        if (message == null) message = "토큰 인증이 실패했습니다.";

        response.getWriter().write("{\"status\":401,\"code\":\"AUTH_001\",\"message\":\"" + message + "\"}");
    }
}
