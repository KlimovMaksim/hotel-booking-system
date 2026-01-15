package ru.klimov.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.dto.UserResponseDto;
import ru.klimov.entity.Role;
import ru.klimov.entity.User;
import ru.klimov.mapper.UserMapper;
import ru.klimov.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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
    void register_ShouldCallAuthServiceRegister() {
        // given
        when(authService.register(userPayload)).thenReturn(userResponseDto);

        // when
        UserResponseDto result = userService.register(userPayload);

        // then
        assertThat(result).isEqualTo(userResponseDto);
        verify(authService).register(userPayload);
    }

    @Test
    void deleteUser_ShouldCallUserRepositoryDeleteById() {
        // given
        UUID userId = UUID.randomUUID();

        // when
        userService.deleteUser(userId);

        // then
        verify(userRepository).deleteById(userId);
    }

    @Test
    void updateUser_WithPasswordAndRole_ShouldUpdateAndReturnDto() {
        // given
        when(userRepository.findByUsername(userPayload.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(userPayload.getPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToUserResponseDto(user)).thenReturn(userResponseDto);

        // when
        UserResponseDto result = userService.updateUser(userPayload);

        // then
        assertThat(result).isEqualTo(userResponseDto);
        verify(userRepository).findByUsername(userPayload.getUsername());
        verify(passwordEncoder).encode(userPayload.getPassword());
        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void updateUser_UserNotFound_ShouldThrowException() {
        // given
        when(userRepository.findByUsername(userPayload.getUsername())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userPayload))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
