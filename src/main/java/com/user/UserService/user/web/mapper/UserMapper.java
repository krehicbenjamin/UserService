package com.user.UserService.user.web.mapper;

import com.user.UserService.user.domain.entity.DeviceSession;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.domain.value.Role;
import com.user.UserService.user.web.dto.DeviceSessionResponse;
import com.user.UserService.user.web.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        Set<String> roles = user.getRoleSet().stream()
                .map(Role::name)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                roles,
                user.getCreatedAt()
        );
    }

    public DeviceSessionResponse toDeviceSessionResponse(DeviceSession session) {
        return new DeviceSessionResponse(
                session.getId(),
                session.getDeviceName(),
                session.getOs(),
                session.getIpAddress(),
                session.getLastUsedAt(),
                session.getCreatedAt()
        );
    }
}

