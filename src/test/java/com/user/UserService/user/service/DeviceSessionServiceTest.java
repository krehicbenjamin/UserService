package com.user.UserService.user.service;

import com.user.UserService.TestFixtures;
import com.user.UserService.user.domain.entity.DeviceSession;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.repository.DeviceSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceSessionServiceTest {

    @Mock
    private DeviceSessionRepository deviceSessionRepository;
    
    @InjectMocks
    private DeviceSessionService deviceSessionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.Users.createUser();
    }

    @Test
    void shouldCreateSession() {
        // given
        DeviceSession session = TestFixtures.DeviceSessions.createDeviceSession(testUser);
        when(deviceSessionRepository.save(any(DeviceSession.class))).thenReturn(session);
        
        // when
        DeviceSession created = deviceSessionService.createOrUpdateSession(
                testUser.getId(),
                "192.168.1.1",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        );
        
        // then
        assertThat(created).isNotNull();
        verify(deviceSessionRepository).save(any(DeviceSession.class));
    }

    @Test
    void shouldGetActiveSessionsForUser() {
        // given
        List<DeviceSession> sessions = List.of(
                TestFixtures.DeviceSessions.createDeviceSession(testUser),
                TestFixtures.DeviceSessions.createDeviceSession(testUser)
        );
        when(deviceSessionRepository.findActiveByUserId(testUser.getId())).thenReturn(sessions);
        
        // when
        List<DeviceSession> result = deviceSessionService.getActiveSessionsForUser(testUser.getId());
        
        // then
        assertThat(result).hasSize(2);
        verify(deviceSessionRepository).findActiveByUserId(testUser.getId());
    }

    @Test
    void shouldRevokeSession() {
        // given
        DeviceSession session = TestFixtures.DeviceSessions.createDeviceSession(testUser);
        when(deviceSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        
        // when
        deviceSessionService.revokeSession(session.getId());
        
        // then
        verify(deviceSessionRepository).save(session);
        assertThat(session.isRevoked()).isTrue();
    }

    @Test
    void shouldRevokeAllUserSessions() {
        // given
        UUID userId = testUser.getId();
        
        // when
        deviceSessionService.revokeAllUserSessions(userId);
        
        // then
        verify(deviceSessionRepository).revokeAllByUserId(userId);
    }

    @Test
    void shouldExtractDeviceNameFromUserAgent() {
        // given
        String windowsUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        when(deviceSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // when
        DeviceSession session = deviceSessionService.createOrUpdateSession(
                testUser.getId(),
                "192.168.1.1",
                windowsUserAgent
        );
        
        // then
        assertThat(session.getDeviceName()).contains("Windows");
    }
}

