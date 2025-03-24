package com.example.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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


    public String createAccessToken(Long userId, String userEmail, List<String> roles) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + ACCEWSS_TOKEN_EXPIRE_COUNT);
        return Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(getAccessSecretKey(), SignatureAlgorithm.HS256)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", "access")
                .compact();
    }

    public String createRefreshToken(Long userId, String userEmail, List<String> roles) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_COUNT);

        return Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(getRefreshSecretKey(), SignatureAlgorithm.HS256)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", "refresh")
                .compact();
    }

    public boolean validateAccessToken(String accessToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getAccessSecretKey())
                    .build()
                    .parseClaimsJws(accessToken);
            return true;
        } catch (Exception e) {
            log.error("Invalid Access Token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getRefreshSecretKey())
                    .build()
                    .parseClaimsJws(refreshToken);
            return true;
        } catch (Exception e) {
            log.error("Invalid Refresh Token: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractAccessClaims(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(getAccessSecretKey())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }

    public Claims extractRefreshClaims(String refreshToken) {
        return Jwts.parserBuilder()
                .setSigningKey(getRefreshSecretKey())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
    }

    // Access Token Secret Key: byte[] 기반
    private SecretKey getAccessSecretKey() {
        return Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes());
    }

    // Refresh Token Secret Key: byte[] 기반
    private SecretKey getRefreshSecretKey() {
        return Keys.hmacShaKeyFor(REFRESH_TOKEN_SECRET.getBytes());
    }
}
