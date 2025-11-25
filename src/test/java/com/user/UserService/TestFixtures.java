package com.user.UserService;

import com.user.UserService.user.domain.entity.DeviceSession;
import com.user.UserService.user.domain.entity.RefreshToken;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.domain.entity.UserRole;
import com.user.UserService.user.domain.value.Role;
import com.user.UserService.user.web.dto.LoginRequest;
import com.user.UserService.user.web.dto.RegisterRequest;

import java.time.Instant;
import java.util.UUID;

/**
 * Central test fixtures for creating test data
 */
public class TestFixtures {

    public static class Users {
        
        public static User createUser() {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("test@example.com");
            user.setPasswordHash("$2a$12$hashedpassword");
            user.setFullName("Test User");
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            return user;
        }
        
        public static User createUser(String email, String fullName) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail(email);
            user.setPasswordHash("$2a$12$hashedpassword");
            user.setFullName(fullName);
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            return user;
        }
        
        public static User createDeletedUser() {
            User user = createUser();
            user.softDelete();
            return user;
        }
        
        public static User createAdminUser() {
            User user = createUser("admin@example.com", "Admin User");
            user.addRole(Role.ADMIN);
            return user;
        }
    }
    
    public static class UserRoles {
        
        public static UserRole createUserRole(User user, Role role) {
            return UserRole.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .role(role)
                    .build();
        }
        
        public static UserRole createUserRole(User user) {
            return createUserRole(user, Role.USER);
        }
    }
    
    public static class RefreshTokens {
        
        public static RefreshToken createRefreshToken(User user) {
            return RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .userId(user.getId())
                    .tokenHash("hashed-token")
                    .expiry(Instant.now().plusSeconds(604800))
                    .revoked(false)
                    .createdAt(Instant.now())
                    .build();
        }
        
        public static RefreshToken createExpiredRefreshToken(User user) {
            return RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .userId(user.getId())
                    .tokenHash("hashed-token")
                    .expiry(Instant.now().minusSeconds(1))
                    .revoked(false)
                    .createdAt(Instant.now().minusSeconds(604800))
                    .build();
        }
        
        public static RefreshToken createRevokedRefreshToken(User user) {
            RefreshToken token = createRefreshToken(user);
            token.revoke();
            return token;
        }
    }
    
    public static class DeviceSessions {
        
        public static DeviceSession createDeviceSession(User user) {
            return DeviceSession.builder()
                    .id(UUID.randomUUID())
                    .userId(user.getId())
                    .deviceName("Test Device")
                    .os("Windows 10")
                    .ipAddress("192.168.1.100")
                    .userAgent("Mozilla/5.0")
                    .revoked(false)
                    .lastUsedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();
        }
        
        public static DeviceSession createDeviceSession(User user, String deviceName, String ipAddress) {
            return DeviceSession.builder()
                    .id(UUID.randomUUID())
                    .userId(user.getId())
                    .deviceName(deviceName)
                    .os("Windows 10")
                    .ipAddress(ipAddress)
                    .userAgent("Mozilla/5.0")
                    .revoked(false)
                    .lastUsedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();
        }
    }
    
    public static class Requests {
        
        public static RegisterRequest createRegisterRequest() {
            return new RegisterRequest(
                    "newuser@example.com",
                    "SecurePass123!@#",
                    "New User"
            );
        }
        
        public static RegisterRequest createRegisterRequest(String email, String password, String fullName) {
            return new RegisterRequest(email, password, fullName);
        }
        
        public static LoginRequest createLoginRequest() {
            return new LoginRequest(
                    "test@example.com",
                    "SecurePass123!@#"
            );
        }
        
        public static LoginRequest createLoginRequest(String email, String password) {
            return new LoginRequest(email, password);
        }
    }
    
    public static class Constants {
        public static final String VALID_EMAIL = "test@example.com";
        public static final String VALID_PASSWORD = "SecurePass123!@#";
        public static final String WEAK_PASSWORD = "weak";
        public static final String INVALID_EMAIL = "not-an-email";
        public static final String FULL_NAME = "Test User";
        
        public static final String DEVICE_NAME = "Test Device";
        public static final String OS = "Windows 10";
        public static final String IP_ADDRESS = "192.168.1.100";
        public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    }
}
