package com.example.api.module.auth.service;

import com.example.api.module.auth.controller.request.LoginRequestBody;
import com.example.api.module.auth.controller.request.SignupRequestBody;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.cart.api.CartApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.domain.user.meta.Status;
import com.example.core.exception.BadRequestException;
import com.example.core.model.AuthResponse;
import com.example.core.utils.JwtUtil;
import com.example.core.utils.MessageUtil;
import com.example.core.utils.SaltedHashUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserApiRepository userApiRepository;
    private final CartApiRepository cartApiRepository;

    private final JwtUtil jwtUtil;
    private final MessageUtil messageUtil;
    private final SaltedHashUtil saltedHashUtil;

    @Transactional
    public AuthResponse signup(SignupRequestBody body) {
        if (userApiRepository.existsUserByEmail(body.getEmail())) {
            throw new BadRequestException(messageUtil.getMessage("auth.email.DUPLICATE"));
        }

        String salt = saltedHashUtil.generateSalt();
        String hashedPassword = saltedHashUtil.hashPassword(body.getPassword(), salt);

        User user = User.of(body.getEmail(), body.getNickname(), salt, hashedPassword, body.getPhoneNumber());

        userApiRepository.save(user);
        cartApiRepository.save(Cart.of(user.getId()));

        return AuthResponse.from(user);
    }

    @Transactional
    public AuthResponse login(HttpServletResponse response, LoginRequestBody body) {
        User user = userApiRepository.findByEmail(body.getEmail())
                .orElseThrow(() -> new BadRequestException(messageUtil.getMessage("auth.user.email.NOTFOUND")));

        // if user is not active
        Status userStatus = user.getStatus();
        if (userStatus != Status.ACTIVE) {
            throw new BadRequestException(messageUtil.getMessage("auth.user.INACTIVE", userStatus));
        }

        // if password is incorrect
        if (!saltedHashUtil.verifyPassword(body.getPassword(), user.getSalt(), user.getHashedPassword())) {
            throw new BadRequestException(messageUtil.getMessage("auth.password.INCORRECT"));
        }

        Long userId = user.getId();
        List<String> roles = getUserRoles(userId);
        setNewCookies(response, userId, roles);

        return AuthResponse.from(user);
    }

    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.extractRefreshTokenFromCookie(request);

        if (!StringUtils.hasText(refreshToken)) {
            throw new BadCredentialsException(messageUtil.getMessage("jwt.EMPTY_TOKEN"));
        }

        // 토큰이 잘못된 경우
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException(messageUtil.getMessage("jwt.INVALID_TOKEN"));
        }

        Claims claims = jwtUtil.extractRefreshClaims(refreshToken);

        // 토큰 타입이 refresh가 아닌 경우
        if (!claims.get("type", String.class).equals("refresh")) {
            throw new BadCredentialsException(messageUtil.getMessage("jwt.WRONG_TYPE_TOKEN"));
        }

        Long userId = claims.get("userId", Long.class);

        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(messageUtil.getMessage("auth.user.email.NOTFOUND")));
        Status userStatus = user.getStatus();
        if (userStatus != Status.ACTIVE) {
            throw new BadRequestException(messageUtil.getMessage("auth.user.INACTIVE", userStatus));
        }

        List<String> roles = getUserRoles(userId);
        setNewCookies(response, userId, roles);

        return AuthResponse.from(user);
    }

    private void setNewCookies(HttpServletResponse response, Long userId, List<String> roles) {
        String accessToken = jwtUtil.createAccessToken(userId, roles);
        String refreshToken = jwtUtil.createRefreshToken(userId, roles);
        jwtUtil.setTokenCookies(response, accessToken, refreshToken);
    }


    // 임시로 ROLE_USER로 설정
    private List<String> getUserRoles(Long userId) {
        return List.of("ROLE_USER");
    }
}
