package com.user.UserService.user.domain.service;

import com.user.UserService.TestFixtures;
import com.user.UserService.security.JwtProperties;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.domain.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TokenGeneratorTest {

    private TokenGenerator tokenGenerator;
    private JwtProperties jwtProperties;
    private static final String TEST_SECRET = "test-secret-for-jwt-that-is-long-enough-to-be-secure-for-hmac512-signing-algorithm-with-more-than-64-bytes";

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setAccessTokenExpirationSeconds(900L);
        jwtProperties.setRefreshTokenExpirationSeconds(604800L);
        
        tokenGenerator = new TokenGenerator(jwtProperties);
    }

    @Test
    void shouldGenerateAccessToken() {
        // given
        User user = TestFixtures.Users.createUser();
        user.addRole(com.user.UserService.user.domain.value.Role.USER);
        
        // when
        String token = tokenGenerator.generateAccessToken(user);
        
        // then
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void shouldGenerateRefreshToken() {
        // given
        UUID userId = UUID.randomUUID();
        
        // when
        String token = tokenGenerator.generateRefreshToken(userId);
        
        // then
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void shouldParseValidAccessToken() {
        // given
        User user = TestFixtures.Users.createUser();
        user.addRole(com.user.UserService.user.domain.value.Role.USER);
        String token = tokenGenerator.generateAccessToken(user);
        
        // when
        Claims claims = tokenGenerator.parseToken(token);
        
        // then
        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
        assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
    }

    @Test
    void shouldExtractUserIdFromToken() {
        // given
        UUID userId = UUID.randomUUID();
        String token = tokenGenerator.generateRefreshToken(userId);
        
        // when
        UUID extractedId = tokenGenerator.extractUserId(token);
        
        // then
        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    void shouldRejectExpiredToken() {
        // given - create expired token manually
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(key)
                .compact();
        
        // when & then
        assertThatThrownBy(() -> tokenGenerator.parseToken(expiredToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldRejectMalformedToken() {
        // given
        String malformedToken = "not.a.valid.token";
        
        // when & then
        assertThatThrownBy(() -> tokenGenerator.parseToken(malformedToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldRejectNullToken() {
        assertThatThrownBy(() -> tokenGenerator.parseToken(null))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldRejectEmptyToken() {
        assertThatThrownBy(() -> tokenGenerator.parseToken(""))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // given
        User user = TestFixtures.Users.createUser();
        user.addRole(com.user.UserService.user.domain.value.Role.USER);
        String token = tokenGenerator.generateAccessToken(user);
        
        // when
        boolean isValid = tokenGenerator.isTokenValid(token);
        
        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        // given
        String invalidToken = "invalid.token.here";
        
        // when
        boolean isValid = tokenGenerator.isTokenValid(invalidToken);
        
        // then
        assertThat(isValid).isFalse();
    }
}
