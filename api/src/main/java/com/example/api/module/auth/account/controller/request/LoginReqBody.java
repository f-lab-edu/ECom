package com.example.api.module.auth.account.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestBody {

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    private String email;

    @NotNull
    private String password;

}
