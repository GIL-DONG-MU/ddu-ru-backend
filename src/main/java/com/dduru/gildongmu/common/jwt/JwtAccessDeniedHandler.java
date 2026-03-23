package com.dduru.gildongmu.common.jwt;

import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("접근 권한 없음 - method: {}, uri: {}", request.getMethod(), request.getRequestURI());
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ErrorResponse body = ErrorResponse.of(ErrorCode.ACCESS_DENIED, "접근 권한이 없습니다.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
