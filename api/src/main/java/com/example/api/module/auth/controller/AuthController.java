package com.example.api.module.auth.controller;

import com.example.api.module.auth.controller.request.LoginRequestBody;
import com.example.api.module.auth.controller.request.SignupRequestBody;
import com.example.api.module.auth.service.AuthService;
import com.example.core.model.AuthResponse;
import com.example.core.model.response.DataResponse;
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
    public DataResponse<AuthResponse> emailSignup(@Valid @RequestBody SignupRequestBody body) {
        return DataResponse.of(authService.signup(body));
    }

    @PostMapping("/login")
    public DataResponse<AuthResponse> emailLogin(@Valid @RequestBody LoginRequestBody body) {
        return DataResponse.of(authService.login(body));
    }

    @PostMapping("/refresh")
    public DataResponse<AuthResponse> refreshToken(HttpServletRequest request) {
        return DataResponse.of(authService.refreshToken(request));
    }
}
