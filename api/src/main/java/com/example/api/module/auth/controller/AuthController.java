package com.example.api.module.auth.controller;

import com.example.api.module.auth.controller.request.LoginReqBody;
import com.example.api.module.auth.controller.request.SignupReqBody;
import com.example.api.module.auth.service.AuthService;
import com.example.core.model.AuthRes;
import com.example.core.model.response.DataResponse;
import com.example.core.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public DataResponse<AuthRes> emailSignup(@Valid @RequestBody SignupReqBody body) {
        return DataResponse.of(authService.signup(body));
    }

    @PostMapping("/login")
    public DataResponse<AuthRes> emailLogin(@Valid @RequestBody LoginReqBody body) {
        return DataResponse.of(authService.login(body));
    }

    @PostMapping("/refresh")
    public DataResponse<AuthRes> refreshToken(HttpServletRequest request) {
        return DataResponse.of(authService.refreshToken(request));
    }
}
