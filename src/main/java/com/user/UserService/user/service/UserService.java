package com.user.UserService.user.service;

import com.user.UserService.user.domain.entity.DeviceSession;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.domain.exception.SessionNotFoundException;
import com.user.UserService.user.domain.exception.UserNotFoundException;
import com.user.UserService.user.repository.DeviceSessionRepository;
import com.user.UserService.user.repository.UserRepository;
import com.user.UserService.user.web.dto.DeviceSessionResponse;
import com.user.UserService.user.web.dto.UserResponse;
import com.user.UserService.user.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeviceSessionRepository deviceSessionRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findActiveById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));
    }

    @Transactional(readOnly = true)
    public List<DeviceSessionResponse> getActiveSessions(UUID userId) {
        List<DeviceSession> sessions = deviceSessionRepository.findActiveByUserId(userId);
        return sessions.stream()
                .map(userMapper::toDeviceSessionResponse)
                .toList();
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId) {
        DeviceSession session = deviceSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId.toString()));

        session.revoke();
        deviceSessionRepository.save(session);
    }

    @Transactional
    public void revokeAllSessions(UUID userId) {
        deviceSessionRepository.revokeAllByUserId(userId);
    }
}

