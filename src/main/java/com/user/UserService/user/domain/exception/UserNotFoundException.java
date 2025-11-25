package com.user.UserService.user.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }

    public static UserNotFoundException byId(String id) {
        return new UserNotFoundException("User not found with id: " + id);
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("User not found with email: " + email);
    }
}

