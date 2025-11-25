package com.user.UserService.user.domain.exception;

public class InvalidTokenException extends DomainException {

    public InvalidTokenException() {
        super("Invalid token", "INVALID_TOKEN");
    }

    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}

