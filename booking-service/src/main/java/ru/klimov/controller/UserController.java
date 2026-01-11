package ru.klimov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.dto.UserDto;
import ru.klimov.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserPayload payload) {
        return ResponseEntity.ok(userService.register(payload));
    }

    @PatchMapping
    public ResponseEntity<UserDto> updateUser(@RequestBody UserPayload payload) {
        return ResponseEntity.ok(userService.updateUser(payload));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
