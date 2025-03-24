package com.example.api.module.auth.service;

import com.example.api.module.auth.controller.request.LoginRequestBody;
import com.example.api.module.auth.controller.request.SignupRequestBody;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.domain.user.meta.Status;
import com.example.core.exception.BadRequestException;
import com.example.core.model.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("api-test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserApiRepository userApiRepository;

    private String email = "testUser_123@example.com";
    private String nickname = "abc";
    private String password = "Ab1!2345";
    private String phoneNumber = "010-1234-5678";


    @BeforeEach
    void cleanup() {
        userApiRepository.deleteAll();
    }

    @Test
    void signup_성공() {
        SignupRequestBody signupRequestBody = new SignupRequestBody();
        signupRequestBody.setEmail(email);
        signupRequestBody.setNickname(nickname);
        signupRequestBody.setPassword(password);
        signupRequestBody.setPhoneNumber(phoneNumber);

        // when
        authService.signup(signupRequestBody);

        //then
        User savedUser = userApiRepository.findByEmail(email).orElseThrow();
        assertEquals(email, savedUser.getEmail());
        assertEquals(nickname, savedUser.getNickname());
        assertEquals(phoneNumber, savedUser.getPhoneNumber());
    }

    @Test
    void signup_실패_이미_존재하는_이메일() {
        // given
        SignupRequestBody signupRequestBody = new SignupRequestBody();
        signupRequestBody.setEmail(email);
        signupRequestBody.setNickname(nickname);
        signupRequestBody.setPassword(password);
        signupRequestBody.setPhoneNumber(phoneNumber);

        // when
        authService.signup(signupRequestBody);

        // then
        assertThrows(BadRequestException.class, () -> authService.signup(signupRequestBody));
    }

    @Test
    void login_성공() {
        // given
        SignupRequestBody signupRequestBody = new SignupRequestBody();
        signupRequestBody.setEmail(email);
        signupRequestBody.setNickname(nickname);
        signupRequestBody.setPassword(password);
        signupRequestBody.setPhoneNumber(phoneNumber);
        authService.signup(signupRequestBody);

        LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.setEmail(email);
        loginRequestBody.setPassword(password);


        // when
        AuthResponse authResponse = authService.login(loginRequestBody);


        // then
        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
    }

    @Test
    void login_실패_존재하지_않는_이메일() {
        // given
        LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.setEmail(email);
        loginRequestBody.setPassword(password);

        // when

        // then
        assertThrows(BadRequestException.class, () -> authService.login(loginRequestBody));
    }

    @Test
    void login_실패_유저_NOT_ACTIVE() {
        // given
        SignupRequestBody signupRequestBody = new SignupRequestBody();
        signupRequestBody.setEmail(email);
        signupRequestBody.setNickname(nickname);
        signupRequestBody.setPassword(password);
        signupRequestBody.setPhoneNumber(phoneNumber);
        authService.signup(signupRequestBody);

        User user = userApiRepository.findByEmail(email).orElseThrow();
        user.setStatus(Status.BANNED);
//        user.setStatus(Status.SUSPENDED);
//        user.setStatus(Status.WITHDRAWN);
        userApiRepository.save(user);

        LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.setEmail(email);
        loginRequestBody.setPassword(password);
        // when

        // then
        assertThrows(BadRequestException.class, () -> authService.login(loginRequestBody));
    }

    @Test
    void login_실패_비밀번호_불일치() {
        // given
        SignupRequestBody signupRequestBody = new SignupRequestBody();
        signupRequestBody.setEmail(email);
        signupRequestBody.setNickname(nickname);
        signupRequestBody.setPassword(password);
        signupRequestBody.setPhoneNumber(phoneNumber);
        authService.signup(signupRequestBody);


        LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.setEmail(email);
        loginRequestBody.setPassword(password + "1");

        // when

        // then
        assertThrows(BadRequestException.class, () -> authService.login(loginRequestBody));
    }
}