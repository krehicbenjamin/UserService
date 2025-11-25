package com.user.UserService.user.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}

