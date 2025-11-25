package com.user.UserService.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpirationSeconds = 900;
    private long refreshTokenExpirationSeconds = 604800;
}
