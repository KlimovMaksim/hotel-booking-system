package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.dto.UserResponseDto;
import ru.klimov.entity.Role;
import ru.klimov.entity.User;
import ru.klimov.mapper.UserMapper;
import ru.klimov.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto register(UserPayload payload) {
        return authService.register(payload);
    }

    public void deleteUser(java.util.UUID id) {
        userRepository.deleteById(id);
    }

    public UserResponseDto updateUser(UserPayload payload) {
        return userRepository.findByUsername(payload.getUsername())
                .map(user -> {
                    if (payload.getPassword() != null) {
                        String encodedPassword = passwordEncoder.encode(payload.getPassword());
                        user.setPassword(encodedPassword);
                    }
                    if (payload.getRole() != null) {
                        user.setRole(Role.valueOf(payload.getRole().toUpperCase()));
                    }
                    return userMapper.userToUserResponseDto(userRepository.save(user));
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
