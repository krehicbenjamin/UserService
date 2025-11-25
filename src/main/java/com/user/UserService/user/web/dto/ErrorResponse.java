package com.user.UserService.user.web.dto;

public record ErrorResponse(
        String error,
        String code,
        int status
) {
}

