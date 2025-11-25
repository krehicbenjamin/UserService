package com.user.UserService.user.domain.service;

import com.user.UserService.user.domain.exception.EmailAlreadyUsedException;
import com.user.UserService.user.domain.value.Email;
import com.user.UserService.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainUserValidator {

    private final UserRepository userRepository;

    public void validateEmailNotInUse(Email email) {
        if (userRepository.existsByEmail(email.getValue())) {
            throw new EmailAlreadyUsedException(email.getValue());
        }
    }

    public void validateEmailNotInUse(String email) {
        validateEmailNotInUse(Email.of(email));
    }
}

