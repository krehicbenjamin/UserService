package com.user.UserService.user.service;

import com.user.UserService.user.domain.entity.RefreshToken;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.domain.event.UserLoggedInEvent;
import com.user.UserService.user.domain.event.UserRegisteredEvent;
import com.user.UserService.user.domain.exception.InvalidCredentialsException;
import com.user.UserService.user.domain.exception.UserNotFoundException;
import com.user.UserService.user.domain.service.DomainUserValidator;
import com.user.UserService.user.domain.service.PasswordPolicy;
import com.user.UserService.user.domain.service.TokenGenerator;
import com.user.UserService.user.domain.value.Email;
import com.user.UserService.user.domain.value.Role;
import com.user.UserService.user.repository.RefreshTokenRepository;
import com.user.UserService.user.repository.UserRepository;
import com.user.UserService.user.web.dto.LoginRequest;
import com.user.UserService.user.web.dto.RegisterRequest;
import com.user.UserService.user.web.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final DomainUserValidator userValidator;
    private final TokenGenerator tokenGenerator;
    private final DeviceSessionService deviceSessionService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.user.UserService.common.InputSanitizer inputSanitizer;

    @Transactional
    public TokenResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        Email email = Email.of(request.email());
        userValidator.validateEmailNotInUse(email);
        passwordPolicy.validate(request.password());

        User user = User.builder()
                .email(email.getValue())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(inputSanitizer.sanitizeAndLimit(request.fullName(), 255))
                .build();

        user.addRole(Role.USER);
        userRepository.save(user);

        eventPublisher.publishEvent(UserRegisteredEvent.of(user.getId(), user.getEmail(), user.getFullName()));

        return createTokensAndSession(user, ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        Email email = Email.of(request.email());

        User user = userRepository.findActiveByEmail(email.getValue())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        eventPublisher.publishEvent(UserLoggedInEvent.of(user.getId(), user.getEmail(), ipAddress, userAgent));

        return createTokensAndSession(user, ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken, String ipAddress, String userAgent) {
        String tokenHash = hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new com.user.UserService.user.domain.exception.InvalidTokenException());

        if (!storedToken.isValid()) {
            if (storedToken.isExpired()) {
                throw new com.user.UserService.user.domain.exception.TokenExpiredException();
            }
            throw new com.user.UserService.user.domain.exception.TokenRevokedException();
        }

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findActiveById(storedToken.getUserId())
                .orElseThrow(() -> UserNotFoundException.byId(storedToken.getUserId().toString()));

        return createTokensAndSession(user, ipAddress, userAgent);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private TokenResponse createTokensAndSession(User user, String ipAddress, String userAgent) {
        String accessToken = tokenGenerator.generateAccessToken(user);
        String refreshToken = tokenGenerator.generateRefreshToken(user.getId());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(refreshToken))
                .expiry(tokenGenerator.getRefreshTokenExpiry())
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        deviceSessionService.createOrUpdateSession(user.getId(), ipAddress, userAgent);

        return new TokenResponse(accessToken, refreshToken, "Bearer");
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

