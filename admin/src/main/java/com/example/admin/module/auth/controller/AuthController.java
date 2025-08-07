package com.example.admin.module.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.admin.module.auth.controller.request.LoginReqBody;
import com.example.admin.module.auth.controller.request.SignupRequestBody;
import com.example.admin.module.auth.service.AuthService;
import com.example.core.model.AuthResponse;
import com.example.core.model.response.DataResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/admins")
    public DataResponse<AuthResponse> createAdmin(@Valid @RequestBody SignupRequestBody body) {
        return DataResponse.of(authService.createAdmin(body));
    }

    @PostMapping("/login")
    public DataResponse<AuthResponse> loginAdmin(@Valid @RequestBody LoginReqBody body,
                                                 HttpServletResponse response) {
        return DataResponse.of(authService.login(response, body));
    }

    @PostMapping("/refresh")
    public DataResponse<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return DataResponse.of(authService.refreshToken(request, response));
    }

    
}
