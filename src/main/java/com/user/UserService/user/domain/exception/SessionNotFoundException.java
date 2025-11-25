package com.user.UserService.user.domain.exception;

public class SessionNotFoundException extends DomainException {

    public SessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId, "SESSION_NOT_FOUND");
    }
}

