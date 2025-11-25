package com.user.UserService.user.service;

import com.user.UserService.TestFixtures;
import com.user.UserService.user.domain.entity.DeviceSession;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.domain.exception.UserNotFoundException;
import com.user.UserService.user.repository.DeviceSessionRepository;
import com.user.UserService.user.repository.UserRepository;
import com.user.UserService.user.web.dto.DeviceSessionResponse;
import com.user.UserService.user.web.dto.UserResponse;
import com.user.UserService.user.web.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DeviceSessionRepository deviceSessionRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.Users.createUser();
        userResponse = new UserResponse(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getFullName(),
                Set.of("USER"),
                testUser.getCreatedAt()
        );
    }

    @Test
    void shouldGetCurrentUser() {
        // given
        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        
        // when
        UserResponse response = userService.getCurrentUser(testUser.getId());
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testUser.getId());
        verify(userRepository).findActiveById(testUser.getId());
    }

    @Test
    void shouldGetUserById() {
        // given
        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // when
        User user = userService.getUserById(testUser.getId());
        
        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(testUser.getId());
        verify(userRepository).findActiveById(testUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldGetActiveSessions() {
        // given
        List<DeviceSession> sessions = List.of(
                TestFixtures.DeviceSessions.createDeviceSession(testUser),
                TestFixtures.DeviceSessions.createDeviceSession(testUser)
        );
        DeviceSessionResponse sessionResponse = new DeviceSessionResponse(
                sessions.get(0).getId(),
                "Test Device",
                "Windows 10",
                "192.168.1.1",
                Instant.now(),
                Instant.now()
        );
        
        when(deviceSessionRepository.findActiveByUserId(testUser.getId())).thenReturn(sessions);
        when(userMapper.toDeviceSessionResponse(any())).thenReturn(sessionResponse);
        
        // when
        List<DeviceSessionResponse> responses = userService.getActiveSessions(testUser.getId());
        
        // then
        assertThat(responses).hasSize(2);
        verify(deviceSessionRepository).findActiveByUserId(testUser.getId());
    }

    @Test
    void shouldRevokeSession() {
        // given
        UUID sessionId = UUID.randomUUID();
        DeviceSession session = TestFixtures.DeviceSessions.createDeviceSession(testUser);
        when(deviceSessionRepository.findByIdAndUserId(sessionId, testUser.getId()))
                .thenReturn(Optional.of(session));
        
        // when
        userService.revokeSession(testUser.getId(), sessionId);
        
        // then
        verify(deviceSessionRepository).save(session);
        assertThat(session.isRevoked()).isTrue();
    }

    @Test
    void shouldRevokeAllSessions() {
        // given
        UUID userId = testUser.getId();
        
        // when
        userService.revokeAllSessions(userId);
        
        // then
        verify(deviceSessionRepository).revokeAllByUserId(userId);
    }
}
