package org.example.coding_convention.config.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.coding_convention.user.model.UserDto;
import org.example.coding_convention.utils.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // ✅ Swagger 관련은 그냥 통과 //
        if(path.startsWith("/swagger-ui")
                ||path.startsWith("/v3/api-docs")
                ||path.startsWith("/swagger-resources")
                ||path.startsWith("/webjars"))

        {
            filterChain.doFilter(request, response);
            return;
        }



        Cookie[] cookies = request.getCookies();
        String jwt = null;
        if (cookies != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("access_token")) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }
        // ✅ 2. Authorization 헤더에서 토큰 찾기 (쿠키 없을 때)
        if (jwt == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7); // "Bearer " 이후 토큰 부분
            }
        }

        if (jwt != null) {
            Claims claims = JwtUtil.getClaims(jwt);
            if (claims != null) {
                String email = JwtUtil.getValue(claims, "email");
                Integer idx = Integer.parseInt(JwtUtil.getValue(claims, "idx"));
                String nickname = JwtUtil.getValue(claims, "nickname");

                UserDto.AuthUser authUser = UserDto.AuthUser.builder()
                        .idx(idx)
                        .email(email)
                        .nickname(nickname)
                        .build();
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")) // 특정 권한 부여, 권한 앞에 ROLE_를 붙여야 함
                );
                // 컨텍스트라는 공간에 인증된 사용자 정보 authentication를 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}

