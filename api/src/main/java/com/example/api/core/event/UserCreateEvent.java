package com.example.api.core.event;

import lombok.Data;

@Data
public class UserCreateEvent {
    private Long userId;

    public static UserCreateEvent of(Long userId) {
        UserCreateEvent event = new UserCreateEvent();
        event.setUserId(userId);
        return event;
    }
}
