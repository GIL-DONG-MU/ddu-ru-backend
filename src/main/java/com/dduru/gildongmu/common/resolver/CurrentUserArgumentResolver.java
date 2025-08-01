package com.dduru.gildongmu.common.resolver;

import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.auth.exception.UnauthorizedException;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증 실패: 인증 정보 없음 또는 비활성 사용자. [principal: {}]", authentication);
            throw new UnauthorizedException();
        }

        try {
            return Long.parseLong(authentication.getPrincipal().toString());
        } catch (NumberFormatException e) {
            log.warn("토큰 파싱 실패: principal 값({}) → NumberFormatException, details: {}", authentication.getPrincipal(), e.getMessage());
            throw new InvalidTokenException();
        }
    }
}
