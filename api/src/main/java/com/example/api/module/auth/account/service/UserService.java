package com.example.api.module.auth.account.service;

import com.example.api.core.event.UserCreateEvent;
import com.example.api.module.auth.account.controller.request.LoginReqBody;
import com.example.api.module.auth.account.controller.request.SignupReqBody;
import com.example.core.config.security.JwtTokenizer;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.cart.api.CartApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.domain.user.meta.Status;
import com.example.core.exception.BadRequestException;
import com.example.core.model.AuthRes;
import com.example.core.utils.SaltedHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserApiRepository userApiRepository;
    private final CartApiRepository cartApiRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final JwtTokenizer jwtTokenizer;
    private final SaltedHashUtil saltedHashUtil;

    @Transactional
    public AuthRes signup(SignupReqBody body) {
        if (userApiRepository.existsUserByEmail(body.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        String salt = saltedHashUtil.generateSalt();
        String hashedPassword = saltedHashUtil.hashPassword(body.getPassword(), salt);

        User user = User.of(body.getEmail(), body.getNickname(), salt, hashedPassword, body.getPhoneNumber());
        userApiRepository.save(user);

        eventPublisher.publishEvent(UserCreateEvent.of(user.getId()));

        return createLoginRes(user);
    }

    @Transactional(readOnly = true)
    public AuthRes login(LoginReqBody body) {
        Optional<User> userOptional = userApiRepository.findByEmail(body.getEmail());
        if (userOptional.isEmpty()) throw new BadRequestException("User not found");

        if (userOptional.get().getStatus() != Status.ACTIVE) {
            throw new BadRequestException("User is " + userOptional.get().getStatus());
        }

        if (!saltedHashUtil.verifyPassword(body.getPassword(), userOptional.get().getSalt(), userOptional.get().getHashedPassword())) {
            throw new BadRequestException("Password is incorrect");
        }
        return createLoginRes(userOptional.get());
    }

    @Transactional
    public AuthRes createLoginRes(User user) {
        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), new ArrayList<>());
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getEmail(), new ArrayList<>());

        userApiRepository.save(user);

        return AuthRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @EventListener
    @Transactional
    @Async
    public void whenUserCreated(final UserCreateEvent event) {
        cartApiRepository.save(Cart.of(event.getUserId()));
    }
}
