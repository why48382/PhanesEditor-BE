package org.example.coding_convention.config.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coding_convention.user.model.UserDto;
import org.example.coding_convention.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${login.success.uri}")
    private String loginSuccessUri;
    @Value("${cookie.secure}")
    private boolean cookieSecure;
    @Value("${cookie.same-site}")
    private String cookieSameSite;
    @Value("${cookie.domain}")
    private String cookieDomain;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("LoginFilter 성공 로직.");
        UserDto.AuthUser authUser = (UserDto.AuthUser) authentication.getPrincipal();

        String jwt = JwtUtil.generateToken(authUser.getEmail(), authUser.getIdx(), authUser.getNickname());

        if (jwt != null) {
            StringBuilder cookieString = new StringBuilder()
                    .append("access_token=").append(jwt)
                    .append("; Path=/; HttpOnly; SameSite=").append(cookieSameSite)
                    .append("; Max-Age=").append(60 * 60 * 24 * 7);
            if (cookieSecure) {
                cookieString.append("; Secure");
            }
            if (cookieDomain != null && !cookieDomain.isBlank()) {
                cookieString.append("; Domain=").append(cookieDomain);
            }
            response.addHeader("Set-Cookie", cookieString.toString());
            response.sendRedirect(loginSuccessUri);
        }
    }
}
