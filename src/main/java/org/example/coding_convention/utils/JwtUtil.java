package org.example.coding_convention.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    private static Key KEY;
    private static final Long EXP = 1000 * 60 * 60 * 24 * 7L;

    @PostConstruct
    public void init() {
        KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public static String generateToken(String email, Integer idx, String nickname) {

        return Jwts.builder()
                .claim("idx", "" + idx)
                .claim("email", email)
                .claim("nickname", nickname)
                .claim("role", "USER")
                .setExpiration(new Date(System.currentTimeMillis() + EXP))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

//    public static void deleteToken(HttpServletResponse response) {
//
//        Cookie cookie = new Cookie("access_token", null);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(true); // 로그인 때 Secure 줬다면 동일하게
//        cookie.setPath("/");
//        cookie.setMaxAge(0); // 즉시 만료
//        response.addCookie(cookie);
//
//    }

    public static void deleteToken(HttpServletResponse response) {
        String cookieString = "access_token=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0";
        response.addHeader("Set-Cookie", cookieString);
    }


    public static String getValue(Claims claims, String key) {
        return (String) claims.get(key);
    }

    public static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

