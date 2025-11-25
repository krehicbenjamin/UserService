package com.user.UserService.user.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.UserService.TestFixtures;
import com.user.UserService.user.domain.exception.EmailAlreadyUsedException;
import com.user.UserService.user.domain.exception.InvalidCredentialsException;
import com.user.UserService.user.domain.exception.WeakPasswordException;
import com.user.UserService.user.service.AuthService;
import com.user.UserService.user.web.dto.LoginRequest;
import com.user.UserService.user.web.dto.RefreshRequest;
import com.user.UserService.user.web.dto.RegisterRequest;
import com.user.UserService.user.web.dto.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;


    @Test
    void shouldRegisterNewUser() throws Exception {
        // given
        RegisterRequest request = TestFixtures.Requests.createRegisterRequest();
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token", "Bearer");
        
        when(authService.register(any(), anyString(), anyString()))
                .thenReturn(tokenResponse);
        
        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturnBadRequestWhenRegistrationFails() throws Exception {
        // given
        RegisterRequest request = TestFixtures.Requests.createRegisterRequest();
        
        when(authService.register(any(), anyString(), anyString()))
                .thenThrow(new EmailAlreadyUsedException("test@example.com"));
        
        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidEmailOnRegistration() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "SecurePass123!@#",
                "Test User"
        );
        
        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectWeakPasswordOnRegistration() throws Exception {
        // given
        RegisterRequest request = TestFixtures.Requests.createRegisterRequest();
        
        when(authService.register(any(), anyString(), anyString()))
                .thenThrow(new WeakPasswordException("Password is too weak"));
        
        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // given
        LoginRequest request = TestFixtures.Requests.createLoginRequest();
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token", "Bearer");
        
        when(authService.login(any(), anyString(), anyString()))
                .thenReturn(tokenResponse);
        
        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        // given
        LoginRequest request = TestFixtures.Requests.createLoginRequest();
        
        when(authService.login(any(), anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException());
        
        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        // given
        RefreshRequest request = new RefreshRequest("valid-refresh-token");
        TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token", "Bearer");
        
        when(authService.refresh(anyString(), anyString(), anyString())).thenReturn(tokenResponse);
        
        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void shouldRejectEmptyRegistrationRequest() throws Exception {
        // given
        String emptyRequest = "{}";
        
        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyRequest)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyLoginRequest() throws Exception {
        // given
        String emptyRequest = "{}";
        
        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyRequest)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}

