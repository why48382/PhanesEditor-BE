package org.example.coding_convention.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
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

    public static void deleteToken(HttpServletResponse response, boolean cookieSecure, String cookieSameSite, String cookieDomain) {
        StringBuilder cookieString = new StringBuilder()
                .append("access_token=; Path=/; HttpOnly; SameSite=").append(cookieSameSite)
                .append("; Max-Age=0");
        if (cookieSecure) {
            cookieString.append("; Secure");
        }
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            cookieString.append("; Domain=").append(cookieDomain);
        }
        response.addHeader("Set-Cookie", cookieString.toString());
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

