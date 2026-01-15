package ru.klimov.controller.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RoomReservationPayload {

    @Schema(description = "Дата начала бронирования", example = "2026-01-16")
    private LocalDate startDate;

    @Schema(description = "Дата окончания бронирования", example = "2026-01-20")
    private LocalDate endDate;

    @Schema(description = "Идентификатор запроса", example = "550e8400-e29b-41d4-a716-446655440000")
    private String requestId;
}
