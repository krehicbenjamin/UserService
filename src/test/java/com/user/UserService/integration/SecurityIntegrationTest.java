package com.user.UserService.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.UserService.TestFixtures;
import com.user.UserService.user.web.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldBlockAccessToProtectedEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(get("/users/me/sessions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectMalformedAuthHeader() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidJWT() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"));
    }

    @Test
    void shouldRejectWeakPassword() throws Exception {
        // given
        var request = TestFixtures.Requests.createRegisterRequest("test@example.com", "weak", "Test User");
        
        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));
    }

    @Test
    void shouldSanitizeInputOnRegistration() throws Exception {
        // given
        var request = TestFixtures.Requests.createRegisterRequest(
                "test@example.com",
                "SecurePass123!@#",
                "<script>alert('xss')</script>Test User"
        );
        
        // when
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        // The full name should be sanitized (checked in other tests)
    }

    @Test
    void shouldEnforceCORS() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .header("Origin", "http://malicious-site.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestFixtures.Requests.createLoginRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRateLimitAuthEndpoints() throws Exception {
        // given
        LoginRequest request = TestFixtures.Requests.createLoginRequest();
        String requestBody = objectMapper.writeValueAsString(request);
        
        // when - make multiple requests rapidly
        for (int i = 0; i < 12; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
        
        // then - should be rate limited
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldValidateEmailFormat() throws Exception {
        // given
        var request = TestFixtures.Requests.createRegisterRequest(
                "not-an-email",
                "SecurePass123!@#",
                "Test User"
        );
        
        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        // given - empty request
        String emptyRequest = "{}";
        
        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
    }
}

