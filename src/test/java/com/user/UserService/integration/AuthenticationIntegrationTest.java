package com.user.UserService.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.UserService.TestFixtures;
import com.user.UserService.user.repository.UserRepository;
import com.user.UserService.user.web.dto.LoginRequest;
import com.user.UserService.user.web.dto.RegisterRequest;
import com.user.UserService.user.web.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterLoginAndAccessProtectedEndpoint() throws Exception {
        // Step 1: Register a new user
        RegisterRequest registerRequest = TestFixtures.Requests.createRegisterRequest();
        
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();
        
        TokenResponse registerTokens = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(),
                TokenResponse.class
        );
        
        assertThat(registerTokens.accessToken()).isNotEmpty();
        assertThat(registerTokens.refreshToken()).isNotEmpty();
        
        // Step 2: Access protected endpoint with access token
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + registerTokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(registerRequest.email()))
                .andExpect(jsonPath("$.fullName").value(registerRequest.fullName()));
        
        // Step 3: Login with same credentials
        LoginRequest loginRequest = new LoginRequest(registerRequest.email(), registerRequest.password());
        
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();
        
        TokenResponse loginTokens = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                TokenResponse.class
        );
        
        // Step 4: Access protected endpoint with new token
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + loginTokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(registerRequest.email()));
    }

    @Test
    void shouldRejectDuplicateEmailRegistration() throws Exception {
        // given - register first user
        RegisterRequest request = TestFixtures.Requests.createRegisterRequest();
        
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        // when & then - try to register with same email
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        // given - register user
        RegisterRequest registerRequest = TestFixtures.Requests.createRegisterRequest();
        
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
        
        // when & then - try to login with wrong password
        LoginRequest wrongPassword = new LoginRequest(registerRequest.email(), "WrongPassword123!");
        
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        // given - register and get tokens
        RegisterRequest registerRequest = TestFixtures.Requests.createRegisterRequest();
        
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        TokenResponse tokens = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TokenResponse.class
        );
        
        // when - refresh token
        String refreshPayload = String.format("{\"refreshToken\":\"%s\"}", tokens.refreshToken());
        
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.accessToken").value(org.hamcrest.Matchers.not(tokens.accessToken())));
    }

    @Test
    void shouldRejectAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetUserSessions() throws Exception {
        // given - register user
        RegisterRequest registerRequest = TestFixtures.Requests.createRegisterRequest();
        
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        TokenResponse tokens = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TokenResponse.class
        );
        
        // when - get sessions
        mockMvc.perform(get("/users/me/sessions")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].deviceName").exists())
                .andExpect(jsonPath("$[0].os").exists())
                .andExpect(jsonPath("$[0].ipAddress").exists());
    }
}

