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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

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

        return createLoginRes(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequestBody body) {
        Optional<User> userOptional = userApiRepository.findByEmail(body.getEmail());

        // if user not found
        if (userOptional.isEmpty()) {
            throw new BadRequestException(messageUtil.getMessage("auth.user.email.NOTFOUND"));
        }


        // if user is not active
        if (userOptional.get().getStatus() != Status.ACTIVE) {
            throw new BadRequestException(messageUtil.getMessage("auth.user.INACTIVE", userOptional.get().getStatus()));
        }


        // if password is incorrect
        if (!saltedHashUtil.verifyPassword(body.getPassword(), userOptional.get().getSalt(), userOptional.get().getHashedPassword())) {
            throw new BadRequestException(messageUtil.getMessage("auth.password.INCORRECT"));
        }
        return createLoginRes(userOptional.get());
    }



    @Transactional
    public AuthResponse refreshToken(HttpServletRequest requst) {
        String refreshToken = requst.getHeader("Authorization");

        // 토큰이 잘못된 경우
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException(messageUtil.getMessage("jwt.INVALID_TOKEN"));
        }

        Claims claims = jwtUtil.extractRefreshClaims(refreshToken);
        // 토큰 타입이 refresh가 아닌 경우
        if (!claims.get("type", String.class).equals("refresh")) {
            throw new BadCredentialsException(messageUtil.getMessage("jwt.WRONG_TYPE_TOKEN"));
        }

        // Todo: Redis에 저장된 토큰과 일치하지 않거나 만료되어 삭제된 경우

        // user가 존재하지 않는 경우
        Optional<User> userOptional = userApiRepository.findById(claims.get("userId", Long.class));
        if (userOptional.isEmpty()) {
            throw new BadRequestException(messageUtil.getMessage("auth.user.NOTFOUND"));
        }

        return createLoginRes(userOptional.get());
    }

    @Transactional
    public AuthResponse createLoginRes(User user) {
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), new ArrayList<>());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), new ArrayList<>());

        userApiRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
