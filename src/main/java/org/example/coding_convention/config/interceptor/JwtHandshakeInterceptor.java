package org.example.coding_convention.config.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.coding_convention.user.model.UserDto;
import org.example.coding_convention.utils.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;


// 웹 소켓 연결을 처음할 때 실행
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if(request instanceof ServletServerHttpRequest serverHttpRequest) {
            HttpServletRequest httpReq = serverHttpRequest.getServletRequest();
            Cookie[] cookies = httpReq.getCookies();
            String jwt = null;
            if(cookies != null) {
                for(Cookie cookie: httpReq.getCookies()) {
                    if(cookie.getName().equals("access_token")) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }

            if( jwt != null) {
                Claims claims = JwtUtil.getClaims(jwt);
                if(claims!= null) {
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

                    // 시큐리티 컨텍스트는 HTTP 통신에서 사용되는 저장 공간
                    // 웹 소켓 통신에서는 사용되지 않는다.
                    // SecurityContextHolder.getContext().setAuthentication(authentication);
                    attributes.put("auth", authentication);
                }
            }

        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
