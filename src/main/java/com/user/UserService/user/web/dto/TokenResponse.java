package com.user.UserService.user.web.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}

