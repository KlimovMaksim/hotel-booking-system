package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.entity.Role;
import ru.klimov.entity.User;
import ru.klimov.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User register(UserPayload payload) {
        User user = new User();
        user.setUsername(payload.getUsername());
        user.setPassword(payload.getPassword()); // todo использовать BCryptPasswordEncoder
        user.setRole(Role.valueOf(payload.getRole().toUpperCase()));
        return userRepository.save(user);
    }

    public Optional<User> login(UserPayload payload) {
        return userRepository.findByUsername(payload.getUsername())
                .filter(user -> user.getPassword().equals(payload.getPassword()));
    }
}
