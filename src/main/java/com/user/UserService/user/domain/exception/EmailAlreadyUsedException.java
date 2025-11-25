package com.user.UserService.user.domain.exception;

public class EmailAlreadyUsedException extends DomainException {

    public EmailAlreadyUsedException(String email) {
        super("Email already in use: " + email, "EMAIL_ALREADY_USED");
    }
}

