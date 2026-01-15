package ru.klimov.controller.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
public class RoomPayload {

    @Schema(description = "Номер комнаты", example = "101")
    private String number;

    @Schema(description = "Доступность номера", example = "true")
    private Boolean available;

    @Schema(description = "Идентификатор отеля", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID hotelId;
}
