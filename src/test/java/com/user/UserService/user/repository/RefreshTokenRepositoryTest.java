package com.user.UserService.user.repository;

import com.user.UserService.TestFixtures;
import com.user.UserService.user.domain.entity.RefreshToken;
import com.user.UserService.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.Users.createUser();
        entityManager.persistAndFlush(testUser);
    }

    @Test
    void shouldSaveAndFindRefreshToken() {
        // given
        RefreshToken token = TestFixtures.RefreshTokens.createRefreshToken(testUser);
        
        // when
        RefreshToken saved = entityManager.persistAndFlush(token);
        Optional<RefreshToken> found = refreshTokenRepository.findById(saved.getId());
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(testUser.getId());
        assertThat(found.get().getTokenHash()).isEqualTo(token.getTokenHash());
    }

    @Test
    void shouldFindByTokenHashAndUserId() {
        // given
        RefreshToken token = TestFixtures.RefreshTokens.createRefreshToken(testUser);
        entityManager.persistAndFlush(token);
        
        // when
        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(token.getTokenHash());
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(token.getId());
        assertThat(found.get().getUserId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindActiveTokensByUserId() {
        // given
        RefreshToken activeToken1 = TestFixtures.RefreshTokens.createRefreshToken(testUser);
        RefreshToken activeToken2 = TestFixtures.RefreshTokens.createRefreshToken(testUser);
        RefreshToken revokedToken = TestFixtures.RefreshTokens.createRevokedRefreshToken(testUser);
        
        entityManager.persist(activeToken1);
        entityManager.persist(activeToken2);
        entityManager.persist(revokedToken);
        entityManager.flush();
        
        // when
        List<RefreshToken> found = refreshTokenRepository.findActiveByUserId(testUser.getId());
        
        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(RefreshToken::isRevoked).containsOnly(false);
    }

    @Test
    void shouldDeleteExpiredTokens() {
        // given
        RefreshToken activeToken = TestFixtures.RefreshTokens.createRefreshToken(testUser);
        RefreshToken expiredToken = TestFixtures.RefreshTokens.createExpiredRefreshToken(testUser);
        
        entityManager.persist(activeToken);
        entityManager.persist(expiredToken);
        entityManager.flush();
        
        // when
        refreshTokenRepository.delete(expiredToken);
        entityManager.flush();
        
        // then
        Optional<RefreshToken> found = refreshTokenRepository.findById(expiredToken.getId());
        assertThat(found).isEmpty();
        
        Optional<RefreshToken> activeFound = refreshTokenRepository.findById(activeToken.getId());
        assertThat(activeFound).isPresent();
    }

    @Test
    void shouldUpdateTokenRevokedStatus() {
        // given
        RefreshToken token = TestFixtures.RefreshTokens.createRefreshToken(testUser);
        entityManager.persistAndFlush(token);
        
        // when
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();
        
        // then
        Optional<RefreshToken> found = refreshTokenRepository.findById(token.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isRevoked()).isTrue();
    }

    @Test
    void shouldFindAllUserTokens() {
        // given
        RefreshToken token1 = TestFixtures.RefreshTokens.createRefreshToken(testUser);
        RefreshToken token2 = TestFixtures.RefreshTokens.createRefreshToken(testUser);
        RefreshToken token3 = TestFixtures.RefreshTokens.createRevokedRefreshToken(testUser);
        
        entityManager.persist(token1);
        entityManager.persist(token2);
        entityManager.persist(token3);
        entityManager.flush();
        
        User anotherUser = TestFixtures.Users.createUser("other@example.com", "Other User");
        entityManager.persist(anotherUser);
        RefreshToken otherUserToken = TestFixtures.RefreshTokens.createRefreshToken(anotherUser);
        entityManager.persistAndFlush(otherUserToken);
        
        // when
        List<RefreshToken> found = refreshTokenRepository.findActiveByUserId(testUser.getId());
        
        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(RefreshToken::getUserId).containsOnly(testUser.getId());
    }
}

