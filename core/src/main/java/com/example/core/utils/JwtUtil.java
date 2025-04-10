package com.example.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {
    //Access Token
    @Value("${jwt.token.access.expire-second}")
    private int ACCEWSS_TOKEN_EXPIRE_COUNT;

    @Value("${jwt.token.access.access-secret}")
    private String ACCESS_TOKEN_SECRET;

    //Refresh Token
    @Value("${jwt.token.refresh.expire-second}")
    private int REFRESH_TOKEN_EXPIRE_COUNT;

    @Value("${jwt.token.refresh.refresh-secret}")
    private String REFRESH_TOKEN_SECRET;


    public String createAccessToken(Long userId, List<String> roles) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + ACCEWSS_TOKEN_EXPIRE_COUNT);

        return Jwts.builder()
                .setSubject("AccessToken")
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(getSecretKey(ACCESS_TOKEN_SECRET), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId, List<String> roles) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_COUNT);

        return Jwts.builder()
                .setSubject("RefreshToken")
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(getSecretKey(REFRESH_TOKEN_SECRET), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validate(token, ACCESS_TOKEN_SECRET);
    }

    public boolean validateRefreshToken(String token) {
        return validate(token, REFRESH_TOKEN_SECRET);
    }

    private boolean validate(String token, String secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey(secret))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid Token: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractAccessClaims(String token) {
        return extractClaims(token, ACCESS_TOKEN_SECRET);
    }

    public Claims extractRefreshClaims(String token) {
        return extractClaims(token, REFRESH_TOKEN_SECRET);
    }

    private Claims extractClaims(String token, String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Token Secret Key: byte[] 기반
    private SecretKey getSecretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie: request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie: request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void setTokenCookies(HttpServletResponse response,
                                String accessToken,
                                String refreshToken) {
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(ACCEWSS_TOKEN_EXPIRE_COUNT / 1000);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setPath("/api/v1/auth/refresh");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setMaxAge(REFRESH_TOKEN_EXPIRE_COUNT / 1000);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }
}
