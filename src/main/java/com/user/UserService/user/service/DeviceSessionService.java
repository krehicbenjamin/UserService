package com.user.UserService.user.service;

import com.user.UserService.user.domain.entity.DeviceSession;
import com.user.UserService.user.repository.DeviceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceSessionService {

    private final DeviceSessionRepository deviceSessionRepository;

    @Transactional
    public DeviceSession createOrUpdateSession(UUID userId, String ipAddress, String userAgent) {
        String deviceName = extractDeviceName(userAgent);
        String os = extractOs(userAgent);

        DeviceSession session = DeviceSession.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceName(deviceName)
                .os(os)
                .revoked(false)
                .build();

        return deviceSessionRepository.save(session);
    }

    @Transactional
    public void updateLastUsed(UUID sessionId) {
        deviceSessionRepository.findById(sessionId).ifPresent(session -> {
            session.updateLastUsed();
            deviceSessionRepository.save(session);
        });
    }

    @Transactional(readOnly = true)
    public List<DeviceSession> getActiveSessionsForUser(UUID userId) {
        return deviceSessionRepository.findActiveByUserId(userId);
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        deviceSessionRepository.findById(sessionId).ifPresent(session -> {
            session.revoke();
            deviceSessionRepository.save(session);
        });
    }

    @Transactional
    public void revokeAllUserSessions(UUID userId) {
        deviceSessionRepository.revokeAllByUserId(userId);
    }

    private String extractDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }

        if (userAgent.contains("Mobile")) {
            if (userAgent.contains("iPhone")) {
                return "iPhone";
            } else if (userAgent.contains("Android")) {
                return "Android Device";
            } else if (userAgent.contains("iPad")) {
                return "iPad";
            }
            return "Mobile Device";
        }

        if (userAgent.contains("Windows")) {
            return "Windows PC";
        } else if (userAgent.contains("Macintosh")) {
            return "Mac";
        } else if (userAgent.contains("Linux")) {
            return "Linux PC";
        }

        return "Unknown Device";
    }

    private String extractOs(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }

        if (userAgent.contains("Windows NT 10")) {
            return "Windows 10/11";
        } else if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Mac OS X")) {
            return "macOS";
        } else if (userAgent.contains("Android")) {
            return "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        } else if (userAgent.contains("Linux")) {
            return "Linux";
        }

        return "Unknown OS";
    }
}

