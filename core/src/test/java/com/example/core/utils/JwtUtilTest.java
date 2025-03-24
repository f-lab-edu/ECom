package com.example.core.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("core-test")
class JwtUtilTest {

    @Value("${jwt.token.access.expire-second}")
    private int ACCEWSS_TOKEN_EXPIRE_COUNT;

    @Value("${jwt.token.access.access-secret}")
    private String ACCESS_TOKEN_SECRET;

    //Refresh Token
    @Value("${jwt.token.refresh.expire-second}")
    private int REFRESH_TOKEN_EXPIRE_COUNT;

    @Value("${jwt.token.refresh.refresh-secret}")
    private String REFRESH_TOKEN_SECRET;

    @Autowired
    private JwtUtil jwtUtil;

    private Date now = new Date();
    private Date accessExpireTime = new Date(now.getTime() + ACCEWSS_TOKEN_EXPIRE_COUNT);
    private Date refreshExpireTime = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_COUNT);

    private Long userId = 1L;
    private String userEmail = "test@email.com";
    private List<String> roles = List.of("ROLE1", "ROLE2");

    @Test
    public void createAccessToken_AND_extractAccessClaim_Test() {
        //Given
        String accessToken = jwtUtil.createAccessToken(userId, userEmail, roles);

        //When
        Claims claims = jwtUtil.extractAccessClaims(accessToken);

        //Then
        assertEquals(userEmail, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals(roles, claims.get("roles", List.class));
        assertEquals("access", claims.get("type"));
    };

    @Test
    public void createRefreshToken_AND_extractRefreshClaim_Test() {
        //Given
        String refreshToken = jwtUtil.createRefreshToken(userId, userEmail, roles);

        //When
        Claims claims = jwtUtil.extractRefreshClaims(refreshToken);

        //Then
        assertEquals(userEmail, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals(roles, claims.get("roles", List.class));
        assertEquals("refresh", claims.get("type"));
    };

    @Test
    public void validateAccessTokenTest_SUCCESS() {
        //Given
        String accessToken = jwtUtil.createAccessToken(userId, userEmail, roles);

        //When
        boolean result = jwtUtil.validateAccessToken(accessToken);

        //Then
        assertTrue(result);
    }

    @Test
    public void validateAccessTokenTest_FAIL() {
        //Given
        String accessToken = jwtUtil.createAccessToken(userId, userEmail, roles) + "a";

        //When
        boolean result = jwtUtil.validateAccessToken(accessToken);

        //Then
        assertFalse(result);
    }

    @Test
    public void validateRefreshTokenTest_SUCCESS() {
        //Given
        String refreshToken = jwtUtil.createRefreshToken(userId, userEmail, roles);

        //When
        boolean result = jwtUtil.validateRefreshToken(refreshToken);

        //Then
        assertTrue(result);
    }

    @Test
    public void validateRefreshTokenTest_FAIL() {
        //Given
        String refreshToken = jwtUtil.createRefreshToken(userId, userEmail, roles) + "a";

        //When
        boolean result = jwtUtil.validateRefreshToken(refreshToken);

        //Then
        assertFalse(result);
    }


}