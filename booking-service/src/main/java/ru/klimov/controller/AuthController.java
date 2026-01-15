package ru.klimov.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.controller.payload.UserShortPayload;
import ru.klimov.dto.TokenResponseDto;
import ru.klimov.dto.UserResponseDto;
import ru.klimov.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя")
    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserPayload userPayload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(userPayload));
    }

    @Operation(summary = "Аутентификация пользователя")
    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody UserShortPayload userShortPayload) {
        return ResponseEntity.ok(authService.login(userShortPayload));
    }
}
