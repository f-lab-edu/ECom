package com.example.api.module.auth.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignupRequestBody {

    //email, nickname, password, phoneNumber

    @NotEmpty(message = "{signup.email.EMPTY}")
    @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "{signup.email.WRONG_FORMAT}")
    private String email;

    @NotEmpty
    private String nickname;

    @NotEmpty(message = "{signup.password.EMPTY}")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,20}$", message = "{signup.email.WRONG_FORMAT}") // 영문, 특수문자 8자 이상 20자 이하
    private String password;

    @NotEmpty
    private String phoneNumber;
}
