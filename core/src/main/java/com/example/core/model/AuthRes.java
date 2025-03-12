package com.example.core.model;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@ToString
public class AuthRes implements Serializable {

    private String accessToken;

    private String refreshToken;
}