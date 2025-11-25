package com.user.UserService.user.domain.event;

import java.time.Instant;
import java.util.UUID;

public record UserLoggedInEvent(
        UUID userId,
        String email,
        String ipAddress,
        String userAgent,
        Instant loggedInAt
) {
    public static UserLoggedInEvent of(UUID userId, String email, String ipAddress, String userAgent) {
        return new UserLoggedInEvent(userId, email, ipAddress, userAgent, Instant.now());
    }
}

