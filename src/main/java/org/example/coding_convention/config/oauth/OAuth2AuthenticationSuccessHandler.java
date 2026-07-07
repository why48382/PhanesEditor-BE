package org.example.coding_convention.config.oauth;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coding_convention.user.model.UserDto;
import org.example.coding_convention.utils.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("LoginFilter 성공 로직.");
        UserDto.AuthUser authUser = (UserDto.AuthUser) authentication.getPrincipal();

        String jwt = JwtUtil.generateToken(authUser.getEmail(), authUser.getIdx(), authUser.getNickname());

        if (jwt != null) {
            String cookieValue = String.format(
                    "access_token=%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=%d",
                    jwt, 60 * 60 * 24
            );
            response.addHeader("Set-Cookie", cookieValue);
            // 소셜 로그인 성공 후 프론트의 특정 경로로 이동
            response.sendRedirect("http://localhost:80/auth/success");
        }
    }
}
