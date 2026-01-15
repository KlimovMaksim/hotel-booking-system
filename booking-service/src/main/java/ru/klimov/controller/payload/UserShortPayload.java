package ru.klimov.controller.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserShortPayload {

    @Schema(description = "Имя пользователя", example = "ivan_ivanov")
    private String username;

    @Schema(description = "Пароль пользователя", example = "password123")
    private String password;
}
