package ru.klimov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.dto.UserDto;
import ru.klimov.service.UserService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserPayload payload) {
        return ResponseEntity.ok(userService.register(payload));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserPayload payload) {
        return userService.login(payload)
                .map(user -> ResponseEntity.ok("Login successful"))
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }
}
