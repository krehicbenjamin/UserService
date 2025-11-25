package com.user.UserService.user.web.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Set<String> roles,
        Instant createdAt
) {
}

