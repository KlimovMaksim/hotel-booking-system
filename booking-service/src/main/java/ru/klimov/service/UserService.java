package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.dto.UserResponseDto;
import ru.klimov.entity.Role;
import ru.klimov.entity.User;
import ru.klimov.mapper.UserMapper;
import ru.klimov.repository.UserRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto register(UserPayload payload) {
        log.info("Registering user via UserService: {}", payload.getUsername());
        return authService.register(payload);
    }

    public void deleteUser(java.util.UUID id) {
        log.info("Deleting user with id: {}", id);
        userRepository.deleteById(id);
        log.info("User with id {} successfully deleted", id);
    }

    public UserResponseDto updateUser(UserPayload payload) {
        log.info("Updating user: {}", payload.getUsername());
        return userRepository.findByUsername(payload.getUsername())
                .map(user -> {
                    if (payload.getPassword() != null) {
                        log.debug("Updating password for user {}", payload.getUsername());
                        String encodedPassword = passwordEncoder.encode(payload.getPassword());
                        user.setPassword(encodedPassword);
                    }
                    if (payload.getRole() != null) {
                        log.debug("Updating role to {} for user {}", payload.getRole(), payload.getUsername());
                        user.setRole(Role.valueOf(payload.getRole().toUpperCase()));
                    }
                    User savedUser = userRepository.save(user);
                    log.info("User {} successfully updated", payload.getUsername());
                    return userMapper.userToUserResponseDto(savedUser);
                }).orElseThrow(() -> {
                    log.error("Update failed: user {} not found", payload.getUsername());
                    return new RuntimeException("User not found");
                });
    }
}
