package com.example.core.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;

import jakarta.servlet.http.HttpServletResponse;

@SpringBootTest
@ActiveProfiles("core-test")
class CustomAuthenticationEntryPointTest {

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = new BadCredentialsException("Invalid credentials");
    }

    // =========================== commence 테스트 (중요도: 높음) ===========================

    @Test
    void commence_성공_기본동작() throws IOException {
        // when
        customAuthenticationEntryPoint.commence(request, response, authException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_다양한예외타입() throws IOException {
        // given
        AuthenticationException insufficientAuthException = 
            new InsufficientAuthenticationException("Insufficient authentication");

        // when
        customAuthenticationEntryPoint.commence(request, response, insufficientAuthException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_다양한요청URI() throws IOException {
        // given
        request.setRequestURI("/api/v1/products");
        request.setMethod("GET");

        // when
        customAuthenticationEntryPoint.commence(request, response, authException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_인증헤더포함요청() throws IOException {
        // given
        request.addHeader("Authorization", "Bearer invalid-token");

        // when
        customAuthenticationEntryPoint.commence(request, response, authException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_CORS요청() throws IOException {
        // given
        request.addHeader("Origin", "http://localhost:3000");
        request.addHeader("Access-Control-Request-Method", "GET");

        // when
        customAuthenticationEntryPoint.commence(request, response, authException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_POST요청() throws IOException {
        // given
        request.setMethod("POST");
        request.setRequestURI("/api/v1/orders");
        request.setContentType("application/json");

        // when
        customAuthenticationEntryPoint.commence(request, response, authException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_빈예외메시지() throws IOException {
        // given
        AuthenticationException emptyMessageException = 
            new BadCredentialsException("");

        // when
        customAuthenticationEntryPoint.commence(request, response, emptyMessageException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_null예외메시지() throws IOException {
        // given
        AuthenticationException nullMessageException = 
            new BadCredentialsException(null);

        // when
        customAuthenticationEntryPoint.commence(request, response, nullMessageException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_관리자API요청() throws IOException {
        // given
        request.setRequestURI("/admin/v1/products");
        request.setMethod("POST");

        // when
        customAuthenticationEntryPoint.commence(request, response, authException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_긴예외메시지() throws IOException {
        // given
        String longMessage = "A very long authentication exception message that contains detailed information about what went wrong during the authentication process";
        AuthenticationException longMessageException = 
            new BadCredentialsException(longMessage);

        // when
        customAuthenticationEntryPoint.commence(request, response, longMessageException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    void commence_성공_특수문자포함예외() throws IOException {
        // given
        AuthenticationException specialCharException = 
            new BadCredentialsException("Special chars: !@#$%^&*()");

        // when
        customAuthenticationEntryPoint.commence(request, response, specialCharException);

        // then
        assertEquals("application/json", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }
} 