package com.user.UserService.user.service;

import com.user.UserService.TestFixtures;
import com.user.UserService.user.domain.entity.RefreshToken;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.Users.createUser();
    }

    @Test
    void shouldRevokeAllUserTokens() {
        // given
        List<RefreshToken> tokens = List.of(
                TestFixtures.RefreshTokens.createRefreshToken(testUser),
                TestFixtures.RefreshTokens.createRefreshToken(testUser)
        );
        when(refreshTokenRepository.findActiveByUserId(testUser.getId())).thenReturn(tokens);
        
        // when
        refreshTokenRepository.revokeAllByUserId(testUser.getId());
        
        // then
        verify(refreshTokenRepository).revokeAllByUserId(testUser.getId());
    }
}
