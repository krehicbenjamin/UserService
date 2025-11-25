package com.user.UserService.user.service;

import com.user.UserService.TestFixtures;
import com.user.UserService.common.InputSanitizer;
import com.user.UserService.user.domain.entity.User;
import com.user.UserService.user.domain.exception.EmailAlreadyUsedException;
import com.user.UserService.user.domain.exception.InvalidCredentialsException;
import com.user.UserService.user.domain.service.DomainUserValidator;
import com.user.UserService.user.domain.service.PasswordPolicy;
import com.user.UserService.user.domain.service.TokenGenerator;
import com.user.UserService.user.repository.RefreshTokenRepository;
import com.user.UserService.user.repository.UserRepository;
import com.user.UserService.user.web.dto.LoginRequest;
import com.user.UserService.user.web.dto.RegisterRequest;
import com.user.UserService.user.web.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private TokenGenerator tokenGenerator;
    
    @Mock
    private PasswordPolicy passwordPolicy;
    
    @Mock
    private DomainUserValidator domainUserValidator;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Mock
    private InputSanitizer inputSanitizer;
    
    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = TestFixtures.Requests.createRegisterRequest();
        loginRequest = TestFixtures.Requests.createLoginRequest();
        testUser = TestFixtures.Users.createUser();
    }

    @Test
    void shouldRegisterNewUser() {
        // given
        doNothing().when(passwordPolicy).validate(anyString());
        doNothing().when(domainUserValidator).validateEmailNotInUse(anyString());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(inputSanitizer.sanitizeAndLimit(anyString(), anyInt())).thenReturn(registerRequest.fullName());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenGenerator.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(tokenGenerator.generateRefreshToken(any())).thenReturn("refresh-token");
        
        // when
        TokenResponse response = authService.register(
                registerRequest,
                TestFixtures.Constants.IP_ADDRESS,
                TestFixtures.Constants.USER_AGENT
        );
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        
        verify(passwordPolicy).validate(registerRequest.password());
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // given
        doThrow(new EmailAlreadyUsedException("test@example.com"))
                .when(domainUserValidator)
                .validateEmailNotInUse(anyString());
        
        // when & then
        assertThatThrownBy(() -> authService.register(
                registerRequest,
                TestFixtures.Constants.IP_ADDRESS,
                TestFixtures.Constants.USER_AGENT
        ))
        .isInstanceOf(EmailAlreadyUsedException.class);
        
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        // given
        when(userRepository.findActiveByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenGenerator.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(tokenGenerator.generateRefreshToken(any())).thenReturn("refresh-token");
        
        // when
        TokenResponse response = authService.login(
                loginRequest,
                TestFixtures.Constants.IP_ADDRESS,
                TestFixtures.Constants.USER_AGENT
        );
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
        
        verify(userRepository).findActiveByEmail(anyString());
        verify(passwordEncoder).matches(loginRequest.password(), testUser.getPasswordHash());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        when(userRepository.findActiveByEmail(anyString())).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> authService.login(
                loginRequest,
                TestFixtures.Constants.IP_ADDRESS,
                TestFixtures.Constants.USER_AGENT
        ))
        .isInstanceOf(InvalidCredentialsException.class);
        
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // given
        when(userRepository.findActiveByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        // when & then
        assertThatThrownBy(() -> authService.login(
                loginRequest,
                TestFixtures.Constants.IP_ADDRESS,
                TestFixtures.Constants.USER_AGENT
        ))
        .isInstanceOf(InvalidCredentialsException.class);
        
        verify(tokenGenerator, never()).generateAccessToken(any());
    }
}
