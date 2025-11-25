package com.user.UserService.user.domain.exception;

public class WeakPasswordException extends DomainException {

    public WeakPasswordException(String message) {
        super(message, "WEAK_PASSWORD");
    }
}

