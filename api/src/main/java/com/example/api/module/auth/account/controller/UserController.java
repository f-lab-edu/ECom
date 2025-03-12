package com.example.api.module.auth.account.controller;

import com.example.api.module.auth.account.controller.request.LoginReqBody;
import com.example.api.module.auth.account.controller.request.SignupReqBody;
import com.example.api.module.auth.account.service.UserService;
import com.example.core.model.AuthRes;
import com.example.core.model.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public DataResponse<AuthRes> emailSignup(@Valid @RequestBody SignupReqBody body) {
        return DataResponse.of(userService.signup(body));
    }

    @PostMapping("/login")
    public DataResponse<AuthRes> emailLogin(@Valid @RequestBody LoginReqBody body) {
        return DataResponse.of(userService.login(body));
    }
}
