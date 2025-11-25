package com.user.UserService.security;

import com.user.UserService.user.domain.event.UserLoggedInEvent;
import com.user.UserService.user.domain.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityEventListener {

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Security Event: User registered - userId={}, email={}", 
                event.userId(), 
                maskEmail(event.email()));
    }

    @EventListener
    public void onUserLoggedIn(UserLoggedInEvent event) {
        log.info("Security Event: User logged in - userId={}, email={}, ipAddress={}", 
                event.userId(), 
                maskEmail(event.email()),
                event.ipAddress());
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(2, parts[0].length())) + "***@" + parts[1];
    }
}

