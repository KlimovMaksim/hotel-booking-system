package ru.klimov.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.controller.payload.UserShortPayload;
import ru.klimov.dto.TokenResponseDto;
import ru.klimov.dto.UserResponseDto;
import ru.klimov.entity.Role;
import ru.klimov.entity.User;
import ru.klimov.exception.RegistrationException;
import ru.klimov.exception.UserNotFoundException;
import ru.klimov.mapper.UserMapper;
import ru.klimov.repository.UserRepository;
import ru.klimov.security.JwtUtil;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UserPayload userPayload;
    private User user;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        userPayload = new UserPayload();
        userPayload.setUsername("testuser");
        userPayload.setPassword("password");
        userPayload.setRole("USER");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        userResponseDto = UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Test
    void register_ValidRole_ShouldRegisterUser() {
        // given
        when(userMapper.userPayloadToUser(userPayload)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userToUserResponseDto(user)).thenReturn(userResponseDto);

        // when
        UserResponseDto result = authService.register(userPayload);

        // then
        assertThat(result).isEqualTo(userResponseDto);
        verify(userMapper).userPayloadToUser(userPayload);
        verify(userRepository).save(user);
        verify(userMapper).userToUserResponseDto(user);
    }

    @Test
    void register_InvalidRole_ShouldThrowException() {
        // given
        userPayload.setRole("INVALID_ROLE");

        // when & then
        assertThatThrownBy(() -> authService.register(userPayload))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("Only one of two roles is available for installation");
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        // given
        UserShortPayload loginPayload = new UserShortPayload();
        loginPayload.setUsername("testuser");
        loginPayload.setPassword("password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateTokenForUser(user)).thenReturn("mock-token");

        // when
        TokenResponseDto result = authService.login(loginPayload);

        // then
        assertThat(result.getToken()).isEqualTo("mock-token");
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password", user.getPassword());
        verify(jwtUtil).generateTokenForUser(user);
    }

    @Test
    void login_UserNotFound_ShouldThrowException() {
        // given
        UserShortPayload loginPayload = new UserShortPayload();
        loginPayload.setUsername("unknown");
        loginPayload.setPassword("password");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(loginPayload))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("The user with the specified data was not found.");
    }

    @Test
    void login_WrongPassword_ShouldThrowException() {
        // given
        UserShortPayload loginPayload = new UserShortPayload();
        loginPayload.setUsername("testuser");
        loginPayload.setPassword("wrong-password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(loginPayload))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("The user with the specified data was not found.");
    }
}
