package com.user.UserService.user.domain.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String email,
        String fullName,
        Instant registeredAt
) {
    public static UserRegisteredEvent of(UUID userId, String email, String fullName) {
        return new UserRegisteredEvent(userId, email, fullName, Instant.now());
    }
}

