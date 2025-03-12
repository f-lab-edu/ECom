package com.example.api.module.auth.account.service;

import com.example.api.module.auth.account.controller.request.LoginReqBody;
import com.example.api.module.auth.account.controller.request.SignupReqBody;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.domain.user.meta.Status;
import com.example.core.exception.BadRequestException;
import com.example.core.model.AuthRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserApiRepository userApiRepository;

    private String email = "test@test.com";
    private String nickname = "test";
    private String password = "test1234";
    private String phoneNumber = "01012345678";


    @BeforeEach
    void cleanup() {
        userApiRepository.deleteAll();
    }

    @Test
    void signup_성공() {
        SignupReqBody signupReqBody = new SignupReqBody();
        signupReqBody.setEmail(email);
        signupReqBody.setNickname(nickname);
        signupReqBody.setPassword(password);
        signupReqBody.setPhoneNumber(phoneNumber);

        // when
        userService.signup(signupReqBody);

        //then
        User savedUser = userApiRepository.findByEmail(email).orElseThrow();
        assertEquals(email, savedUser.getEmail());
        assertEquals(nickname, savedUser.getNickname());
        assertEquals(phoneNumber, savedUser.getPhoneNumber());
    }

    @Test
    void signup_실패_이미_존재하는_이메일() {
        // given
        SignupReqBody signupReqBody = new SignupReqBody();
        signupReqBody.setEmail(email);
        signupReqBody.setNickname(nickname);
        signupReqBody.setPassword(password);
        signupReqBody.setPhoneNumber(phoneNumber);

        // when
        userService.signup(signupReqBody);

        // then
        assertThrows(BadRequestException.class, () -> userService.signup(signupReqBody));
    }

    @Test
    void login_성공() {
        // given
        SignupReqBody signupReqBody = new SignupReqBody();
        signupReqBody.setEmail(email);
        signupReqBody.setNickname(nickname);
        signupReqBody.setPassword(password);
        signupReqBody.setPhoneNumber(phoneNumber);
        userService.signup(signupReqBody);

        LoginReqBody loginReqBody = new LoginReqBody();
        loginReqBody.setEmail(email);
        loginReqBody.setPassword(password);


        // when
        AuthRes authRes = userService.login(loginReqBody);


        // then
        assertNotNull(authRes.getAccessToken());
        assertNotNull(authRes.getRefreshToken());
    }

    @Test
    void login_실패_존재하지_않는_이메일() {
        // given
        LoginReqBody loginReqBody = new LoginReqBody();
        loginReqBody.setEmail(email);
        loginReqBody.setPassword(password);

        // when

        // then
        assertThrows(BadRequestException.class, () -> userService.login(loginReqBody));
    }

    @Test
    void login_실패_유저_NOT_ACITVE() {
        // given
        SignupReqBody signupReqBody = new SignupReqBody();
        signupReqBody.setEmail(email);
        signupReqBody.setNickname(nickname);
        signupReqBody.setPassword(password);
        signupReqBody.setPhoneNumber(phoneNumber);
        userService.signup(signupReqBody);

        User user = userApiRepository.findByEmail(email).orElseThrow();
        user.setStatus(Status.BANNED);
//        user.setStatus(Status.SUSPENDED);
//        user.setStatus(Status.WITHDRAWN);
        userApiRepository.save(user);

        LoginReqBody loginReqBody = new LoginReqBody();
        loginReqBody.setEmail(email);
        loginReqBody.setPassword(password);
        // when

        // then
        assertThrows(BadRequestException.class, () -> userService.login(loginReqBody));
    }

    @Test
    void login_실패_비밀번호_불일치() {
        // given
        SignupReqBody signupReqBody = new SignupReqBody();
        signupReqBody.setEmail(email);
        signupReqBody.setNickname(nickname);
        signupReqBody.setPassword(password);
        signupReqBody.setPhoneNumber(phoneNumber);
        userService.signup(signupReqBody);


        LoginReqBody loginReqBody = new LoginReqBody();
        loginReqBody.setEmail(email);
        loginReqBody.setPassword(password + "1");

        // when

        // then
        assertThrows(BadRequestException.class, () -> userService.login(loginReqBody));
    }
}