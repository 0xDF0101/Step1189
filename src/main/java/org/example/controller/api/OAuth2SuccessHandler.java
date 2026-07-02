package org.example.controller.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.model.Role;
import org.example.repository.UserRepository;
import org.example.service.auth.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * OAuth로 로그인시 곧바로 실행됨
 * (Security Config 에서 설정함)
 *
 * OAuth로 로그인하면 username이 설정되지 않기 때문에
 * 바로 입력받을 수 있도록 함
 */

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        var user = principal.getUser();
        String targetUrl;
        if (user.getUsername().startsWith("TEMP_")) {
            targetUrl = "/signup/set-username";
        } else if (user.getDisplayName() == null) {
            targetUrl = "/signup/set-display-name";
        } else {
            targetUrl = "/main";
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }
}
