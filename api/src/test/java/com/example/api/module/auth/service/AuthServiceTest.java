package com.example.api.module.auth.service;

import com.example.api.module.auth.controller.request.LoginRequestBody;
import com.example.api.module.auth.controller.request.SignupRequestBody;
import com.example.core.domain.cart.api.CartApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.domain.user.meta.Status;
import com.example.core.exception.BadRequestException;
import com.example.core.model.AuthResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"api-test","core-test"})
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserApiRepository userApiRepository;

    private String email = "testUser_123@example.com";
    private String nickname = "abc";
    private String password = "Ab1!2345";
    private String phoneNumber = "010-1234-5678";
    @Autowired
    private CartApiRepository cartApiRepository;


    @BeforeEach
    void cleanup() {
        userApiRepository.deleteAll();
        cartApiRepository.deleteAll();
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
        signupRequestBody.setPassword(password);
        signupRequestBody.setPhoneNumber(phoneNumber);
        signupRequestBody.setNickname(nickname);
        authService.signup(signupRequestBody);

        LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.setEmail(email);
        loginRequestBody.setPassword(password);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        AuthResponse authResponse = authService.login(response, loginRequestBody);


        // then
        assertEquals(authResponse.getEmail(), email);
        assertEquals(authResponse.getNickname(), nickname);
        assertEquals(authResponse.getStatus(), "ACTIVE");

        Cookie accessCookie = response.getCookie("accessToken");
        Cookie refreshCookie = response.getCookie("refreshToken");
        assertNotNull(accessCookie);
        assertNotNull(refreshCookie);
        assertEquals("accessToken", accessCookie.getName());
        assertEquals("refreshToken", refreshCookie.getName());
    }

    @Test
    void login_실패_존재하지_않는_이메일() {
        // given
        LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.setEmail(email);
        loginRequestBody.setPassword(password);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when

        // then
        assertThrows(BadRequestException.class, () -> authService.login(response, loginRequestBody));
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

        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = userApiRepository.findByEmail(email).orElseThrow();
        user.setStatus(Status.BANNED);
        userApiRepository.save(user);

        LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.setEmail(email);
        loginRequestBody.setPassword(password);
        // when

        // then
        assertThrows(BadRequestException.class, () -> authService.login(response, loginRequestBody));
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

        MockHttpServletResponse response = new MockHttpServletResponse();

        LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.setEmail(email);
        loginRequestBody.setPassword(password + "1");

        // when

        // then
        assertThrows(BadRequestException.class, () -> authService.login(response,loginRequestBody));
    }
}