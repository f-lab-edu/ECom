package com.example.core.config.security;

import com.example.core.utils.JwtUtil;
import com.example.core.utils.MessageUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MessageUtil messageUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // refresh token 요청은 필터링 하지 않음
        if (request.getRequestURI().equals("/api/v1/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.extractAccessTokenFromCookie(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.extractAccessClaims(token);
            if (!claims.get("type", String.class).equals("access") ||
                    !jwtUtil.validateAccessToken(token)) {
                throw new BadCredentialsException(messageUtil.getMessage("jwt.INVALID_TOKEN"));
            }

            Long userId = claims.get("userId", Long.class);
            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        // access token 만료 시
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token", e);
            throw new BadCredentialsException(messageUtil.getMessage("jwt.EXPIRED_TOKEN"));
        // access token 유효하지 않음
        } catch (UnsupportedJwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token", e);
            throw new BadCredentialsException(messageUtil.getMessage("jwt.INVALID_TOKEN"));
        }

        filterChain.doFilter(request, response);
    }
}
