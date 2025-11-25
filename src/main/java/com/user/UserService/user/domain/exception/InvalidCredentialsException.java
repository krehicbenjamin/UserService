package com.user.UserService.user.domain.exception;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid email or password", "INVALID_CREDENTIALS");
    }

    public InvalidCredentialsException(String message) {
        super(message, "INVALID_CREDENTIALS");
    }
}

