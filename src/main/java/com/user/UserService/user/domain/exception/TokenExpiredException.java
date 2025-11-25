package com.user.UserService.user.domain.exception;

public class TokenExpiredException extends DomainException {

    public TokenExpiredException() {
        super("Token has expired", "TOKEN_EXPIRED");
    }
}

