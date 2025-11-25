package com.user.UserService.user.service;

import com.user.UserService.user.domain.entity.RefreshToken;
import com.user.UserService.user.domain.exception.InvalidTokenException;
import com.user.UserService.user.domain.exception.TokenExpiredException;
import com.user.UserService.user.domain.exception.TokenRevokedException;
import com.user.UserService.user.domain.service.TokenGenerator;
import com.user.UserService.user.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenGenerator tokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    public boolean isAccessTokenValid(String token) {
        return tokenGenerator.isTokenValid(token);
    }

    public UUID extractUserId(String token) {
        return tokenGenerator.extractUserId(token);
    }

    public Claims parseAccessToken(String token) {
        return tokenGenerator.parseToken(token);
    }

    @Transactional
    public void validateRefreshToken(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidTokenException::new);

        if (storedToken.isExpired()) {
            throw new TokenExpiredException();
        }

        if (storedToken.isRevoked()) {
            throw new TokenRevokedException();
        }
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidTokenException::new);

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
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

