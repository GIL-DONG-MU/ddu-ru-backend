package com.dduru.gildongmu.common.resolver;

import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.auth.exception.UnauthorizedException;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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
            throw new UnauthorizedException();
        }

        Object principal = authentication.getPrincipal();

        if ("anonymousUser".equals(principal)) {
            return null;
        }

        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            throw new InvalidTokenException();
        }
    }
}
