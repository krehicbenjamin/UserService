package com.user.UserService.user.domain.value;

public enum Role {
    USER,
    ADMIN;

    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        try {
            return Role.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
}

