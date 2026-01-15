package ru.klimov.controller.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingPayload {

    @Schema(description = "Идентификатор комнаты", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID roomId;

    @Schema(description = "Дата начала бронирования", example = "2026-01-16")
    private LocalDate startDate;

    @Schema(description = "Дата окончания бронирования", example = "2026-01-20")
    private LocalDate endDate;

    @Schema(description = "Автоматический выбор номера", example = "false")
    private Boolean autoSelect;
}
