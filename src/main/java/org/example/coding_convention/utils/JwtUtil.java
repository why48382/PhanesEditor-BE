package org.example.coding_convention.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET = "abcdeffghijklmnopqrstuvwxyz0123456";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final Long EXP = 1000 * 60 * 240L;

    public static String generateToken(String email, Integer idx, String nickname) {

        Map<String, String> claims = new HashMap<>();
        claims.put("idx", "" + idx);
        claims.put("email", email);
        claims.put("nickname", nickname);
        claims.put("role", "USER");

        return Jwts.builder()
                .setClaims(claims)
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
        String value = (String) claims.get(key);

        return value;
    }

    public static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

