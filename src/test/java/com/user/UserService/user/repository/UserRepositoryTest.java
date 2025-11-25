package com.user.UserService.user.repository;

import com.user.UserService.TestFixtures;
import com.user.UserService.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        // given
        User user = TestFixtures.Users.createUser();
        
        // when
        User saved = entityManager.persistAndFlush(user);
        Optional<User> found = userRepository.findById(saved.getId());
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(found.get().getFullName()).isEqualTo(user.getFullName());
    }

    @Test
    void shouldFindActiveUserByEmail() {
        // given
        User user = TestFixtures.Users.createUser();
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> found = userRepository.findActiveByEmail(user.getEmail());
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldNotFindDeletedUserByEmail() {
        // given
        User user = TestFixtures.Users.createDeletedUser();
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> found = userRepository.findActiveByEmail(user.getEmail());
        
        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindActiveUserById() {
        // given
        User user = TestFixtures.Users.createUser();
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> found = userRepository.findActiveById(user.getId());
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldNotFindDeletedUserById() {
        // given
        User user = TestFixtures.Users.createDeletedUser();
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> found = userRepository.findActiveById(user.getId());
        
        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // given
        User user = TestFixtures.Users.createUser();
        entityManager.persistAndFlush(user);
        
        // when
        boolean exists = userRepository.existsByEmail(user.getEmail());
        
        // then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseForNonExistentEmail() {
        // given
        String nonExistentEmail = "nonexistent@example.com";
        
        // when
        boolean exists = userRepository.existsByEmail(nonExistentEmail);
        
        // then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldUpdateUser() {
        // given
        User user = TestFixtures.Users.createUser();
        entityManager.persistAndFlush(user);
        
        // when
        user.setFullName("Updated Name");
        User updated = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        Optional<User> found = userRepository.findById(updated.getId());
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldSoftDeleteUser() {
        // given
        User user = TestFixtures.Users.createUser();
        entityManager.persistAndFlush(user);
        
        // when
        user.softDelete();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // then
        Optional<User> found = userRepository.findActiveById(user.getId());
        assertThat(found).isEmpty();
        
        Optional<User> foundById = userRepository.findById(user.getId());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().isDeleted()).isTrue();
    }
}

