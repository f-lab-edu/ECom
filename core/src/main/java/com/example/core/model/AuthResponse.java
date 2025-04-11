package com.example.core.model;

import com.example.core.domain.admin.Admin;
import com.example.core.domain.user.User;
import com.example.core.domain.user.meta.Status;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@ToString
public class AuthResponse implements Serializable {
    private Long id;
    private String email;
    private String nickname;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AuthResponse from(User user) {
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getModifiedAt())
                .build();
    }

    public static AuthResponse from(Admin admin) {
        return AuthResponse.builder()
                .id(admin.getId())
                .email(admin.getEmail())
                .nickname(admin.getNickname())
                .status(admin.getStatus().name())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getModifiedAt())
                .build();
    }
}