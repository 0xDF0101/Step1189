package org.example.utility.resolver;

import org.example.dto.user.CustomUserDetails;
import org.example.utility.annotation.LoginUser;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 LoginUser 어노테이션이 붙어있는지 확인
        boolean hasLoginUserAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        // 파라미터의 타입이 userId의 타입과 맞는지 확인
        boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType());
        return hasLoginUserAnnotation && isLongType;
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) throws Exception {

        // 시큐리티 컨텍스트에서 유저 정보를 꺼내옴
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증이 안된 경우
        if(auth == null || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            return null;
        }

        return user.getUserId(); // 필요한 정보만 쏙 빼옴
    }
}
