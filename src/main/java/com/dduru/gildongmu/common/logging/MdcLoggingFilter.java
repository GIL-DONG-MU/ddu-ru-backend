package com.dduru.gildongmu.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MdcLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String userAgent = truncateUserAgent(request.getHeader(USER_AGENT_HEADER));

        try {
            String requestId = resolveRequestId(request);
            response.setHeader(REQUEST_ID_HEADER, requestId);

            MDC.put("requestId", requestId);
            MDC.put("method", method);
            MDC.put("uri", uri);
            MDC.put("clientIp", resolveClientIp(request));
            MDC.put("userAgent", userAgent);
            MDC.put("userId", "-");

            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.info("ACCESS - method: {}, uri: {}, status: {}, elapsedMs: {}, ua: {}",
                    method, uri, response.getStatus(), elapsedMs, userAgent);
            MDC.clear();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestIdFromHeader = request.getHeader(REQUEST_ID_HEADER);
        if (StringUtils.hasText(requestIdFromHeader)) {
            return requestIdFromHeader;
        }
        return UUID.randomUUID().toString();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncateUserAgent(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "-";
        }
        int maxLength = 100;
        if (userAgent.length() <= maxLength) {
            return userAgent;
        }
        return userAgent.substring(0, maxLength);
    }
}
