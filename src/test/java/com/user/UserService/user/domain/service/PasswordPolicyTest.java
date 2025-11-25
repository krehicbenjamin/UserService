package com.user.UserService.user.domain.service;

import com.user.UserService.user.domain.exception.WeakPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class PasswordPolicyTest {

    private PasswordPolicy passwordPolicy;

    @BeforeEach
    void setUp() {
        passwordPolicy = new PasswordPolicy();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SecurePass123!@#",
            "MyP@ssw0rd",
            "Str0ng!Pass",
            "Abc123!@#Def",
            "P@ssw0rdWithNumbers123"
    })
    void shouldAcceptStrongPasswords(String strongPassword) {
        assertThatNoException()
                .isThrownBy(() -> passwordPolicy.validate(strongPassword));
    }

    @Test
    void shouldRejectPasswordTooShort() {
        // given
        String shortPassword = "Abc1!";
        
        // when & then
        assertThatThrownBy(() -> passwordPolicy.validate(shortPassword))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("at least 8 characters");
    }

    @Test
    void shouldRejectPasswordTooLong() {
        // given
        String longPassword = "A".repeat(129) + "1!aB";
        
        // when & then
        assertThatThrownBy(() -> passwordPolicy.validate(longPassword))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("maximum 128 characters");
    }

    @Test
    void shouldRejectPasswordWithoutUppercase() {
        // given
        String noUppercase = "password123!@#";
        
        // when & then
        assertThatThrownBy(() -> passwordPolicy.validate(noUppercase))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("uppercase letter");
    }

    @Test
    void shouldRejectPasswordWithoutLowercase() {
        // given
        String noLowercase = "PASSWORD123!@#";
        
        // when & then
        assertThatThrownBy(() -> passwordPolicy.validate(noLowercase))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("lowercase letter");
    }

    @Test
    void shouldRejectPasswordWithoutDigit() {
        // given
        String noDigit = "Password!@#";
        
        // when & then
        assertThatThrownBy(() -> passwordPolicy.validate(noDigit))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("digit");
    }

    @Test
    void shouldRejectPasswordWithoutSpecialCharacter() {
        // given
        String noSpecial = "Password123";
        
        // when & then
        assertThatThrownBy(() -> passwordPolicy.validate(noSpecial))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("special character");
    }

    @Test
    void shouldRejectNullPassword() {
        assertThatThrownBy(() -> passwordPolicy.validate(null))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void shouldRejectEmptyPassword() {
        assertThatThrownBy(() -> passwordPolicy.validate(""))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void shouldRejectWhitespaceOnlyPassword() {
        assertThatThrownBy(() -> passwordPolicy.validate("   "))
                .isInstanceOf(WeakPasswordException.class);
    }
}

