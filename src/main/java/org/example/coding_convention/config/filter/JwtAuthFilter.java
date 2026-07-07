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
                System.out.println(cookie.getName());
                if (cookie.getName().equals("access_token")) {
                    jwt = cookie.getValue();
                    System.out.println(2);
                    break;
                }
            }
        }
        // ✅ 2. Authorization 헤더에서 토큰 찾기 (쿠키 없을 때)
        if (jwt == null) {
            System.out.println(3);
            String authHeader = request.getHeader("Authorization");
            System.out.println(4);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                System.out.println(5);
                jwt = authHeader.substring(7); // "Bearer " 이후 토큰 부분
                System.out.println(6);
            }
        }

        if (jwt != null) {
            System.out.println(7);
            Claims claims = JwtUtil.getClaims(jwt);
            System.out.println(8);
            if (claims != null) {
                String email = JwtUtil.getValue(claims, "email");
                System.out.println(9);
                Integer idx = Integer.parseInt(JwtUtil.getValue(claims, "idx"));
                System.out.println(10);
                String nickname = JwtUtil.getValue(claims, "nickname");
                System.out.println(11);

                UserDto.AuthUser authUser = UserDto.AuthUser.builder()
                        .idx(idx)
                        .email(email)
                        .nickname(nickname)
                        .build();
                System.out.println(12);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")) // 특정 권한 부여, 권한 앞에 ROLE_를 붙여야 함
                );
                System.out.println(13);
                // 컨텍스트라는 공간에 인증된 사용자 정보 authentication를 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println(14);

            }
        }

        filterChain.doFilter(request, response);
    }
}

