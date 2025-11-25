package com.user.UserService.user.domain.exception;

public class TokenRevokedException extends DomainException {

    public TokenRevokedException() {
        super("Token has been revoked", "TOKEN_REVOKED");
    }
}

