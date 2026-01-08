package ru.klimov.controller.payload;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class UserPayload {

    private String username;

    private String password;

    private String role;
}
