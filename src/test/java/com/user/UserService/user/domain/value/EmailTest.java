package com.user.UserService.user.domain.value;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        // given
        String validEmail = "test@example.com";
        
        // when
        Email email = Email.of(validEmail);
        
        // then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }
    
    @Test
    void shouldNormalizeEmailToLowerCase() {
        // given
        String mixedCaseEmail = "Test@Example.COM";
        
        // when
        Email email = Email.of(mixedCaseEmail);
        
        // then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }
    
    @Test
    void shouldTrimWhitespace() {
        // given
        String emailWithSpaces = "  test@example.com  ";
        
        // when
        Email email = Email.of(emailWithSpaces);
        
        // then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_123@test-domain.com",
            "a@b.c"
    })
    void shouldAcceptValidEmailFormats(String validEmail) {
        assertThatNoException().isThrownBy(() -> Email.of(validEmail));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "not-an-email",
            "@example.com",
            "user@",
            "user @example.com",
            "user@.com",
            "user@domain",
            "user@@example.com"
    })
    void shouldRejectInvalidEmailFormats(String invalidEmail) {
        assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }
    
    @Test
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void shouldBeEqualWhenEmailsAreSame() {
        // given
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("TEST@example.com");
        
        // then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenEmailsAreDifferent() {
        // given
        Email email1 = Email.of("test1@example.com");
        Email email2 = Email.of("test2@example.com");
        
        // then
        assertThat(email1).isNotEqualTo(email2);
    }
    
    @Test
    void shouldHaveProperToString() {
        // given
        Email email = Email.of("test@example.com");
        
        // then
        assertThat(email.toString()).contains("test@example.com");
    }
}

