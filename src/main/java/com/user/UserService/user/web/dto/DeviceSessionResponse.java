package com.user.UserService.user.web.dto;

import java.time.Instant;
import java.util.UUID;

public record DeviceSessionResponse(
        UUID id,
        String deviceName,
        String os,
        String ipAddress,
        Instant lastUsedAt,
        Instant createdAt
) {
}

