package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.dto.UserDto;
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

    public UserDto register(UserPayload payload) {
        User user = new User();
        user.setUsername(payload.getUsername());
        user.setPassword(payload.getPassword()); // todo использовать BCryptPasswordEncoder
        user.setRole(Role.valueOf(payload.getRole().toUpperCase()));
        return userMapper.toDto(userRepository.save(user));
    }

    public void deleteUser(java.util.UUID id) {
        userRepository.deleteById(id);
    }

    public UserDto updateUser(UserPayload payload) {
        return userRepository.findByUsername(payload.getUsername())
                .map(user -> {
                    if (payload.getPassword() != null) {
                        user.setPassword(payload.getPassword());
                    }
                    if (payload.getRole() != null) {
                        user.setRole(Role.valueOf(payload.getRole().toUpperCase()));
                    }
                    return userMapper.toDto(userRepository.save(user));
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> login(UserPayload payload) {
        return userRepository.findByUsername(payload.getUsername())
                .filter(user -> user.getPassword().equals(payload.getPassword()));
    }
}
