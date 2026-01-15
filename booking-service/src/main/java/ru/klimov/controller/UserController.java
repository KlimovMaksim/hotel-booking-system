package ru.klimov.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.dto.UserResponseDto;
import ru.klimov.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Создать нового пользователя")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserPayload payload) {
        return ResponseEntity.ok(userService.register(payload));
    }

    @Operation(summary = "Обновить данные пользователя")
    @PatchMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody UserPayload payload) {
        return ResponseEntity.ok(userService.updateUser(payload));
    }

    @Operation(summary = "Удалить пользователя")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
