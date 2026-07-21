package org.example.coding_convention.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coding_convention.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.example.coding_convention.user.model.UserDto;

import java.io.IOException;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final boolean cookieSecure;
    private final String cookieSameSite;
    private final String cookieDomain;

    public LoginFilter(AuthenticationManager authenticationManager, boolean cookieSecure, String cookieSameSite, String cookieDomain) {
        this.authenticationManager = authenticationManager;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
        this.cookieDomain = cookieDomain;
    }

    // 원래는 form-data 형식으로 사용자 정보를 입력받았는데
    // 우리는 JSON 형태로 입력을 받기 위해서 재정의
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authToken;
        try {
            log.debug("LoginFilter 실행됐다.");

            UserDto.Login dto = new ObjectMapper().readValue(request.getInputStream(), UserDto.Login.class);
            authToken = new UsernamePasswordAuthenticationToken(
                    dto.getEmail(), dto.getPassword()
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("LoginFilter 성공 로직.");
        UserDto.AuthUser authUser = (UserDto.AuthUser) authResult.getPrincipal();

        String jwt = JwtUtil.generateToken(authUser.getEmail(), authUser.getIdx(), authUser.getNickname());

        if (jwt != null) {
            StringBuilder cookieString = new StringBuilder()
                    .append("access_token=").append(jwt)
                    .append("; Path=/; HttpOnly; SameSite=").append(cookieSameSite);
            if (cookieSecure) {
                cookieString.append("; Secure");
            }
            if (cookieDomain != null && !cookieDomain.isBlank()) {
                cookieString.append("; Domain=").append(cookieDomain);
            }
            response.addHeader("Set-Cookie", cookieString.toString());

            response.getWriter().write(new ObjectMapper().writeValueAsString(UserDto.LoginRes.from(authUser)));
        }
    }

}

