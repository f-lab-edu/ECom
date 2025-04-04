package com.example.core.model;

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
    private Long userId;
    private String userEmail;
    private String userNickname;
    private Status userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AuthResponse from(User user) {
        return AuthResponse.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userNickname(user.getNickname())
                .userStatus(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getModifiedAt())
                .build();
    }
}